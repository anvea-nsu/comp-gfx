package ecg.wireframe.model;

import ecg.wireframe.model.math.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Central model. Owns all state; rebuilds the full pipeline on demand.
 *
 * Pipeline:
 *   controlPoints → BSpline → splinePoints(2D)
 *   → RevolutionBody → worldSegments(3D)
 *   → normalize to [-1,1]³
 *   → user rotation (Rx * Ry * Rz)
 *   → lookAt + perspective
 *   → screen coordinates → projectedSegments(2D with depth)
 */
public class Scene {

    // Camera is fixed
    private static final Vector3 CAM_POS   = new Vector3(-10, 0, 0);
    private static final Vector3 CAM_TARGET = new Vector3( 10, 0, 0);
    private static final Vector3 CAM_UP     = new Vector3(  0, 1, 0);

    // ── State ────────────────────────────────────────────────────────────────
    private final List<Point2D>   controlPoints  = new ArrayList<>();
    private       SceneParameters params         = new SceneParameters();

    // Computed / cached
    private List<Point2D>   splinePoints        = new ArrayList<>();
    private List<Segment3D> worldSegments       = new ArrayList<>();
    private List<Segment2D> projectedSegments   = new ArrayList<>();

    // Axis segments (XYZ arrows) in world coords before rotation
    private List<Segment2D> axisSegments2D = new ArrayList<>();

    private int viewW = 800, viewH = 600;

    // Observers
    private final List<SceneObserver> observers = new ArrayList<>();

    // ── Construction ─────────────────────────────────────────────────────────
    public Scene() {
        initDefaultControlPoints();
        rebuild();
    }

    private void initDefaultControlPoints() {
        // Clamped endpoints: repeat first/last twice so B-spline actually
        // touches v=0 (the revolution axis) — gives a solid, not a hollow tube.
        controlPoints.add(new Point2D(0.0,  0.00)); // clamp top x2
        controlPoints.add(new Point2D(0.0,  0.00));
        controlPoints.add(new Point2D(0.15, 0.35));
        controlPoints.add(new Point2D(0.4,  0.50));
        controlPoints.add(new Point2D(0.6,  0.38));
        controlPoints.add(new Point2D(0.8,  0.45));
        controlPoints.add(new Point2D(1.0,  0.18));
        controlPoints.add(new Point2D(1.0,  0.00)); // clamp bottom x2
        controlPoints.add(new Point2D(1.0,  0.00));
    }

    // ── Observer ─────────────────────────────────────────────────────────────
    public void addObserver(SceneObserver o) { observers.add(o); }
    public void removeObserver(SceneObserver o) { observers.remove(o); }
    private void notifyObservers() { for (SceneObserver o : observers) o.onSceneChanged(); }

    // ── Rebuild pipeline ──────────────────────────────────────────────────────
    public synchronized void rebuild() {
        int K = controlPoints.size();
        if (K < 4) {
            projectedSegments = new ArrayList<>();
            axisSegments2D   = new ArrayList<>();
            notifyObservers();
            return;
        }

        // 1. Spline
        splinePoints = BSpline.computeSplinePoints(controlPoints, params.N);

        // 2. Revolution body
        worldSegments = RevolutionBody.generate(splinePoints, K, params.N, params.M, params.M1);

        if (worldSegments.isEmpty()) {
            projectedSegments = new ArrayList<>();
            axisSegments2D   = new ArrayList<>();
            notifyObservers();
            return;
        }

        // 3. Normalize to [-1,1]^3
        double[] bb = RevolutionBody.boundingBox(worldSegments);
        double cx = (bb[0] + bb[3]) / 2, cy = (bb[1] + bb[4]) / 2, cz = (bb[2] + bb[5]) / 2;
        double rangeX = bb[3] - bb[0], rangeY = bb[4] - bb[1], rangeZ = bb[5] - bb[2];
        double maxRange = Math.max(rangeX, Math.max(rangeY, rangeZ));
        if (maxRange < 1e-12) maxRange = 1.0;
        double scaleVal = 2.0 / maxRange;

        Matrix4 norm = Matrix4.scale(scaleVal, scaleVal, scaleVal)
                              .multiply(Matrix4.translate(-cx, -cy, -cz));

        // 4. User rotation — order Y*X*Z:
        //    apply Z first (roll), then X (pitch), then Y (yaw/spin).
        //    This way horizontal mouse drag (angleY) always spins around
        //    world Y, and vertical drag (angleX) tilts top/bottom.
        Matrix4 rot = Matrix4.rotateY(params.angleY)
                             .multiply(Matrix4.rotateX(params.angleX))
                             .multiply(Matrix4.rotateZ(params.angleZ));

        // 5. View + Projection
        Matrix4 view = Matrix4.lookAt(CAM_POS, CAM_TARGET, CAM_UP);

        double aspect = (viewH > 0) ? (double) viewW / viewH : 1.0;
        double sh = 0.5;
        double sw = sh * aspect;
        Matrix4 proj = Matrix4.perspective(params.zn, params.zf, sw, sh);

        // Combined: proj * view * rot * norm
        Matrix4 mvp = proj.multiply(view).multiply(rot).multiply(norm);

        // 6. Project all segments
        projectedSegments = new ArrayList<>(worldSegments.size());
        for (Segment3D seg : worldSegments) {
            Segment2D s2 = projectSegment(seg, mvp);
            if (s2 != null) projectedSegments.add(s2);
        }

        // 7. Axis segments (in normalized model space before user rotation)
        axisSegments2D = buildAxisSegments(proj.multiply(view).multiply(rot));

        notifyObservers();
    }

