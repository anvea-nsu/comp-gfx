package view;

import model.*;
import model.Box;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainPanel extends JPanel {
    private final SceneModel sceneModel;
    private final RenderModel renderModel;
    private final Camera camera;
    private BufferedImage renderedImage;

    // Placeholder text shown before any scene is loaded
    private static final String HINT_LINE1 = "No scene loaded";
    private static final String HINT_LINE2 = "File → Open Scene";
    private static final Color  HINT_COLOR = new Color(160, 160, 160);

    public MainPanel(SceneModel sceneModel, RenderModel renderModel, Camera camera) {
        this.sceneModel  = sceneModel;
        this.renderModel = renderModel;
        this.camera      = camera;

        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                camera.updateAspectRatio(getWidth(), getHeight());
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ── Rendered image mode ─────────────────────────────────────────
        if (renderedImage != null) {
            g2d.drawImage(renderedImage, 0, 0, getWidth(), getHeight(), null);
            return;
        }

        // ── Wireframe mode ──────────────────────────────────────────────
        Color bg = new Color(
                renderModel.getBackgroundR(),
                renderModel.getBackgroundG(),
                renderModel.getBackgroundB()
        );
        g2d.setColor(bg);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        boolean sceneEmpty = sceneModel.getSpheres().isEmpty()
                && sceneModel.getBoxes().isEmpty()
                && sceneModel.getTriangles().isEmpty()
                && sceneModel.getQuadrangles().isEmpty();

        if (sceneEmpty) {
            drawHint(g2d);
            return;
        }

        // Choose wireframe colour for contrast with background
        boolean dark = bg.getRed() < 40 && bg.getGreen() < 40 && bg.getBlue() < 40;
        g2d.setColor(dark ? Color.YELLOW : new Color(30, 30, 30));
        g2d.setStroke(new BasicStroke(1.0f));

        for (Sphere    s : sceneModel.getSpheres())     drawSphere(g2d, s, getWidth(), getHeight());
        for (Box       b : sceneModel.getBoxes())       drawBox(g2d, b, getWidth(), getHeight());
        for (Triangle  t : sceneModel.getTriangles())   drawTriangle(g2d, t, getWidth(), getHeight());
        for (Quadrangle q : sceneModel.getQuadrangles()) drawQuadrangle(g2d, q, getWidth(), getHeight());

        drawAxisGizmo(g2d);
    }

    // ── Hint overlay ────────────────────────────────────────────────────

    private void drawHint(Graphics2D g2d) {
        g2d.setColor(HINT_COLOR);

        Font bigFont   = new Font(Font.SANS_SERIF, Font.PLAIN, 22);
        Font smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

        FontMetrics fm1 = g2d.getFontMetrics(bigFont);
        FontMetrics fm2 = g2d.getFontMetrics(smallFont);

        int cx = getWidth()  / 2;
        int cy = getHeight() / 2;

        g2d.setFont(bigFont);
        g2d.drawString(HINT_LINE1, cx - fm1.stringWidth(HINT_LINE1) / 2, cy - 8);

        g2d.setFont(smallFont);
        g2d.setColor(new Color(120, 120, 120));
        g2d.drawString(HINT_LINE2, cx - fm2.stringWidth(HINT_LINE2) / 2, cy + 22);
    }

    // ── Axis gizmo ───────────────────────────────────────────────────────

    private static final int   GIZMO_MARGIN = 14;   // px from bottom-left corner
    private static final int   GIZMO_LEN    = 40;   // axis arm length in px
    private static final int   GIZMO_DOT    = 4;    // dot radius at origin
    private static final float GIZMO_STROKE = 2.2f;
    private static final Font  GIZMO_FONT   = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    private void drawAxisGizmo(Graphics2D g2d) {
        if (camera.getForward() == null || camera.getRight() == null || camera.getUpCorrected() == null) {
            return;
        }

        // Gizmo origin in screen space (bottom-left corner)
        int ox = GIZMO_MARGIN + GIZMO_LEN;
        int oy = getHeight() - GIZMO_MARGIN - GIZMO_LEN;

        // Camera basis vectors (already normalised)
        double rx = camera.getRight().getX(),       ry = camera.getRight().getY(),       rz = camera.getRight().getZ();
        double ux = camera.getUpCorrected().getX(), uy = camera.getUpCorrected().getY(), uz = camera.getUpCorrected().getZ();
        double fx = camera.getForward().getX(),     fy = camera.getForward().getY(),     fz = camera.getForward().getZ();

        // World axes expressed in camera space → 2-D screen offset
        // X world = (1,0,0) projected onto right / upCorrected
        int[] xEnd = gizmoProject(ox, oy, 1, 0, 0, rx, ry, rz, ux, uy, uz);
        int[] yEnd = gizmoProject(ox, oy, 0, 1, 0, rx, ry, rz, ux, uy, uz);
        int[] zEnd = gizmoProject(ox, oy, 0, 0, 1, rx, ry, rz, ux, uy, uz);

        // Sort axes by depth (forward component) so the farthest is drawn first
        double depthX = rx * 1 + ry * 0 + rz * 0;  // dot(axis, forward) – use forward for depth
        // Actually project along forward for depth
        depthX = fx;  double depthY = fy;  double depthZ = fz;

        // Depth of each world axis tip along camera forward
        double dX = fx * 1;
        double dY = fy * 1;
        double dZ = fz * 1;

        // Draw farthest first (painter's algorithm)
        record Axis(int[] end, Color col, String label, double depth) {}
        Axis axX = new Axis(xEnd, new Color(220,  60,  60), "X", dX);
        Axis axY = new Axis(yEnd, new Color( 60, 200,  60), "Y", dY);
        Axis axZ = new Axis(zEnd, new Color( 80, 130, 255), "Z", dZ);
        Axis[] axes = {axX, axY, axZ};

        // Bubble-sort descending depth (draw back-to-front)
        for (int i = 0; i < 3; i++)
            for (int j = i + 1; j < 3; j++)
                if (axes[i].depth() < axes[j].depth()) {
                    Axis tmp = axes[i]; axes[i] = axes[j]; axes[j] = tmp;
                }

        // Semi-transparent background circle
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
        g2d.setColor(Color.BLACK);
        int r = GIZMO_LEN + 10;
        g2d.fillOval(ox - r, oy - r, r * 2, r * 2);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        Stroke oldStroke = g2d.getStroke();
        Font   oldFont   = g2d.getFont();
        g2d.setStroke(new BasicStroke(GIZMO_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setFont(GIZMO_FONT);

        for (Axis ax : axes) {
            g2d.setColor(ax.col());
            g2d.drawLine(ox, oy, ax.end()[0], ax.end()[1]);

            // Dot at tip
            g2d.fillOval(ax.end()[0] - GIZMO_DOT, ax.end()[1] - GIZMO_DOT,
                         GIZMO_DOT * 2, GIZMO_DOT * 2);

            // Label
            g2d.setColor(ax.col().brighter());
            g2d.drawString(ax.label(), ax.end()[0] + 4, ax.end()[1] + 4);
        }

        // Origin dot
        g2d.setColor(Color.WHITE);
        g2d.fillOval(ox - GIZMO_DOT, oy - GIZMO_DOT, GIZMO_DOT * 2, GIZMO_DOT * 2);

        g2d.setStroke(oldStroke);
        g2d.setFont(oldFont);
    }

    /** Projects a world-space unit vector onto camera right/up, returns screen tip coords. */
    private int[] gizmoProject(int ox, int oy,
                               double wx, double wy, double wz,
                               double rx, double ry, double rz,
                               double ux, double uy, double uz) {
        double screenX =  (rx * wx + ry * wy + rz * wz) * GIZMO_LEN;
        double screenY = -(ux * wx + uy * wy + uz * wz) * GIZMO_LEN;
        return new int[]{ ox + (int) Math.round(screenX), oy + (int) Math.round(screenY) };
    }

    // ── Wireframe draw helpers ───────────────────────────────────────────

    private void drawTriangle(Graphics2D g2d, Triangle triangle, int pw, int ph) {
        List<Point3D> pts = triangle.points();
        if (pts.size() != 3) return;

        drawLine3D(g2d, pts.get(0), pts.get(1), pw, ph);
        drawLine3D(g2d, pts.get(1), pts.get(2), pw, ph);
        drawLine3D(g2d, pts.get(2), pts.get(0), pw, ph);
    }

    private void drawQuadrangle(Graphics2D g2d, Quadrangle quadrangle, int pw, int ph) {
        List<Point3D> pts = quadrangle.points();
        if (pts.size() != 4) return;

        drawLine3D(g2d, pts.get(0), pts.get(1), pw, ph);
        drawLine3D(g2d, pts.get(1), pts.get(2), pw, ph);
        drawLine3D(g2d, pts.get(2), pts.get(3), pw, ph);
        drawLine3D(g2d, pts.get(3), pts.get(0), pw, ph);
    }

    private void drawBox(Graphics2D g2d, Box box, int pw, int ph) {
        List<Point3D> pts = box.points();
        if (pts.size() != 2) return;

        double minX = pts.get(0).getX(), minY = pts.get(0).getY(), minZ = pts.get(0).getZ();
        double maxX = pts.get(1).getX(), maxY = pts.get(1).getY(), maxZ = pts.get(1).getZ();

        Point3D p000 = new Point3D(minX, minY, minZ);
        Point3D p100 = new Point3D(maxX, minY, minZ);
        Point3D p110 = new Point3D(maxX, maxY, minZ);
        Point3D p010 = new Point3D(minX, maxY, minZ);
        Point3D p001 = new Point3D(minX, minY, maxZ);
        Point3D p101 = new Point3D(maxX, minY, maxZ);
        Point3D p111 = new Point3D(maxX, maxY, maxZ);
        Point3D p011 = new Point3D(minX, maxY, maxZ);

        // Bottom face
        drawLine3D(g2d, p000, p100, pw, ph);
        drawLine3D(g2d, p100, p110, pw, ph);
        drawLine3D(g2d, p110, p010, pw, ph);
        drawLine3D(g2d, p010, p000, pw, ph);
        // Top face
        drawLine3D(g2d, p001, p101, pw, ph);
        drawLine3D(g2d, p101, p111, pw, ph);
        drawLine3D(g2d, p111, p011, pw, ph);
        drawLine3D(g2d, p011, p001, pw, ph);
        // Verticals
        drawLine3D(g2d, p000, p001, pw, ph);
        drawLine3D(g2d, p100, p101, pw, ph);
        drawLine3D(g2d, p110, p111, pw, ph);
        drawLine3D(g2d, p010, p011, pw, ph);
    }

    private void drawSphere(Graphics2D g2d, Sphere sphere, int pw, int ph) {
        List<Point3D> pts = sphere.center();
        if (pts.size() != 1) return;

        Point3D center = pts.get(0);
        double  radius = sphere.radius();
        int     segs   = 48;

        drawSphereCircleXY(g2d, center, radius, segs, pw, ph);
        drawSphereCircleXZ(g2d, center, radius, segs, pw, ph);
        drawSphereCircleYZ(g2d, center, radius, segs, pw, ph);
    }

    private void drawSphereCircleXY(Graphics2D g2d, Point3D c, double r, int segs, int pw, int ph) {
        Point3D prev = null;
        for (int i = 0; i <= segs; i++) {
            double angle = 2.0 * Math.PI * i / segs;
            Point3D cur = new Point3D(c.getX() + r * Math.cos(angle), c.getY() + r * Math.sin(angle), c.getZ());
            if (prev != null) drawLine3D(g2d, prev, cur, pw, ph);
            prev = cur;
        }
    }

    private void drawSphereCircleXZ(Graphics2D g2d, Point3D c, double r, int segs, int pw, int ph) {
        Point3D prev = null;
        for (int i = 0; i <= segs; i++) {
            double angle = 2.0 * Math.PI * i / segs;
            Point3D cur = new Point3D(c.getX() + r * Math.cos(angle), c.getY(), c.getZ() + r * Math.sin(angle));
            if (prev != null) drawLine3D(g2d, prev, cur, pw, ph);
            prev = cur;
        }
    }

    private void drawSphereCircleYZ(Graphics2D g2d, Point3D c, double r, int segs, int pw, int ph) {
        Point3D prev = null;
        for (int i = 0; i <= segs; i++) {
            double angle = 2.0 * Math.PI * i / segs;
            Point3D cur = new Point3D(c.getX(), c.getY() + r * Math.cos(angle), c.getZ() + r * Math.sin(angle));
            if (prev != null) drawLine3D(g2d, prev, cur, pw, ph);
            prev = cur;
        }
    }

    private void drawLine3D(Graphics2D g2d, Point3D p1, Point3D p2, int pw, int ph) {
        Point3D s1 = camera.convertModelToScreen(p1, pw, ph);
        Point3D s2 = camera.convertModelToScreen(p2, pw, ph);

        if (s1 == null || s2 == null) return;

        g2d.drawLine(
                (int) Math.round(s1.getX()), (int) Math.round(s1.getY()),
                (int) Math.round(s2.getX()), (int) Math.round(s2.getY())
        );
    }

    // ── Image save ───────────────────────────────────────────────────────

    public void saveImage(String path) throws IOException {
        int width  = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            throw new IOException("Panel has invalid size");
        }

        BufferedImage image;

        if (renderedImage != null) {
            image = renderedImage;
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            try {
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, width, height);
                paint(g2d);
            } finally {
                g2d.dispose();
            }
        }

        String format = getImageFormat(path);

        if (!ImageIO.write(image, format, new File(path))) {
            throw new IOException("Unsupported image format: " + format);
        }
    }

    private String getImageFormat(String path) {
        String lp = path.toLowerCase();
        if (lp.endsWith(".jpg") || lp.endsWith(".jpeg")) return "jpg";
        if (lp.endsWith(".bmp"))  return "bmp";
        return "png";
    }

    public void clearRenderedImage() {
        renderedImage = null;
        repaint();
    }

    public void setRenderedImage(BufferedImage renderedImage) {
        this.renderedImage = renderedImage;
        repaint();
    }
}
