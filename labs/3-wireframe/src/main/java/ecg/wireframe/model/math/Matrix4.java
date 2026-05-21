package ecg.wireframe.model.math;

/**
 * Row-major 4×4 matrix.
 * Convention: column vectors on the right → result = M * v.
 */
public class Matrix4 {
    private final double[][] m; // m[row][col]

    public Matrix4() {
        m = new double[4][4];
    }

    private Matrix4(double[][] data) {
        m = new double[4][4];
        for (int i = 0; i < 4; i++)
            System.arraycopy(data[i], 0, m[i], 0, 4);
    }

    // ── Factories ──────────────────────────────────────────────────────────────

    public static Matrix4 identity() {
        Matrix4 r = new Matrix4();
        for (int i = 0; i < 4; i++) r.m[i][i] = 1.0;
        return r;
    }

    public static Matrix4 rotateX(double angleDeg) {
        double a = Math.toRadians(angleDeg);
        double c = Math.cos(a), s = Math.sin(a);
        Matrix4 r = identity();
        r.m[1][1] =  c;  r.m[1][2] = -s;
        r.m[2][1] =  s;  r.m[2][2] =  c;
        return r;
    }

    public static Matrix4 rotateY(double angleDeg) {
        double a = Math.toRadians(angleDeg);
        double c = Math.cos(a), s = Math.sin(a);
        Matrix4 r = identity();
        r.m[0][0] =  c;  r.m[0][2] =  s;
        r.m[2][0] = -s;  r.m[2][2] =  c;
        return r;
    }

    public static Matrix4 rotateZ(double angleDeg) {
        double a = Math.toRadians(angleDeg);
        double c = Math.cos(a), s = Math.sin(a);
        Matrix4 r = identity();
        r.m[0][0] =  c;  r.m[0][1] = -s;
        r.m[1][0] =  s;  r.m[1][1] =  c;
        return r;
    }

    public static Matrix4 scale(double sx, double sy, double sz) {
        Matrix4 r = identity();
        r.m[0][0] = sx; r.m[1][1] = sy; r.m[2][2] = sz;
        return r;
    }

    public static Matrix4 translate(double tx, double ty, double tz) {
        Matrix4 r = identity();
        r.m[0][3] = tx; r.m[1][3] = ty; r.m[2][3] = tz;
        return r;
    }

    /**
     * Standard right-handed lookAt.
     * Camera looks from eye toward target; up defines orientation.
     */
    public static Matrix4 lookAt(Vector3 eye, Vector3 target, Vector3 up) {
        Vector3 f = target.subtract(eye).normalize();   // forward  (+Z in camera)
        Vector3 r = f.cross(up).normalize();             // right    (+X in camera)
        Vector3 u = r.cross(f);                          // corrected up (+Y in camera)

        Matrix4 mat = new Matrix4();
        mat.m[0][0] =  r.x; mat.m[0][1] =  r.y; mat.m[0][2] =  r.z; mat.m[0][3] = -r.dot(eye);
        mat.m[1][0] =  u.x; mat.m[1][1] =  u.y; mat.m[1][2] =  u.z; mat.m[1][3] = -u.dot(eye);
        mat.m[2][0] = -f.x; mat.m[2][1] = -f.y; mat.m[2][2] = -f.z; mat.m[2][3] =  f.dot(eye);
        mat.m[3][3] = 1.0;
        return mat;
    }

    /**
     * OpenGL-style symmetric perspective frustum.
     * @param zn  near plane distance
     * @param zf  far plane distance
     * @param sw  near plane width
     * @param sh  near plane height
     */
    public static Matrix4 perspective(double zn, double zf, double sw, double sh) {
        Matrix4 mat = new Matrix4();
        mat.m[0][0] = 2.0 * zn / sw;
        mat.m[1][1] = 2.0 * zn / sh;
        mat.m[2][2] = -(zf + zn) / (zf - zn);
        mat.m[2][3] = -2.0 * zf * zn / (zf - zn);
        mat.m[3][2] = -1.0;
        return mat;
    }

    // ── Operations ─────────────────────────────────────────────────────────────

    /** Returns this * other. */
    public Matrix4 multiply(Matrix4 other) {
        Matrix4 res = new Matrix4();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 4; k++)
                    res.m[i][j] += m[i][k] * other.m[k][j];
        return res;
    }

    /**
     * Transform homogeneous vector [x, y, z, w].
     * @return result array [x', y', z', w']
     */
    public double[] transform(double x, double y, double z, double w) {
        return new double[]{
            m[0][0]*x + m[0][1]*y + m[0][2]*z + m[0][3]*w,
            m[1][0]*x + m[1][1]*y + m[1][2]*z + m[1][3]*w,
            m[2][0]*x + m[2][1]*y + m[2][2]*z + m[2][3]*w,
            m[3][0]*x + m[3][1]*y + m[3][2]*z + m[3][3]*w
        };
    }

    public double get(int row, int col) { return m[row][col]; }
}
