package ecg.wireframe.model.math;

public class Point2D {
    public double u, v;

    public Point2D(double u, double v) {
        this.u = u;
        this.v = v;
    }

    public Point2D copy() {
        return new Point2D(u, v);
    }

    @Override
    public String toString() {
        return u + " " + v;
    }
}
