package model;

public class Camera {
    private Point3D eye = new Point3D(-25, 0, 4);
    private Point3D view = new Point3D(0, 0, 1);
    private Point3D up = new Point3D(0, 0, 1);

    private double zn = 8;
    private double zf = 60;

    private double sw = 12;
    private double sh = 9;

    private Point3D forward;
    private Point3D right;
    private Point3D upCorrected;

    public Camera() {
        recalculate();
    }

    public Point3D getForward()     { return forward;     }
    public Point3D getRight()       { return right;       }
    public Point3D getUpCorrected() { return upCorrected; }

    public Point3D getEye() {
        return eye;
    }

    public Point3D getView() {
        return view;
    }

    public Point3D getUp() {
        return up;
    }

    public double getZn() {
        return zn;
    }

    public double getZf() {
        return zf;
    }

    public double getSw() {
        return sw;
    }

    public double getSh() {
        return sh;
    }

    public void setEye(Point3D eye) {
        this.eye = eye;
    }

    public void setView(Point3D view) {
        this.view = view;
    }

    public void setUp(Point3D up) {
        this.up = up;
    }

    public void setZn(double zn) {
        this.zn = zn;
    }

    public void setZf(double zf) {
        this.zf = zf;
    }

    public void setSw(double sw) {
        this.sw = sw;
    }

    public void setSh(double sh) {
        this.sh = sh;
    }

    public void clear() {
        eye = null;
        view = null;
        up = null;
        zn = 0;
        zf = 0;
        sw = 0;
        sh = 0;
    }

    public void recalculate() {
        calculateForward();
        calculateRight();
        calculateUpCorrected();
    }

    private void calculateForward() {
        double eyeX = eye.getX();
        double eyeY = eye.getY();
        double eyeZ = eye.getZ();

        double viewX = view.getX();
        double viewY = view.getY();
        double viewZ = view.getZ();

        forward = new Point3D(viewX - eyeX, viewY - eyeY, viewZ - eyeZ);

        normalizeVector(forward);
    }

    private void calculateRight() {
        right = vectorMultiply(forward, up);

        normalizeVector(right);
    }

    private void calculateUpCorrected() {
        upCorrected = vectorMultiply(right, forward);

        normalizeVector(upCorrected);
    }

    public Point3D convertModelToScreen(Point3D point, int panelWidth, int panelHeight) {
        Point3D eye = getEye();

        double zn = getZn();
        double sw = getSw();
        double sh = getSh();

        Point3D vectorToPoint = subVectors(point, eye);

        double xCam = scalarProduct(vectorToPoint, right);
        double yCam = scalarProduct(vectorToPoint, upCorrected);
        double zCam = scalarProduct(vectorToPoint, forward);

        if (zCam <= 0) {
            return null;
        }

        double xProj = zn * xCam / zCam;
        double yProj = zn * yCam / zCam;

        double screenX = (xProj + sw / 2.0) / sw * panelWidth;
        double screenY = (sh / 2.0 - yProj) / sh * panelHeight;

        return new Point3D(screenX, screenY, zCam);
    }

    private Point3D vectorMultiply(Point3D v1,  Point3D v2) {
        double v1X = v1.getX();
        double v1Y = v1.getY();
        double v1Z = v1.getZ();

        double v2X = v2.getX();
        double v2Y = v2.getY();
        double v2Z = v2.getZ();

        return new Point3D(v1Y * v2Z - v1Z * v2Y, v1Z * v2X - v1X * v2Z, v1X * v2Y - v1Y * v2X);
    }

    private double scalarProduct(Point3D v1, Point3D v2) {
        return v1.getX() * v2.getX()
                + v1.getY() * v2.getY()
                + v1.getZ() * v2.getZ();
    }

    private Point3D subVectors(Point3D v1, Point3D v2) {
        return new Point3D(v1.getX() - v2.getX(),v1.getY() - v2.getY(), v1.getZ() - v2.getZ());
    }

    private Point3D sumVectors(Point3D v1, Point3D v2) {
        double v1X = v1.getX();
        double v1Y = v1.getY();
        double v1Z = v1.getZ();

        double v2X = v2.getX();
        double v2Y = v2.getY();
        double v2Z = v2.getZ();

        return new Point3D(v1X + v2X, v1Y + v2Y, v1Z + v2Z);
    }

    private Point3D scalarMultiply(double scalar, Point3D v) {
        return new Point3D(v.getX() * scalar, v.getY() * scalar, v.getZ() * scalar);
    }

    public void rotateAroundView(double dx, double dy) {
        double sensitivity = 0.005;

        Point3D eyeVector = subVectors(eye, view);

        Point3D worldUp = new Point3D(0, 0, 1);

        eyeVector = rotateVectorAroundAxis(eyeVector, worldUp, -dx * sensitivity);

        recalculate();

        eyeVector = rotateVectorAroundAxis(eyeVector, right, -dy * sensitivity);

        eye = sumVectors(view, eyeVector);

        recalculate();
    }

