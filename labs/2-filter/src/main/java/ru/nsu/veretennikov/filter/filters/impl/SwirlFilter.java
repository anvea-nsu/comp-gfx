package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SwirlFilter implements Filter {
    @Override public String getName() {
        return "Закручивание (Swirl)";
    }

    @Override
    public List<FilterParameter> getParameters() {
        return Arrays.asList(
            new FilterParameter("strength", "Сила закручивания", -10.0, 10.0, 3.0),
            new FilterParameter("radius",   "Радиус (пиксели)",  10,    1500,  300)
        );
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        double strength = ((Number) params.getOrDefault("strength", 3.0)).doubleValue();
        int    radius   = ((Number) params.getOrDefault("radius",   300)).intValue();

        int w = src.getWidth(), h = src.getHeight();
        int[] srcPx = src.getRGB(0, 0, w, h, null, 0, w);
        int[] dstPx = new int[w * h];

        double cx = w / 2.0, cy = h / 2.0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dx = x - cx, dy = y - cy;
                double r  = Math.sqrt(dx * dx + dy * dy);

                if (r >= radius) {
                    dstPx[y * w + x] = srcPx[y * w + x];
                } else {
                    double theta = Math.atan2(dy, dx);
                    double swirlAngle = strength * Math.PI * (1.0 - r / radius);
                    double newTheta   = theta + swirlAngle;

                    double sx = cx + r * Math.cos(newTheta);
                    double sy = cy + r * Math.sin(newTheta);

                    sx = Math.min(w - 1.001, Math.max(0, sx));
                    sy = Math.min(h - 1.001, Math.max(0, sy));

                    dstPx[y * w + x] = 0xFF000000 | bilinear(srcPx, w, sx, sy);
                }
            }
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, dstPx, 0, w);
        return result;
    }

    private static int bilinear(int[] pixels, int w, double sx, double sy) {
        int x0 = (int) sx, y0 = (int) sy;
        int x1 = x0 + 1, y1 = y0 + 1;
        int len = pixels.length;
        int h   = len / w;
        x1 = Math.min(x1, w - 1);
        y1 = Math.min(y1, h - 1);

        double fx = sx - x0, fy = sy - y0;

        int c00 = pixels[y0 * w + x0], c10 = pixels[y0 * w + x1];
        int c01 = pixels[y1 * w + x0], c11 = pixels[y1 * w + x1];

        int r = interpCh(c00, c10, c01, c11, fx, fy, 16);
        int g = interpCh(c00, c10, c01, c11, fx, fy,  8);
        int b = interpCh(c00, c10, c01, c11, fx, fy,  0);
        return (r << 16) | (g << 8) | b;
    }

    private static int interpCh(int c00, int c10, int c01, int c11,
                                 double fx, double fy, int sh) {
        double v0 = ((c00>>sh)&0xFF)*(1-fx) + ((c10>>sh)&0xFF)*fx;
        double v1 = ((c01>>sh)&0xFF)*(1-fx) + ((c11>>sh)&0xFF)*fx;
        return Math.min(255, Math.max(0, (int)(v0*(1-fy)+v1*fy)));
    }
}
