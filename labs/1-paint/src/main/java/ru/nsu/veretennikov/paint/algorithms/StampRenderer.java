package ru.nsu.veretennikov.paint.algorithms;

import ru.nsu.veretennikov.paint.model.AppState.ShapeType;

import java.awt.image.BufferedImage;

public final class StampRenderer {
    public static void drawStamp(BufferedImage img,
                                 int cx, int cy,
                                 int vertices, int radius, int rotationDeg,
                                 ShapeType shapeType,
                                 java.awt.Color color) {
        double rot = Math.toRadians(rotationDeg);

        int[] px, py;

        if (shapeType == ShapeType.POLYGON) {
            px = new int[vertices];
            py = new int[vertices];
            for (int i = 0; i < vertices; i++) {
                double angle = 2.0 * Math.PI * i / vertices + rot;
                px[i] = cx + (int) Math.round(radius * Math.cos(angle));
                py[i] = cy + (int) Math.round(radius * Math.sin(angle));
            }
        } else {
            int pts = vertices * 2;
            px = new int[pts];
            py = new int[pts];
            double innerRadius = radius * 0.45;

            for (int i = 0; i < pts; i++) {
                double angle  = Math.PI * i / vertices + rot;
                double r      = (i % 2 == 0) ? radius : innerRadius;
                px[i] = cx + (int) Math.round(r * Math.cos(angle));
                py[i] = cy + (int) Math.round(r * Math.sin(angle));
            }
        }

        int n = px.length;
        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            BresenhamLine.drawLine(img, px[i], py[i], px[next], py[next], color);
        }
    }
}
