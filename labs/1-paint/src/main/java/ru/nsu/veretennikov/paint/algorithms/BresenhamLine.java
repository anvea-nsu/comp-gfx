package ru.nsu.veretennikov.paint.algorithms;

import java.awt.Color;
import java.awt.image.BufferedImage;

public final class BresenhamLine {
    public static void drawLine(BufferedImage img,
                                int x1, int y1, int x2, int y2,
                                Color color) {
        final int rgb = color.getRGB();
        final int w   = img.getWidth();
        final int h   = img.getHeight();

        int dx  = Math.abs(x2 - x1);
        int dy  = Math.abs(y2 - y1);
        int sx  = (x1 < x2) ? 1 : -1;
        int sy  = (y1 < y2) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (x1 >= 0 && x1 < w && y1 >= 0 && y1 < h) {
                img.setRGB(x1, y1, rgb);
            }
            if (x1 == x2 && y1 == y2) break;
            int e2 = err << 1;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 <  dx) { err += dx; y1 += sy; }
        }
    }
}
