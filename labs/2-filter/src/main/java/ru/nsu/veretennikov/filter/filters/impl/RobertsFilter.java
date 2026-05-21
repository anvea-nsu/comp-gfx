package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class RobertsFilter implements Filter {
    @Override public String getName() {
        return "Оператор Робертса";
    }

    @Override
    public List<FilterParameter> getParameters() {
        return List.of(
            new FilterParameter("threshold", "Порог бинаризации", 0, 255, 30)
        );
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        int threshold = ((Number) params.getOrDefault("threshold", 30)).intValue();
        int w = src.getWidth(), h = src.getHeight();
        int[] srcPx = src.getRGB(0, 0, w, h, null, 0, w);
        int[] dstPx = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int y1 = Math.min(y + 1, h - 1);
                int x1 = Math.min(x + 1, w - 1);

                int lum00 = lum(srcPx[y  * w + x ]);
                int lum11 = lum(srcPx[y1 * w + x1]);
                int lum10 = lum(srcPx[y1 * w + x ]);
                int lum01 = lum(srcPx[y  * w + x1]);

                int gx = Math.abs(lum00 - lum11);
                int gy = Math.abs(lum01 - lum10);
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
