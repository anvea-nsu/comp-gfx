package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WatercolorFilter implements Filter {
    @Override public String getName() {
        return "Акварелизация";
    }

    @Override
    public List<FilterParameter> getParameters() {
        return List.of(
                new FilterParameter("radius", "Радиус сглаживания", 1, 5, 2)
        );
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        int radius = ((Number) params.getOrDefault("radius", 2)).intValue();

        int w = src.getWidth(), h = src.getHeight();
        int[] srcPx = src.getRGB(0, 0, w, h, null, 0, w);
        int[] dstPx = new int[w * h];

        int windowSize = (2 * radius + 1) * (2 * radius + 1);
        List<Integer> reds   = new ArrayList<>(windowSize);
        List<Integer> greens = new ArrayList<>(windowSize);
        List<Integer> blues  = new ArrayList<>(windowSize);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                reds.clear();
                greens.clear();
                blues.clear();

                for (int dy = -radius; dy <= radius; dy++) {
                    int ny = Math.min(Math.max(y + dy, 0), h - 1);
                    for (int dx = -radius; dx <= radius; dx++) {
                        int nx = Math.min(Math.max(x + dx, 0), w - 1);
                        int rgb = srcPx[ny * w + nx];
                        reds.add((rgb >> 16) & 0xFF);
                        greens.add((rgb >> 8) & 0xFF);
                        blues.add(rgb & 0xFF);
                    }
                }

                Collections.sort(reds);
                Collections.sort(greens);
                Collections.sort(blues);

                int mid = reds.size() / 2;
                int r = reds.get(mid);
                int g = greens.get(mid);
                int b = blues.get(mid);

                dstPx[y * w + x] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }

        BufferedImage median = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        median.setRGB(0, 0, w, h, dstPx, 0, w);

        return new SharpenFilter().apply(median, Map.of());
    }
}