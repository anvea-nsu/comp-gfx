package ecg.wireframe.view;

import ecg.wireframe.model.Scene;
import ecg.wireframe.model.SceneObserver;
import ecg.wireframe.model.Segment2D;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

/**
 * Viewport panel. Observes Scene and repaints when it changes.
 */
public class RenderPanel extends JPanel implements SceneObserver {

    private final Scene scene;

    private static final Color BG_COLOR    = new Color(15, 15, 25);
    private static final Color NEAR_COLOR  = new Color(100, 200, 255);
    private static final Color FAR_COLOR   = new Color(20,  60, 100);

    public RenderPanel(Scene scene) {
        this.scene = scene;
        scene.addObserver(this);
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(640, 480));
    }

    @Override
    public void onSceneChanged() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.0f));

        // Update view size in scene
        scene.setViewSize(getWidth(), getHeight());

        List<Segment2D> segments = scene.getProjectedSegments();

        if (segments.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("No figure to display. Add at least 4 control points.", 20, 30);
        } else {
            // Find global depth range across all segment endpoints
            double minD = Double.MAX_VALUE, maxD = -Double.MAX_VALUE;
            for (Segment2D s : segments) {
                if (s.depthA < minD) minD = s.depthA;
                if (s.depthA > maxD) maxD = s.depthA;
                if (s.depthB < minD) minD = s.depthB;
                if (s.depthB > maxD) maxD = s.depthB;
            }
            double range = maxD - minD;
            if (range < 1e-9) range = 1.0;

            for (Segment2D s : segments) {
                // Per-endpoint color based on its individual depth
                double tA = (s.depthA - minD) / range;
                double tB = (s.depthB - minD) / range;
                Color cA = interpolateColor(NEAR_COLOR, FAR_COLOR, tA);
                Color cB = interpolateColor(NEAR_COLOR, FAR_COLOR, tB);

                if (cA.equals(cB) ||
                    (Math.abs(s.x1 - s.x2) < 0.5 && Math.abs(s.y1 - s.y2) < 0.5)) {
                    // Same color or degenerate segment — plain line is fine
                    g2.setColor(cA);
                    g2.draw(new Line2D.Double(s.x1, s.y1, s.x2, s.y2));
                } else {
                    // GradientPaint: color changes per-pixel along the segment
                    GradientPaint gp = new GradientPaint(
                        (float) s.x1, (float) s.y1, cA,
                        (float) s.x2, (float) s.y2, cB
                    );
                    g2.setPaint(gp);
                    g2.draw(new Line2D.Double(s.x1, s.y1, s.x2, s.y2));
                }
            }
            // Restore solid paint for axes drawn next
            g2.setPaint(Color.WHITE);
        }

        // Draw XYZ axes in corner
        drawAxes(g2);
    }

    private void drawAxes(Graphics2D g2) {
        List<Segment2D> axes = scene.getAxisSegments2D();
        if (axes == null || axes.size() < 3) return;

        Color[] axisColors = {Color.RED, Color.GREEN, new Color(80, 120, 255)};
        String[] axisLabels = {"X", "Y", "Z"};

        g2.setStroke(new BasicStroke(2.0f));
        for (int i = 0; i < axes.size(); i++) {
            Segment2D s = axes.get(i);
            if (s == null) continue;
            g2.setColor(axisColors[i]);
            g2.draw(new Line2D.Double(s.x1, s.y1, s.x2, s.y2));
            g2.drawString(axisLabels[i], (int) s.x2 + 3, (int) s.y2 + 3);
        }
        g2.setStroke(new BasicStroke(1.0f));
    }

    private Color interpolateColor(Color near, Color far, double t) {
        t = Math.max(0, Math.min(1, t));
        int r  = (int) (near.getRed()   + t * (far.getRed()   - near.getRed()));
        int gv = (int) (near.getGreen() + t * (far.getGreen() - near.getGreen()));
        int b  = (int) (near.getBlue()  + t * (far.getBlue()  - near.getBlue()));
        return new Color(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, gv)),
            Math.max(0, Math.min(255, b))
        );
    }
}