    private Segment2D projectSegment(Segment3D seg, Matrix4 mvp) {
        double[] pa = mvp.transform(seg.a.x, seg.a.y, seg.a.z, 1.0);
        double[] pb = mvp.transform(seg.b.x, seg.b.y, seg.b.z, 1.0);

        if (Math.abs(pa[3]) < 1e-9 || Math.abs(pb[3]) < 1e-9) return null;

        // Perspective divide
        double ax = pa[0] / pa[3], ay = pa[1] / pa[3];
        double bx = pb[0] / pb[3], by = pb[1] / pb[3];

        // Use homogeneous W as depth proxy (larger W = farther from camera)
        double depthA = pa[3];
        double depthB = pb[3];

        // NDC → screen
        double sx1 = (ax + 1.0) * 0.5 * viewW;
        double sy1 = (1.0 - ay) * 0.5 * viewH;
        double sx2 = (bx + 1.0) * 0.5 * viewW;
        double sy2 = (1.0 - by) * 0.5 * viewH;

        return new Segment2D(sx1, sy1, sx2, sy2, depthA, depthB);
    }

    private List<Segment2D> buildAxisSegments(Matrix4 viewRot) {
        // Draw axes in normalized model space (1.5 units long), in corner
        double len = 1.2;
        Point3D o  = new Point3D(0, 0, 0);
        Point3D px = new Point3D(len, 0, 0);
        Point3D py = new Point3D(0, len, 0);
        Point3D pz = new Point3D(0, 0, len);

        List<Segment2D> axes = new ArrayList<>();
        axes.add(projectAxisSeg(o, px, viewRot, 0)); // X - red
        axes.add(projectAxisSeg(o, py, viewRot, 1)); // Y - green
        axes.add(projectAxisSeg(o, pz, viewRot, 2)); // Z - blue
        return axes;
    }

    // depthA/depthB encode axis color (same value): 0=X, 1=Y, 2=Z
    private Segment2D projectAxisSeg(Point3D a, Point3D b, Matrix4 m, double colorCode) {
        double[] pa = m.transform(a.x, a.y, a.z, 1.0);
        double[] pb = m.transform(b.x, b.y, b.z, 1.0);
        if (Math.abs(pa[3]) < 1e-9 || Math.abs(pb[3]) < 1e-9) return null;

        // Map to a small corner (bottom-left, 80px square)
        int cx = 50, cy = viewH - 50, size = 40;
        double ax = (pa[0] / pa[3]) * size + cx;
        double ay = -(pa[1] / pa[3]) * size + cy;
        double bx = (pb[0] / pb[3]) * size + cx;
        double by = -(pb[1] / pb[3]) * size + cy;
        return new Segment2D(ax, ay, bx, by, colorCode, colorCode);
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public List<Point2D>   getControlPoints()    { return controlPoints; }
    public List<Point2D>   getSplinePoints()     { return splinePoints; }
    public List<Segment2D> getProjectedSegments(){ return projectedSegments; }
    public List<Segment2D> getAxisSegments2D()   { return axisSegments2D; }
    public SceneParameters getParams()           { return params; }

    public void setParams(SceneParameters p) {
        this.params = p;
        rebuild();
    }

    public void setViewSize(int w, int h) {
        this.viewW = w;
        this.viewH = h;
    }

    public void setControlPoints(List<Point2D> pts) {
        controlPoints.clear();
        controlPoints.addAll(pts);
        rebuild();
    }
}
