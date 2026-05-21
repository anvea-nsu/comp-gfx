package ecg.wireframe.model;

import ecg.wireframe.model.math.Point3D;

public class Segment3D {
    public Point3D a, b;

    public Segment3D(Point3D a, Point3D b) {
        this.a = a;
        this.b = b;
    }
}
