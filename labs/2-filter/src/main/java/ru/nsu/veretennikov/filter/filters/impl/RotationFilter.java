package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class RotationFilter implements Filter {
    @Override public String getName() {
        return "Поворот";
    }

    @Override
    public List<FilterParameter> getParameters() {
        return List.of(
            new FilterParameter("angle", "Угол (градусы)", -180.0, 180.0, 0.0)
        );
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        double angleDeg = ((Number) params.getOrDefault("angle", 0.0)).doubleValue();
        double rad      = Math.toRadians(angleDeg);

        int sw = src.getWidth(), sh = src.getHeight();
        int[] srcPx = src.getRGB(0, 0, sw, sh, null, 0, sw);

        double cos = Math.abs(Math.cos(rad)), sin = Math.abs(Math.sin(rad));
        int dw = (int) Math.ceil(sw * cos + sh * sin);
        int dh = (int) Math.ceil(sw * sin + sh * cos);

        double cx = sw / 2.0, cy = sh / 2.0;
        double dcx = dw / 2.0, dcy = dh / 2.0;

        int[] dstPx = new int[dw * dh];
        int WHITE = 0xFFFFFFFF;

        double cosA = Math.cos(-rad), sinA = Math.sin(-rad);

        for (int y = 0; y < dh; y++) {
            for (int x = 0; x < dw; x++) {
                double tx = x - dcx, ty = y - dcy;
                double sx = cosA * tx - sinA * ty + cx;
                double sy = sinA * tx + cosA * ty + cy;

                if (sx < 0 || sy < 0 || sx >= sw - 1 || sy >= sh - 1) {
                    dstPx[y * dw + x] = WHITE;
                } else {
                    dstPx[y * dw + x] = 0xFF000000 | bilinear(srcPx, sw, sx, sy);
                }
            }
        }

        BufferedImage result = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, dw, dh, dstPx, 0, dw);
        return result;
    }

    private static int bilinear(int[] pixels, int w, double sx, double sy) {
        int x0 = (int) sx, y0 = (int) sy;
        int x1 = x0 + 1, y1 = y0 + 1;
        double fx = sx - x0, fy = sy - y0;

        int c00 = pixels[y0 * w + x0];
        int c10 = pixels[y0 * w + x1];
        int c01 = pixels[y1 * w + x0];
        int c11 = pixels[y1 * w + x1];

        int r = bilinearChannel(c00, c10, c01, c11, fx, fy, 16);
        int g = bilinearChannel(c00, c10, c01, c11, fx, fy,  8);
        int b = bilinearChannel(c00, c10, c01, c11, fx, fy,  0);
        return (r << 16) | (g << 8) | b;
    }

    private static int bilinearChannel(int c00, int c10, int c01, int c11,
                                       double fx, double fy, int shift) {
        double v0 = ((c00 >> shift) & 0xFF) * (1 - fx) + ((c10 >> shift) & 0xFF) * fx;
        double v1 = ((c01 >> shift) & 0xFF) * (1 - fx) + ((c11 >> shift) & 0xFF) * fx;
        return Math.min(255, Math.max(0, (int)(v0 * (1 - fy) + v1 * fy)));
    }
}
