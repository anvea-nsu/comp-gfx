package ru.nsu.veretennikov.paint.algorithms;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

public final class SpanFill {
    public static void fill(BufferedImage img, int x, int y, Color fillColor) {
        final int width  = img.getWidth();
        final int height = img.getHeight();

        if (x < 0 || x >= width || y < 0 || y >= height) return;

        final int targetRGB = img.getRGB(x, y);
        final int fillRGB   = fillColor.getRGB();

        if (targetRGB == fillRGB) return;

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{x, y});

        while (!stack.isEmpty()) {
            int[] seed  = stack.pop();
            int   sx    = seed[0];
            int   sy    = seed[1];

            if (sy < 0 || sy >= height) continue;
            if (img.getRGB(sx, sy) != targetRGB) continue;

            int left = sx;
            while (left > 0 && img.getRGB(left - 1, sy) == targetRGB) left--;

            int right = sx;
            while (right < width - 1 && img.getRGB(right + 1, sy) == targetRGB) right++;

            for (int i = left; i <= right; i++) {
                img.setRGB(i, sy, fillRGB);
            }

            pushSpanSeeds(img, left, right, sy - 1, targetRGB, stack, width, height);
            pushSpanSeeds(img, left, right, sy + 1, targetRGB, stack, width, height);
        }
    }

    private static void pushSpanSeeds(BufferedImage img,
                                      int left, int right, int y,
                                      int targetRGB,
                                      Deque<int[]> stack,
                                      int width, int height) {
        if (y < 0 || y >= height) return;
        boolean inRun = false;
        for (int i = left; i <= right; i++) {
            if (img.getRGB(i, y) == targetRGB) {
                if (!inRun) {
                    stack.push(new int[]{i, y});
                    inRun = true;
                }
            } else {
                inRun = false;
            }
        }
    }
}
