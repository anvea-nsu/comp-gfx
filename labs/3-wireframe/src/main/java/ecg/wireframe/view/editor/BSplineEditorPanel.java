package ecg.wireframe.view.editor;

import ecg.wireframe.model.BSpline;
import ecg.wireframe.model.math.Point2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 2D canvas for editing B-spline control points.
 *
 * Left-drag  : move control point (if near one) or pan canvas
 * Right-click: add new point (click on empty space) / remove point (click near existing)
 * Wheel      : zoom canvas
 */
public class BSplineEditorPanel extends JPanel {

    private static final int    POINT_RADIUS = 7;
    private static final Color  BG           = new Color(30, 30, 45);
    private static final Color  AXIS_COLOR   = new Color(80, 80, 100);
    private static final Color  CTRL_LINE    = new Color(80, 120, 160);
    private static final Color  SPLINE_COLOR = new Color(80, 220, 100);
    private static final Color  POINT_COLOR  = new Color(220, 180, 60);
    private static final Color  SEL_COLOR    = new Color(255, 240, 120);
    private static final int    N_PREVIEW    = 20;

    // Control points in model space
    private final List<Point2D> controlPoints = new ArrayList<>();

    // Canvas transform: model → screen
    // screenX = modelX * scale + offsetX
    private double scale   = 150.0;
    private double offsetX = 0;
    private double offsetY = 0;

    // Interaction
    private int    draggedIndex = -1;
    private double dragStartSX, dragStartSY;
    private double panStartOX, panStartOY;
    private boolean panning = false;

    // Callback when points change
    private Runnable changeListener;

    public BSplineEditorPanel() {
        setBackground(BG);
        setPreferredSize(new Dimension(600, 500));
        setupMouseListeners();
    }

    // ── Model accessors ───────────────────────────────────────────────────────

    public List<Point2D> getControlPoints() {
        List<Point2D> copy = new ArrayList<>();
        for (Point2D p : controlPoints) copy.add(p.copy());
        return copy;
    }

    public void setControlPoints(List<Point2D> pts) {
        controlPoints.clear();
        for (Point2D p : pts) controlPoints.add(p.copy());
        centerView();
        repaint();
    }

    public void setChangeListener(Runnable r) { this.changeListener = r; }

    private void fireChange() {
        if (changeListener != null) changeListener.run();
    }

    // ── View helpers ──────────────────────────────────────────────────────────

    private double toScreenX(double mx) { return mx * scale + offsetX; }
    private double toScreenY(double my) { return -my * scale + offsetY; }
    private double toModelX(double sx)  { return (sx - offsetX) / scale; }
    private double toModelY(double sy)  { return -(sy - offsetY) / scale; }

    public void centerView() {
        offsetX = getWidth()  / 2.0;
        offsetY = getHeight() / 2.0;
        repaint();
    }