    private Point3D rotateVectorAroundAxis(Point3D vector, Point3D axis, double angle) {
        Point3D normalizedAxis = new Point3D(axis.getX(), axis.getY(), axis.getZ());
        normalizeVector(normalizedAxis);

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        Point3D part1 = scalarMultiply(cos, vector);

        Point3D axisCrossVector = vectorMultiply(normalizedAxis, vector);
        Point3D part2 = scalarMultiply(sin, axisCrossVector);

        double dot = scalarProduct(normalizedAxis, vector);
        Point3D part3 = scalarMultiply(dot * (1.0 - cos), normalizedAxis);

        return sumVectors(sumVectors(part1, part2), part3);
    }

    public void zoom(int wheelRotation) {
        double zoomFactor = 1.1;

        if (wheelRotation > 0) {
            zn /= zoomFactor;
        } else if (wheelRotation < 0) {
            zn *= zoomFactor;
        }

        if (zn < 0.001) {
            zn = 0.001;
        }
    }

    private void normalizeVector(Point3D vector) {
        double len = Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY() + vector.getZ() * vector.getZ());

        if (len == 0) {
            return;
        }

        vector.setX(vector.getX() / len);
        vector.setY(vector.getY() / len);
        vector.setZ(vector.getZ() / len);
    }

    public Camera copy() {
        Camera copy = new Camera();

        if (eye != null) {
            copy.setEye(new Point3D(eye.getX(), eye.getY(), eye.getZ()));
        }

        if (view != null) {
            copy.setView(new Point3D(view.getX(), view.getY(), view.getZ()));
        }

        if (up != null) {
            copy.setUp(new Point3D(up.getX(), up.getY(), up.getZ()));
        }

        copy.setZn(zn);
        copy.setZf(zf);
        copy.setSw(sw);
        copy.setSh(sh);

        copy.recalculate();

        return copy;
    }

    public void copyFrom(Camera other) {
        if (other == null) {
            return;
        }

        if (other.getEye() != null) {
            eye = new Point3D(other.getEye().getX(),other.getEye().getY(),other.getEye().getZ());
        }

        if (other.getView() != null) {
            view = new Point3D(other.getView().getX(),other.getView().getY(), other.getView().getZ());
        }

        if (other.getUp() != null) {
            up = new Point3D(other.getUp().getX(), other.getUp().getY(), other.getUp().getZ());
        }

        zn = other.getZn();
        zf = other.getZf();
        sw = other.getSw();
        sh = other.getSh();

        recalculate();
    }

    public void updateAspectRatio(int panelWidth, int panelHeight) {
        if (panelWidth <= 0 || panelHeight <= 0) {
            return;
        }

        double aspect = (double) panelWidth / panelHeight;

        sw = sh * aspect;
    }

    public Ray buildRay(int pixelX, int pixelY, int panelWidth, int panelHeight) {
        return buildRay(pixelX + 0.5, pixelY + 0.5, panelWidth, panelHeight);
    }

    public Ray buildRay(double pixelX, double pixelY, int panelWidth, int panelHeight) {
        double xScreen = (pixelX / panelWidth - 0.5) * sw;
        double yScreen = (0.5 - pixelY / panelHeight) * sh;

        Point3D centerNearPlane = sumVectors(eye, scalarMultiply(zn, forward));

        Point3D pointOnNearPlane = sumVectors(
                sumVectors(centerNearPlane, scalarMultiply(xScreen, right)),
                scalarMultiply(yScreen, upCorrected)
        );

        Point3D direction = subVectors(pointOnNearPlane, eye);
        normalizeVector(direction);

        return new Ray(
                new Point3D(eye.getX(), eye.getY(), eye.getZ()),
                direction
        );
    }

    private Point3D calculatePointOnNearPlane(int panelWidth, int panelHeight, double pixelX, double pixelY) {
        double offsetX = (pixelX - panelWidth / 2.0) / (panelWidth / 2.0) * (sw / 2.0);
        double offsetY = (panelHeight / 2.0 - pixelY) / (panelHeight / 2.0) * (sh / 2.0);

        Point3D centerNearPlane = sumVectors(eye, scalarMultiply(zn, forward));

        return sumVectors(sumVectors(centerNearPlane, scalarMultiply(offsetX, right)), scalarMultiply(offsetY, upCorrected));
    }

    public void moveAlongView(int wheelRotation) {
        double step = wheelRotation * 0.7;

        Point3D shift = scalarMultiply(step, forward);

        eye = sumVectors(eye, shift);
        view = sumVectors(view, shift);

        recalculate();
    }

    public void moveHorizontal(int direction) {
        double step = 0.5 * direction;

        Point3D shift = scalarMultiply(step, right);

        eye = sumVectors(eye, shift);
        view = sumVectors(view, shift);

        recalculate();
    }

    public void moveVertical(int direction) {
        double step = 0.5 * direction;

        Point3D shift = scalarMultiply(step, upCorrected);

        eye = sumVectors(eye, shift);
        view = sumVectors(view, shift);

        recalculate();
    }
}
