package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class SobelFilter implements Filter {
    @Override public String getName() {
        return "Оператор Собеля";
    }

    @Override
    public List<FilterParameter> getParameters() {
        return List.of(
            new FilterParameter("threshold", "Порог бинаризации", 0, 255, 50)
        );
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        int threshold = ((Number) params.getOrDefault("threshold", 50)).intValue();
        int w = src.getWidth(), h = src.getHeight();
        int[] srcPx = src.getRGB(0, 0, w, h, null, 0, w);
        int[] dstPx = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int xm = Math.max(x - 1, 0), xp = Math.min(x + 1, w - 1);
                int ym = Math.max(y - 1, 0), yp = Math.min(y + 1, h - 1);

                int tl = lum(srcPx[ym * w + xm]);
                int tm = lum(srcPx[ym * w + x ]);
                int tr = lum(srcPx[ym * w + xp]);
                int ml = lum(srcPx[y  * w + xm]);
                int mr = lum(srcPx[y  * w + xp]);
                int bl = lum(srcPx[yp * w + xm]);
                int bm = lum(srcPx[yp * w + x ]);
                int br = lum(srcPx[yp * w + xp]);

                int gx = -tl + tr - 2 * ml + 2 * mr - bl + br;
                int gy = -tl - 2 * tm - tr + bl + 2 * bm + br;
                int g  = (int) Math.sqrt(gx * gx + gy * gy);

                int val = g >= threshold ? 0 : 255;
                dstPx[y * w + x] = 0xFF000000 | (val << 16) | (val << 8) | val;
            }
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, dstPx, 0, w);
        return result;
    }

    private static int lum(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8)  & 0xFF;
        int b =  rgb        & 0xFF;
        return (int)(0.299 * r + 0.587 * g + 0.114 * b);
    }
}