    public void autoNormalize() {
        if (controlPoints.isEmpty()) return;
        double minU = controlPoints.stream().mapToDouble(p -> p.u).min().orElse(0);
        double maxU = controlPoints.stream().mapToDouble(p -> p.u).max().orElse(1);
        double minV = controlPoints.stream().mapToDouble(p -> p.v).min().orElse(0);
        double maxV = controlPoints.stream().mapToDouble(p -> p.v).max().orElse(1);
        double rangeU = maxU - minU, rangeV = maxV - minV;
        double range = Math.max(rangeU, rangeV);
        if (range < 1e-9) return;
        double targetSize = 0.7 * Math.min(getWidth(), getHeight());
        scale = targetSize / range;
        double cu = (minU + maxU) / 2, cv = (minV + maxV) / 2;
        offsetX = getWidth()  / 2.0 - cu * scale;
        offsetY = getHeight() / 2.0 + cv * scale;
        repaint();
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    private void setupMouseListeners() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int idx = nearestPoint(e.getX(), e.getY());
                    if (idx >= 0) {
                        draggedIndex = idx;
                    } else {
                        // Start pan
                        panning = true;
                        dragStartSX = e.getX();
                        dragStartSY = e.getY();
                        panStartOX  = offsetX;
                        panStartOY  = offsetY;
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClick(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedIndex >= 0) {
                    Point2D p = controlPoints.get(draggedIndex);
                    p.u = toModelX(e.getX());
                    p.v = toModelY(e.getY());
                    repaint();
                    fireChange();
                } else if (panning) {
                    offsetX = panStartOX + (e.getX() - dragStartSX);
                    offsetY = panStartOY + (e.getY() - dragStartSY);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedIndex = -1;
                panning = false;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // getPreciseWheelRotation() is a double — handles trackpads and
                // smooth-scroll devices correctly. getWheelRotation() is int and
                // returns 0 for intermediate events, making zoom always go one way.
                double rotation = e.getPreciseWheelRotation();
                if (Math.abs(rotation) < 1e-3) return; // ignore zero-delta events
                double factor = rotation < 0 ? 1.1 : 1.0 / 1.1;
                double mx = toModelX(e.getX());
                double my = toModelY(e.getY());
                scale *= factor;
                offsetX = e.getX() - mx * scale;
                offsetY = e.getY() + my * scale;
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    private int nearestPoint(int sx, int sy) {
        for (int i = 0; i < controlPoints.size(); i++) {
            double dx = toScreenX(controlPoints.get(i).u) - sx;
            double dy = toScreenY(controlPoints.get(i).v) - sy;
            if (Math.sqrt(dx * dx + dy * dy) <= POINT_RADIUS + 4) return i;
        }
        return -1;
    }

    private void handleRightClick(int sx, int sy) {
        int idx = nearestPoint(sx, sy);
        if (idx >= 0) {
            // Remove point (keep minimum 4)
            if (controlPoints.size() > 4) {
                controlPoints.remove(idx);
                repaint();
                fireChange();
            } else {
                JOptionPane.showMessageDialog(this,
                    "At least 4 control points are required.",
                    "Cannot remove", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            // Add new point at click location
            double mu = toModelX(sx);
            double mv = toModelY(sy);
            // Insert after the nearest segment
            controlPoints.add(insertionIndex(mu, mv), new Point2D(mu, mv));
            repaint();
            fireChange();
        }
    }

    /** Find best insertion index to keep polyline order sensible. */
    private int insertionIndex(double mu, double mv) {
        if (controlPoints.size() < 2) return controlPoints.size();
        int best = controlPoints.size();
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            Point2D a = controlPoints.get(i);
            Point2D b = controlPoints.get(i + 1);
            double d = pointToSegDist(mu, mv, a.u, a.v, b.u, b.v);
            if (d < bestDist) { bestDist = d; best = i + 1; }
        }
        return best;
    }

    private double pointToSegDist(double px, double py,
                                   double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double len2 = dx * dx + dy * dy;
        if (len2 < 1e-12) return Math.hypot(px - ax, py - ay);
        double t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / len2));
        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    // ── Paint ─────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawAxes(g2);
        if (controlPoints.size() >= 2) drawControlPolyline(g2);
        if (controlPoints.size() >= 4) drawSpline(g2);
        drawControlPoints(g2);
    }

    private void drawAxes(Graphics2D g2) {
        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, new float[]{6, 4}, 0));
        int w = getWidth(), h = getHeight();
        // U axis (horizontal)
        int oy = (int) toScreenY(0);
        if (oy >= 0 && oy <= h) g2.drawLine(0, oy, w, oy);
        // V axis (vertical)
        int ox = (int) toScreenX(0);
        if (ox >= 0 && ox <= w) g2.drawLine(ox, 0, ox, h);

        g2.setStroke(new BasicStroke(1.0f));
        g2.setColor(AXIS_COLOR.brighter());
        g2.drawString("U", w - 15, oy - 4);
        g2.drawString("V", ox + 5, 15);
    }

    private void drawControlPolyline(Graphics2D g2) {
        g2.setColor(CTRL_LINE);
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[]{5, 4}, 0));
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            Point2D a = controlPoints.get(i), b = controlPoints.get(i + 1);
            g2.draw(new Line2D.Double(toScreenX(a.u), toScreenY(a.v),
                                      toScreenX(b.u), toScreenY(b.v)));
        }
        g2.setStroke(new BasicStroke(1.0f));
    }

    private void drawSpline(Graphics2D g2) {
        List<Point2D> pts = BSpline.computeSplinePoints(controlPoints, N_PREVIEW);
        g2.setColor(SPLINE_COLOR);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < pts.size() - 1; i++) {
            Point2D a = pts.get(i), b = pts.get(i + 1);
            g2.draw(new Line2D.Double(toScreenX(a.u), toScreenY(a.v),
                                      toScreenX(b.u), toScreenY(b.v)));
        }
        g2.setStroke(new BasicStroke(1.0f));
    }

    private void drawControlPoints(Graphics2D g2) {
        for (int i = 0; i < controlPoints.size(); i++) {
            Point2D p = controlPoints.get(i);
            int sx = (int) toScreenX(p.u);
            int sy = (int) toScreenY(p.v);
            boolean sel = (i == draggedIndex);
            g2.setColor(sel ? SEL_COLOR : POINT_COLOR);
            g2.fillOval(sx - POINT_RADIUS, sy - POINT_RADIUS,
                        POINT_RADIUS * 2, POINT_RADIUS * 2);
            g2.setColor(sel ? SEL_COLOR.darker() : POINT_COLOR.darker());
            g2.drawOval(sx - POINT_RADIUS, sy - POINT_RADIUS,
                        POINT_RADIUS * 2, POINT_RADIUS * 2);
            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(i), sx + POINT_RADIUS + 2, sy - POINT_RADIUS);
        }
    }
}
