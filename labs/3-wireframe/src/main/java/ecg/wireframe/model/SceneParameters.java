package ecg.wireframe.model;

public class SceneParameters {
    public int N = 20;   // segments per B-spline section
    public int M = 18;   // number of generatrices
    public int M1 = 4;   // segments between generatrices on circles

    public double zn = 3.0;   // near plane (zoom)
    public double zf = 100.0; // far plane

    public double angleX = 25.0;   // pitch (vertical tilt)
    public double angleY = -40.0;  // yaw   (horizontal spin)
    public double angleZ = 0.0;    // roll

    public SceneParameters copy() {
        SceneParameters p = new SceneParameters();
        p.N = N; p.M = M; p.M1 = M1;
        p.zn = zn; p.zf = zf;
        p.angleX = angleX; p.angleY = angleY; p.angleZ = angleZ;
        return p;
    }
}
