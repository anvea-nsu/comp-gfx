package ecg.wireframe.io;

import ecg.wireframe.model.Scene;
import ecg.wireframe.model.SceneParameters;
import ecg.wireframe.model.math.Point2D;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple key=value text format:
 *
 * N=20
 * M=18
 * M1=4
 * zn=3.0
 * zf=100.0
 * angleX=20.0
 * angleY=-30.0
 * angleZ=0.0
 * points=7
 * 0.0 0.0
 * 0.2 0.3
 * ...
 */
public class SceneFileManager {

    public static class InvalidFileException extends Exception {
        public InvalidFileException(String msg) { super(msg); }
    }

    public static void save(Scene scene, File file) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
            SceneParameters p = scene.getParams();
            w.println("N=" + p.N);
            w.println("M=" + p.M);
            w.println("M1=" + p.M1);
            w.println("zn=" + p.zn);
            w.println("zf=" + p.zf);
            w.println("angleX=" + p.angleX);
            w.println("angleY=" + p.angleY);
            w.println("angleZ=" + p.angleZ);

            List<Point2D> pts = scene.getControlPoints();
            w.println("points=" + pts.size());
            for (Point2D pt : pts) {
                w.println(pt.u + " " + pt.v);
            }
        }
    }

    public static void load(File file, Scene scene) throws IOException, InvalidFileException {
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            SceneParameters p = new SceneParameters();
            List<Point2D> pts = new ArrayList<>();
            int pointCount = -1;
            String line;

            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("points=")) {
                    pointCount = Integer.parseInt(line.substring(7));
                    for (int i = 0; i < pointCount; i++) {
                        String pline = r.readLine();
                        if (pline == null) throw new InvalidFileException("Unexpected end of file reading points.");
                        String[] parts = pline.trim().split("\\s+");
                        if (parts.length < 2) throw new InvalidFileException("Invalid point line: " + pline);
                        pts.add(new Point2D(Double.parseDouble(parts[0]), Double.parseDouble(parts[1])));
                    }
                } else if (line.contains("=")) {
                    String[] kv = line.split("=", 2);
                    String key = kv[0].trim(), val = kv[1].trim();
                    switch (key) {
                        case "N":      p.N      = Integer.parseInt(val); break;
                        case "M":      p.M      = Integer.parseInt(val); break;
                        case "M1":     p.M1     = Integer.parseInt(val); break;
                        case "zn":     p.zn     = Double.parseDouble(val); break;
                        case "zf":     p.zf     = Double.parseDouble(val); break;
                        case "angleX": p.angleX = Double.parseDouble(val); break;
                        case "angleY": p.angleY = Double.parseDouble(val); break;
                        case "angleZ": p.angleZ = Double.parseDouble(val); break;
                    }
                }
            }

            // Validate
            if (pts.size() < 4)
                throw new InvalidFileException("At least 4 control points required, found: " + pts.size());
            if (p.N < 1)  throw new InvalidFileException("N must be >= 1");
            if (p.M < 2)  throw new InvalidFileException("M must be >= 2");
            if (p.M1 < 1) throw new InvalidFileException("M1 must be >= 1");

            scene.setParams(p);
            scene.setControlPoints(pts);

        } catch (NumberFormatException e) {
            throw new InvalidFileException("Invalid number format: " + e.getMessage());
        }
    }
}
