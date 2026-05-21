package render;

import model.*;
import model.Box;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class RayTracer {
    private static final double EPS = 1e-7;
    private static final double RAY_SHIFT = 1e-4;

    private final SceneModel sceneModel;
    private final RenderModel renderModel;
    private final Camera camera;

    public RayTracer(SceneModel sceneModel, RenderModel renderModel, Camera camera) {
        this.sceneModel = sceneModel;
        this.renderModel = renderModel;
        this.camera = camera;
    }

    public BufferedImage render(int width, int height, IntConsumer progressConsumer) {
        if (width <= 0) {
            width = 800;
        }

        if (height <= 0) {
            height = 600;
        }

        if (progressConsumer != null) {
            progressConsumer.accept(0);
        }

        ColorRGB[][] rawPixels = new ColorRGB[height][width];

        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        AtomicInteger completedRows = new AtomicInteger(0);
        AtomicInteger lastProgress = new AtomicInteger(-1);

        List<Future<?>> futures = new ArrayList<>();

        int finalWidth = width;
        int finalHeight = height;

        for (int row = 0; row < finalHeight; row++) {
            final int y = row;

            Future<?> future = executorService.submit(() -> {
                for (int x = 0; x < finalWidth; x++) {
                    rawPixels[y][x] = renderPixelFine(x, y, finalWidth, finalHeight);
                }

                int doneRows = completedRows.incrementAndGet();
                int progress = (int) (doneRows * 100.0 / finalHeight);

                if (progress != lastProgress.get()) {
                    lastProgress.set(progress);

                    if (progressConsumer != null) {
                        progressConsumer.accept(progress);
                    }
                }
            });

            futures.add(future);
        }

        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            throw new RuntimeException("Render interrupted: " + e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }

        double maxComponent = findMaxComponent(rawPixels, finalWidth, finalHeight);

        if (maxComponent <= EPS) {
            maxComponent = 1.0;
        }

        BufferedImage image = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);

        double gamma = renderModel.getGamma();

        if (gamma <= EPS) {
            gamma = 1.0;
        }

        for (int y = 0; y < finalHeight; y++) {
            for (int x = 0; x < finalWidth; x++) {
                ColorRGB color = rawPixels[y][x];

                double r = color.r / maxComponent;
                double g = color.g / maxComponent;
                double b = color.b / maxComponent;

                r = Math.pow(clamp01(r), 1.0 / gamma);
                g = Math.pow(clamp01(g), 1.0 / gamma);
                b = Math.pow(clamp01(b), 1.0 / gamma);

                int ir = to255(r);
                int ig = to255(g);
                int ib = to255(b);

                image.setRGB(x, y, new Color(ir, ig, ib).getRGB());
            }
        }

        if (progressConsumer != null) {
            progressConsumer.accept(100);
        }

        return image;
    }

    private ColorRGB renderPixelFine(int x, int y, int width, int height) {
        ColorRGB sum = ColorRGB.BLACK;

        sum = addColor(sum, trace(camera.buildRay(x + 0.25, y + 0.25, width, height), renderModel.getDepth()));
        sum = addColor(sum, trace(camera.buildRay(x + 0.75, y + 0.25, width, height), renderModel.getDepth()));
        sum = addColor(sum, trace(camera.buildRay(x + 0.25, y + 0.75, width, height), renderModel.getDepth()));
        sum = addColor(sum, trace(camera.buildRay(x + 0.75, y + 0.75, width, height), renderModel.getDepth()));

        return mulColor(0.25, sum);
    }

    private double findMaxComponent(ColorRGB[][] rawPixels, int width, int height) {
        double maxComponent = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ColorRGB color = rawPixels[y][x];

                maxComponent = Math.max(maxComponent, color.r);
                maxComponent = Math.max(maxComponent, color.g);
                maxComponent = Math.max(maxComponent, color.b);
            }
        }

        return maxComponent;
    }

    private ColorRGB trace(Ray ray, int depth) {
        if (depth <= 0) {
            return ColorRGB.BLACK;
        }

        Hit hit = findNearestHit(ray);

        if (hit == null) {
            return getBackgroundColor();
        }

        ColorRGB local = shade(ray, hit);

        if (depth == 1) {
            return local;
        }

        OpticalChar material = hit.opticalChar();

        Point3D reflectedDir = reflect(ray.direction(), hit.normal());
        normalize(reflectedDir);

        Ray reflectedRay = new Ray(
                add(hit.point(), mul(RAY_SHIFT, hit.normal())),
                reflectedDir
        );

        ColorRGB reflected = trace(reflectedRay, depth - 1);

        return new ColorRGB(
                local.r + ksR(material) * reflected.r,
                local.g + ksG(material) * reflected.g,
                local.b + ksB(material) * reflected.b
        );
    }

    private ColorRGB shade(Ray ray, Hit hit) {
        OpticalChar material = hit.opticalChar();

        double ambientR = sceneModel.getAmbientRed() / 255.0;
        double ambientG = sceneModel.getAmbientGreen() / 255.0;
        double ambientB = sceneModel.getAmbientBlue() / 255.0;

        double resultR = ambientR * kdR(material);
        double resultG = ambientG * kdG(material);
        double resultB = ambientB * kdB(material);

        for (Light light : sceneModel.getLights()) {
            Point3D lightPos = getLightPosition(light);
            ColorRGB lightColor = getLightColor(light);

            if (!isLightVisible(hit.point(), lightPos)) {
                continue;
            }

            Point3D toLight = sub(lightPos, hit.point());
            double distanceToLight = length(toLight);
            normalize(toLight);

            double diffuseFactor = Math.max(0.0, dot(hit.normal(), toLight));

            Point3D viewDir = mul(-1.0, ray.direction());
            normalize(viewDir);

            Point3D h = add(toLight, viewDir);
            normalize(h);

            double specFactor = Math.pow(
                    Math.max(0.0, dot(hit.normal(), h)),
                    power(material)
            );

            double attenuation = 1.0 / (1.0 + distanceToLight);

            resultR += attenuation * lightColor.r * (
                    kdR(material) * diffuseFactor + ksR(material) * specFactor
            );

            resultG += attenuation * lightColor.g * (
                    kdG(material) * diffuseFactor + ksG(material) * specFactor
            );

            resultB += attenuation * lightColor.b * (
                    kdB(material) * diffuseFactor + ksB(material) * specFactor
            );
        }

        return new ColorRGB(resultR, resultG, resultB);
    }

    private static final double SHADOW_EPS = 1e-2;

    private boolean isLightVisible(Point3D point, Point3D lightPos) {
        Point3D direction = sub(point, lightPos);
        double maxDistance = length(direction);

        if (maxDistance <= EPS) {
            return true;
        }

        normalize(direction);

        Ray shadowRay = new Ray(
                add(lightPos, mul(RAY_SHIFT, direction)),
                direction
        );

        Hit hit = findNearestHitBeforeDistance(shadowRay, maxDistance - SHADOW_EPS);

        return hit == null;
    }

    private Hit findNearestHitBeforeDistance(Ray ray, double maxDistance) {
        Hit nearest = null;

        for (Sphere sphere : sceneModel.getSpheres()) {
            Hit hit = intersectSphere(ray, sphere);

            if (hit != null && hit.distance() < maxDistance) {
                nearest = chooseNearest(nearest, hit);
            }
        }

        for (Triangle triangle : sceneModel.getTriangles()) {
            Hit hit = intersectTriangle(ray, triangle.points(), triangle.opticalChar());

            if (hit != null && hit.distance() < maxDistance) {
                nearest = chooseNearest(nearest, hit);
            }
        }

        for (Quadrangle quadrangle : sceneModel.getQuadrangles()) {
            Hit hit = intersectQuadrangle(ray, quadrangle);

            if (hit != null && hit.distance() < maxDistance) {
                nearest = chooseNearest(nearest, hit);
            }
        }

        for (Box box : sceneModel.getBoxes()) {
            Hit hit = intersectBox(ray, box);

            if (hit != null && hit.distance() < maxDistance) {
                nearest = chooseNearest(nearest, hit);
            }
        }

        return nearest;
    }

    private Hit findNearestHit(Ray ray) {
        Hit nearest = null;

        for (Sphere sphere : sceneModel.getSpheres()) {
            Hit hit = intersectSphere(ray, sphere);
            nearest = chooseNearest(nearest, hit);
        }

        for (Triangle triangle : sceneModel.getTriangles()) {
            Hit hit = intersectTriangle(ray, triangle.points(), triangle.opticalChar());
            nearest = chooseNearest(nearest, hit);
        }

        for (Quadrangle quadrangle : sceneModel.getQuadrangles()) {
            Hit hit = intersectQuadrangle(ray, quadrangle);
            nearest = chooseNearest(nearest, hit);
        }

        for (Box box : sceneModel.getBoxes()) {
            Hit hit = intersectBox(ray, box);
            nearest = chooseNearest(nearest, hit);
        }

        return nearest;
    }

    private Hit chooseNearest(Hit currentNearest, Hit candidate) {
        if (candidate == null) {
            return currentNearest;
        }

        if (candidate.distance() <= EPS) {
            return currentNearest;
        }

        if (currentNearest == null || candidate.distance() < currentNearest.distance()) {
            return candidate;
        }

        return currentNearest;
    }

    private Hit intersectSphere(Ray ray, Sphere sphere) {
        List<Point3D> centers = sphere.center();

        if (centers == null || centers.size() != 1) {
            return null;
        }

        Point3D center = centers.get(0);
        double radius = sphere.radius();

        Point3D oc = sub(ray.origin(), center);

        double a = dot(ray.direction(), ray.direction());
        double b = 2.0 * dot(oc, ray.direction());
        double c = dot(oc, oc) - radius * radius;

        double discriminant = b * b - 4.0 * a * c;

        if (discriminant < 0.0) {
            return null;
        }

        double sqrtD = Math.sqrt(discriminant);

        double t1 = (-b - sqrtD) / (2.0 * a);
        double t2 = (-b + sqrtD) / (2.0 * a);

        double t = Double.POSITIVE_INFINITY;

        if (t1 > EPS) {
            t = t1;
        } else if (t2 > EPS) {
            t = t2;
        }

        if (!Double.isFinite(t)) {
            return null;
        }

        Point3D point = add(ray.origin(), mul(t, ray.direction()));
        Point3D normal = sub(point, center);
        normalize(normal);

        if (dot(normal, ray.direction()) >= 0.0) {
            return null;
        }

        return new Hit(t, point, normal, sphere.opticalChar());
    }

    private Hit intersectTriangle(Ray ray, List<Point3D> points, OpticalChar opticalChar) {
        if (points == null || points.size() != 3) {
            return null;
        }

        return intersectTriangleByPoints(
                ray,
                points.get(0),
                points.get(1),
                points.get(2),
                opticalChar
        );
    }

    private Hit intersectTriangleByPoints(
            Ray ray,
            Point3D p0,
            Point3D p1,
            Point3D p2,
            OpticalChar opticalChar
    ) {
        Point3D edge1 = sub(p1, p0);
        Point3D edge2 = sub(p2, p0);

        Point3D normal = cross(edge1, edge2);
        normalize(normal);

        double denom = dot(normal, ray.direction());

        if (denom >= -EPS) {
            return null;
        }

        double t = dot(sub(p0, ray.origin()), normal) / denom;

        if (t <= EPS) {
            return null;
        }

        Point3D point = add(ray.origin(), mul(t, ray.direction()));

        if (!isPointInsideTriangleOneSided(point, p0, p1, p2, normal)) {
            return null;
        }

        return new Hit(t, point, normal, opticalChar);
    }

    private boolean isPointInsideTriangleOneSided(Point3D p, Point3D a, Point3D b, Point3D c, Point3D normal) {
        Point3D ab = sub(b, a);
        Point3D bp = sub(p, b);

        Point3D bc = sub(c, b);
        Point3D cp = sub(p, c);

        Point3D ca = sub(a, c);
        Point3D ap = sub(p, a);

        double d1 = dot(cross(ab, bp), normal);
        double d2 = dot(cross(bc, cp), normal);
        double d3 = dot(cross(ca, ap), normal);

        return d1 >= -EPS && d2 >= -EPS && d3 >= -EPS;
    }

    private Hit intersectQuadrangle(Ray ray, Quadrangle quadrangle) {
        List<Point3D> points = quadrangle.points();

        if (points == null || points.size() != 4) {
            return null;
        }

        Point3D p0 = points.get(0);
        Point3D p1 = points.get(1);
        Point3D p2 = points.get(2);
        Point3D p3 = points.get(3);

        Hit first = intersectTriangleByPoints(
                ray,
                p0,
                p1,
                p2,
                quadrangle.opticalChar()
        );

        Hit second = intersectTriangleByPoints(
                ray,
                p0,
                p2,
                p3,
                quadrangle.opticalChar()
        );

        return chooseNearest(first, second);
    }

    private Hit intersectBox(Ray ray, Box box) {
        List<Point3D> points = box.points();

        if (points == null || points.size() != 2) {
            return null;
        }

        Point3D a = points.get(0);
        Point3D b = points.get(1);

        double minX = Math.min(a.getX(), b.getX());
        double minY = Math.min(a.getY(), b.getY());
        double minZ = Math.min(a.getZ(), b.getZ());

        double maxX = Math.max(a.getX(), b.getX());
        double maxY = Math.max(a.getY(), b.getY());
        double maxZ = Math.max(a.getZ(), b.getZ());

        SlabResult result = intersectSlabs(ray, minX, minY, minZ, maxX, maxY, maxZ);

        if (result == null || result.t <= EPS) {
            return null;
        }

        Point3D point = add(ray.origin(), mul(result.t, ray.direction()));
        Point3D normal = result.normal;

        if (normal == null) {
            return null;
        }

        if (dot(normal, ray.direction()) >= 0.0) {
            return null;
        }

        return new Hit(result.t, point, normal, box.opticalChar());
    }

    private SlabResult intersectSlabs(
            Ray ray,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        double tMin = -Double.MAX_VALUE;
        double tMax = Double.MAX_VALUE;

        Point3D normalMin = null;
        Point3D normalMax = null;

        SlabAxisResult x = intersectAxis(
                ray.origin().getX(),
                ray.direction().getX(),
                minX,
                maxX,
                new Point3D(-1, 0, 0),
                new Point3D(1, 0, 0)
        );

        if (x == null) {
            return null;
        }

        if (x.tNear > tMin) {
            tMin = x.tNear;
            normalMin = x.normalNear;
        }

        if (x.tFar < tMax) {
            tMax = x.tFar;
            normalMax = x.normalFar;
        }

        if (tMin > tMax) {
            return null;
        }

        SlabAxisResult y = intersectAxis(
                ray.origin().getY(),
                ray.direction().getY(),
                minY,
                maxY,
                new Point3D(0, -1, 0),
                new Point3D(0, 1, 0)
        );

        if (y == null) {
            return null;
        }

        if (y.tNear > tMin) {
            tMin = y.tNear;
            normalMin = y.normalNear;
        }

        if (y.tFar < tMax) {
            tMax = y.tFar;
            normalMax = y.normalFar;
        }

        if (tMin > tMax) {
            return null;
        }

        SlabAxisResult z = intersectAxis(
                ray.origin().getZ(),
                ray.direction().getZ(),
                minZ,
                maxZ,
                new Point3D(0, 0, -1),
                new Point3D(0, 0, 1)
        );

        if (z == null) {
            return null;
        }

        if (z.tNear > tMin) {
            tMin = z.tNear;
            normalMin = z.normalNear;
        }

        if (z.tFar < tMax) {
            tMax = z.tFar;
            normalMax = z.normalFar;
        }

        if (tMin > tMax) {
            return null;
        }

        if (tMin > EPS) {
            return new SlabResult(tMin, normalMin);
        }

        if (tMax > EPS) {
            return new SlabResult(tMax, normalMax);
        }

        return null;
    }

    private SlabAxisResult intersectAxis(
            double origin,
            double direction,
            double min,
            double max,
            Point3D normalMin,
            Point3D normalMax
    ) {
        if (Math.abs(direction) < EPS) {
            if (origin < min || origin > max) {
                return null;
            }

            return new SlabAxisResult(
                    -Double.MAX_VALUE,
                    Double.MAX_VALUE,
                    normalMin,
                    normalMax
            );
        }

        double t1 = (min - origin) / direction;
        double t2 = (max - origin) / direction;

        Point3D n1 = normalMin;
        Point3D n2 = normalMax;

        if (t1 > t2) {
            double tempT = t1;
            t1 = t2;
            t2 = tempT;

            Point3D tempN = n1;
            n1 = n2;
            n2 = tempN;
        }

        return new SlabAxisResult(t1, t2, n1, n2);
    }

    private Point3D reflect(Point3D direction, Point3D normal) {
        double d = dot(direction, normal);
        return sub(direction, mul(2.0 * d, normal));
    }

    private ColorRGB getBackgroundColor() {
        return new ColorRGB(
                renderModel.getBackgroundR() / 255.0,
                renderModel.getBackgroundG() / 255.0,
                renderModel.getBackgroundB() / 255.0
        );
    }

    private Point3D getLightPosition(Light light) {
        return new Point3D(light.x(), light.y(), light.z());
    }

    private ColorRGB getLightColor(Light light) {
        return new ColorRGB(
                light.r() / 255.0,
                light.g() / 255.0,
                light.b() / 255.0
        );
    }

    private double kdR(OpticalChar opticalChar) {
        return opticalChar.diffR();
    }

    private double kdG(OpticalChar opticalChar) {
        return opticalChar.diffG();
    }

    private double kdB(OpticalChar opticalChar) {
        return opticalChar.diffB();
    }

    private double ksR(OpticalChar opticalChar) {
        return opticalChar.specR();
    }

    private double ksG(OpticalChar opticalChar) {
        return opticalChar.specG();
    }

    private double ksB(OpticalChar opticalChar) {
        return opticalChar.specB();
    }

    private double power(OpticalChar opticalChar) {
        return opticalChar.power();
    }

    private ColorRGB addColor(ColorRGB a, ColorRGB b) {
        return new ColorRGB(
                a.r + b.r,
                a.g + b.g,
                a.b + b.b
        );
    }

    private ColorRGB mulColor(double k, ColorRGB color) {
        return new ColorRGB(
                k * color.r,
                k * color.g,
                k * color.b
        );
    }

    private double dot(Point3D a, Point3D b) {
        return a.getX() * b.getX()
                + a.getY() * b.getY()
                + a.getZ() * b.getZ();
    }

    private Point3D cross(Point3D a, Point3D b) {
        return new Point3D(
                a.getY() * b.getZ() - a.getZ() * b.getY(),
                a.getZ() * b.getX() - a.getX() * b.getZ(),
                a.getX() * b.getY() - a.getY() * b.getX()
        );
    }

    private Point3D add(Point3D a, Point3D b) {
        return new Point3D(
                a.getX() + b.getX(),
                a.getY() + b.getY(),
                a.getZ() + b.getZ()
        );
    }

    private Point3D sub(Point3D a, Point3D b) {
        return new Point3D(
                a.getX() - b.getX(),
                a.getY() - b.getY(),
                a.getZ() - b.getZ()
        );
    }

    private Point3D mul(double k, Point3D v) {
        return new Point3D(
                k * v.getX(),
                k * v.getY(),
                k * v.getZ()
        );
    }

    private double length(Point3D v) {
        return Math.sqrt(dot(v, v));
    }

    private void normalize(Point3D v) {
        double len = length(v);

        if (len <= EPS) {
            return;
        }

        v.setX(v.getX() / len);
        v.setY(v.getY() / len);
        v.setZ(v.getZ() / len);
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private int to255(double value) {
        return (int) (clamp01(value) * 255.0 + 0.5);
    }

    private record ColorRGB(double r, double g, double b) {
        private static final ColorRGB BLACK = new ColorRGB(0, 0, 0);
    }

    private record SlabResult(double t, Point3D normal) {
    }

    private record SlabAxisResult(
            double tNear,
            double tFar,
            Point3D normalNear,
            Point3D normalFar
    ) {
    }
}