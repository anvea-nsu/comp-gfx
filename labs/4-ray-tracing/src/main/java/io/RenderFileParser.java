package io;

import model.Camera;
import model.Point3D;
import model.RenderModel;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RenderFileParser {
    private final RenderModel renderModel;
    private final Camera camera;

    private FileReader fileReader;

    public RenderFileParser(String path, RenderModel renderModel, Camera camera) throws FileNotFoundException {
        this.renderModel = renderModel;
        this.camera = camera;

        fileReader = new FileReader(path);
    }

    public void parseRenderFile() {
        try {
            int[] backgroundColors = readBackgroundColors();
            double gamma = readGamma();
            int depth = readDepth();

            Point3D eye = readPoint3D("Eye");
            Point3D view = readPoint3D("View");
            Point3D up = readPoint3D("Up");

            double[] zParams = readDoubleParams("Z parameters", 2);
            double[] screenParams = readDoubleParams("Screen parameters", 2);

            renderModel.setBackgroundR(backgroundColors[0]);
            renderModel.setBackgroundG(backgroundColors[1]);
            renderModel.setBackgroundB(backgroundColors[2]);
            renderModel.setGamma(gamma);
            renderModel.setDepth(depth);

            camera.setEye(eye);
            camera.setView(view);
            camera.setUp(up);

            camera.setZn(zParams[0]);
            camera.setZf(zParams[1]);

            camera.setSw(screenParams[0]);
            camera.setSh(screenParams[1]);

            camera.recalculate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Invalid .render file:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            renderModel.clear();
            camera.clear();

        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    private int[] readBackgroundColors() throws IOException {
        int[] colors = fileReader.getLineNumbers();

        if (colors.length != 3) {
            throw new IOException("Background color line must contain 3 integer values");
        }

        for (int color : colors) {
            if (color < 0 || color > 255) {
                throw new IOException("Background color must be in range 0..255: " + color);
            }
        }

        return colors;
    }

    private double readGamma() throws IOException {
        double[] gamma = fileReader.getLineDoubles();

        if (gamma.length != 1) {
            throw new IOException("Gamma line must contain 1 value");
        }

        if (gamma[0] < 0 || gamma[0] > 10) {
            throw new IOException("Gamma must be in range 0..10: " + gamma[0]);
        }

        return gamma[0];
    }

    private int readDepth() throws IOException {
        int[] depth = fileReader.getLineNumbers();

        if (depth.length != 1) {
            throw new IOException("Depth line must contain 1 integer value");
        }

        if (depth[0] < 1 || depth[0] > 10) {
            throw new IOException("Depth must be in range 1..10: " + depth[0]);
        }

        return depth[0];
    }

    private Point3D readPoint3D(String name) throws IOException {
        double[] coords = fileReader.getLineDoubles();

        if (coords.length != 3) {
            throw new IOException(name + " line must contain 3 double values");
        }

        return new Point3D(coords[0], coords[1], coords[2]);
    }

    private double[] readDoubleParams(String name, int expectedLength) throws IOException {
        double[] params = fileReader.getLineDoubles();

        if (params.length != expectedLength) {
            throw new IOException(name + " line must contain " + expectedLength + " double values");
        }

        return params;
    }
}