package ecg.wireframe.model;

public class Segment2D {
    public double x1, y1, x2, y2;
    public double depthA; // depth at point A (for gradient coloring)
    public double depthB; // depth at point B

    public Segment2D(double x1, double y1, double x2, double y2,
                     double depthA, double depthB) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.depthA = depthA;
        this.depthB = depthB;
    }
}
