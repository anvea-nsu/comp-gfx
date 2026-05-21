package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NegativeFilter implements Filter {
    @Override public String getName() {
        return "Негатив";
    }

    @Override public List<FilterParameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = src.getRGB(0, 0, w, h, null, 0, w);

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];
            int r = 255 - ((rgb >> 16) & 0xFF);
            int g = 255 - ((rgb >> 8)  & 0xFF);
            int b = 255 - ( rgb        & 0xFF);
            pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, pixels, 0, w);
        return result;
    }
}
