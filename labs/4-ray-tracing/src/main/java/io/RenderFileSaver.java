package io;

import model.Camera;
import model.Point3D;
import model.RenderModel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class RenderFileSaver {
    private final String path;
    private final RenderModel renderModel;
    private final Camera camera;

    public RenderFileSaver(String path, RenderModel renderModel, Camera camera) {
        this.path = path;
        this.renderModel = renderModel;
        this.camera = camera;
    }

    public void save() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writeBackground(writer);
            writeGamma(writer);
            writeDepth(writer);
            writeCamera(writer);
        }
    }

    private void writeBackground(BufferedWriter writer) throws IOException {
        writer.write(renderModel.getBackgroundR() + " " + renderModel.getBackgroundG() + " " + renderModel.getBackgroundB());
        writer.newLine();
    }

    private void writeGamma(BufferedWriter writer) throws IOException {
        writer.write(String.valueOf(renderModel.getGamma()));
        writer.newLine();
    }

    private void writeDepth(BufferedWriter writer) throws IOException {
        writer.write(String.valueOf((int) renderModel.getDepth()));
        writer.newLine();
    }

    private void writeCamera(BufferedWriter writer) throws IOException {
        writePoint(writer, camera.getEye());
        writePoint(writer, camera.getView());
        writePoint(writer, camera.getUp());

        writer.write(camera.getZn() + " " + camera.getZf());
        writer.newLine();

        writer.write(camera.getSw() + " " + camera.getSh());
        writer.newLine();
    }

    private void writePoint(BufferedWriter writer, Point3D point) throws IOException {
        if (point == null) {
            throw new IOException("Camera point is null");
        }

        writer.write(point.getX() + " " + point.getY() + " " + point.getZ());
        writer.newLine();
    }
}