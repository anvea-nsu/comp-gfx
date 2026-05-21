package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class GammaFilter implements Filter {
    @Override public String getName() {
        return "Гамма-коррекция";
    }

    @Override
    public List<FilterParameter> getParameters() {
        return List.of(new FilterParameter("gamma", "Гамма (γ)", 0.1, 10.0, 1.0));
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        double gamma = ((Number) params.getOrDefault("gamma", 1.0)).doubleValue();

        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = (int) Math.round(255.0 * Math.pow(i / 255.0, gamma));
            lut[i] = Math.min(255, Math.max(0, lut[i]));
        }

        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = src.getRGB(0, 0, w, h, null, 0, w);

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];
            int r = lut[(rgb >> 16) & 0xFF];
            int g = lut[(rgb >> 8)  & 0xFF];
            int b = lut[ rgb        & 0xFF];
            pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, pixels, 0, w);
        return result;
    }
}
