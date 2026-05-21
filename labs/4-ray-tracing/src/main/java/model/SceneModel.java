package model;

import java.util.ArrayList;
import java.util.List;

public class SceneModel {
    private int ambientRed = 0;
    private int ambientGreen = 0;
    private int ambientBlue = 0;

    private int lightCount = 0;
    private final List<Light> lights = new ArrayList<>();

    private final List<Sphere> spheres = new ArrayList<>();
    private final List<Box> boxes = new ArrayList<>();
    private final List<Triangle> triangles = new ArrayList<>();
    private final List<Quadrangle> quadrangles = new ArrayList<>();

    public int getAmbientRed() { return ambientRed; }
    public int getAmbientGreen() { return ambientGreen; }
    public int getAmbientBlue() { return ambientBlue; }
    public int getLightCount() { return lightCount; }
    public List<Sphere> getSpheres() { return spheres; }
    public List<Box> getBoxes() { return boxes; }
    public List<Triangle> getTriangles() { return triangles; }
    public List<Quadrangle> getQuadrangles() { return quadrangles; }
    public List<Light> getLights() { return lights; }

    public void setAmbientRed(int ambientRed) { this.ambientRed = ambientRed; }
    public void setAmbientGreen(int ambientGreen) { this.ambientGreen = ambientGreen; }
    public void setAmbientBlue(int ambientBlue) { this.ambientBlue = ambientBlue; }
    public void setLightCount(int lightCount) { this.lightCount = lightCount; }

    public void clear() {
        ambientRed = 0;
        ambientGreen = 0;
        ambientBlue = 0;

        lightCount = 0;
        lights.clear();

        spheres.clear();
        boxes.clear();
        triangles.clear();
        quadrangles.clear();
    }
}
