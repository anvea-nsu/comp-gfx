package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GrayscaleFilter implements Filter {
    @Override public String getName() {
        return "Оттенки серого";
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
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8)  & 0xFF;
            int b =  rgb        & 0xFF;
            int grey = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            pixels[i] = 0xFF000000 | (grey << 16) | (grey << 8) | grey;
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, pixels, 0, w);
        return result;
    }
}
