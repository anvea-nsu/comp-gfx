package io;

import model.*;
import model.Box;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.*;

public class SceneFileParser {

    // Figure type constants (replaces FigureType enum)
    private static final String FIGURE_SPHERE     = "SPHERE";
    private static final String FIGURE_BOX        = "BOX";
    private static final String FIGURE_TRIANGLE   = "TRIANGLE";
    private static final String FIGURE_QUADRANGLE = "QUADRANGLE";

    private static final Map<String, Integer> POINTS_MAP;
    static {
        POINTS_MAP = new HashMap<>();
        POINTS_MAP.put(FIGURE_SPHERE,     1);
        POINTS_MAP.put(FIGURE_BOX,        2);
        POINTS_MAP.put(FIGURE_TRIANGLE,   3);
        POINTS_MAP.put(FIGURE_QUADRANGLE, 4);
    }

    private FileReader fileReader;
    private final SceneModel sceneModel;

    public SceneFileParser(String path, SceneModel sceneModel) {
        this.sceneModel = sceneModel;

        try {
            fileReader = new FileReader(path);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "File not found: " + path, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parseSceneFileHeader() {
        try {
            int[] ambients = fileReader.getLineNumbers();
            if (ambients.length != 3) {
                JOptionPane.showMessageDialog(null, "Invalid number of ambients: " + ambients.length, "Error", JOptionPane.ERROR_MESSAGE);
                sceneModel.clear();
                return;
            }

            for (int ambient : ambients) {
                if (ambient < 0 || ambient > 255) {
                    JOptionPane.showMessageDialog(null, "Invalid ambient: " + ambient, "Error", JOptionPane.ERROR_MESSAGE);
                    sceneModel.clear();
                    return;
                }
            }

            sceneModel.setAmbientRed(ambients[0]);
            sceneModel.setAmbientGreen(ambients[1]);
            sceneModel.setAmbientBlue(ambients[2]);

            int[] numLights = fileReader.getLineNumbers();

            if (numLights.length != 1) {
                JOptionPane.showMessageDialog(null, "Invalid number of lights: " + numLights.length, "Error", JOptionPane.ERROR_MESSAGE);
                sceneModel.clear();
                return;
            }

            sceneModel.setLightCount(numLights[0]);

            for (int i = 0; i < numLights[0]; i++) {
                int[] lightParams = fileReader.getLineNumbers();

                if (lightParams.length != 6) {
                    JOptionPane.showMessageDialog(null, "Invalid number of lights: " + lightParams.length, "Error", JOptionPane.ERROR_MESSAGE);
                    sceneModel.clear();
                    return;
                }

                for (int j = 3; j < 6; j++) {
                    if (lightParams[j] < 0 || lightParams[j] > 255) {
                        JOptionPane.showMessageDialog(null, "Invalid light color: " + lightParams[j], "Error", JOptionPane.ERROR_MESSAGE);
                        sceneModel.clear();
                        return;
                    }
                }

                Light light = new Light(lightParams[0], lightParams[1], lightParams[2], lightParams[3], lightParams[4], lightParams[5]);
                sceneModel.getLights().add(light);
            }

            parseSceneFileSection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading scene file header: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void parseSceneFileSection() {
        try {
            String[] figureType = fileReader.getLineTokens();

            while (figureType.length != 0) {
                if (figureType.length != 1) {
                    JOptionPane.showMessageDialog(null, "Invalid figure data: " + Arrays.toString(figureType), "Error", JOptionPane.ERROR_MESSAGE);
                    sceneModel.clear();
                    return;
                }

                switch (figureType[0]) {
                    case FIGURE_SPHERE     -> parseSphere();
                    case FIGURE_BOX        -> parseBox();
                    case FIGURE_TRIANGLE   -> parseTriangle();
                    case FIGURE_QUADRANGLE -> parseQuadrangle();
                    default -> {
                        JOptionPane.showMessageDialog(null, "Unknown figure type: " + figureType[0], "Error", JOptionPane.ERROR_MESSAGE);
                        sceneModel.clear();
                        return;
                    }
                }

                figureType = fileReader.getLineTokens();
            }

        } catch (Exception ignored) {
        }
    }

    public List<Point3D> parsePoints(int countPoints) {
        List<Point3D> points = new ArrayList<>();

        for (int i = 0; i < countPoints; i++) {
            try {
                double[] coords = fileReader.getLineDoubles();
                if (coords.length != 3) {
                    JOptionPane.showMessageDialog(null, "Invalid coords: " + coords.length, "Error", JOptionPane.ERROR_MESSAGE);
                    sceneModel.clear();
                    return null;
                }

                points.add(new Point3D(coords[0], coords[1], coords[2]));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error reading points: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        return points;
    }

    public double parseRadius() {
        try {
            double[] r = fileReader.getLineDoubles();

            if (r.length != 1) {
                JOptionPane.showMessageDialog(null, "Invalid radius: " + r.length, "Error", JOptionPane.ERROR_MESSAGE);
                return -1;
            }

            if (r[0] <= 0) {
                JOptionPane.showMessageDialog(null, "Invalid radius: " + r[0], "Error", JOptionPane.ERROR_MESSAGE);
                return -1;
            }

            return r[0];
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading radius: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    public OpticalChar parseOpticalChar() {
        try {
            double[] optChar = fileReader.getLineDoubles();

            if (optChar.length != 7) {
                JOptionPane.showMessageDialog(null, "Invalid optical chars: " + optChar.length, "Error", JOptionPane.ERROR_MESSAGE);
                sceneModel.clear();
                return null;
            }

            return new OpticalChar(optChar[0], optChar[1], optChar[2], optChar[3], optChar[4], optChar[5], optChar[6]);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading optical chars: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public void parseSphere() {
        List<Point3D> points = parsePoints(1);
        if (points == null) return;

        double radius = parseRadius();
        if (radius == -1) return;

        OpticalChar opticalChar = parseOpticalChar();
        if (opticalChar == null) return;

        sceneModel.getSpheres().add(new Sphere(points, radius, opticalChar));
    }

    public void parseBox() {
        List<Point3D> points = parsePoints(2);
        if (points == null) return;

        OpticalChar opticalChar = parseOpticalChar();
        if (opticalChar == null) return;

        sceneModel.getBoxes().add(new Box(points, opticalChar));
    }

    public void parseTriangle() {
        List<Point3D> points = parsePoints(3);
        if (points == null) return;

        OpticalChar opticalChar = parseOpticalChar();
        if (opticalChar == null) return;

        sceneModel.getTriangles().add(new Triangle(points, opticalChar));
    }

    public void parseQuadrangle() {
        List<Point3D> points = parsePoints(4);
        if (points == null) return;

        OpticalChar opticalChar = parseOpticalChar();
        if (opticalChar == null) return;

        sceneModel.getQuadrangles().add(new Quadrangle(points, opticalChar));
    }
}
