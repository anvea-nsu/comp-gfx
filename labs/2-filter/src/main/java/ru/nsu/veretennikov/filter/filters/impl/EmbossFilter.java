package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.ConvolutionFilter;

import java.awt.image.BufferedImage;
import java.util.Map;

public class EmbossFilter extends ConvolutionFilter {
    private static final float[][] KERNEL = {
        {-1, -1, 0},
        {-1,  0, 1},
        { 0,  1, 1}
    };

    @Override public String getName() {
        return "Тиснение";
    }

    @Override protected float[][] getKernel(Map<String, Object> params) {
        return KERNEL;
    }

    @Override protected int getBias() {
        return 128;
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        // сначала обесцвечиваем
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[] px = src.getRGB(0, 0, src.getWidth(), src.getHeight(), null, 0, src.getWidth());
        for (int i = 0; i < px.length; i++) {
            int r = (px[i] >> 16) & 0xFF;
            int g = (px[i] >> 8)  & 0xFF;
            int b =  px[i]        & 0xFF;
            int lum = (int)(0.299*r + 0.587*g + 0.114*b);
            px[i] = 0xFF000000 | (lum << 16) | (lum << 8) | lum;
        }
        gray.setRGB(0, 0, src.getWidth(), src.getHeight(), px, 0, src.getWidth());

        // потом применяем свёртку
        return super.apply(gray, params);
    }
}
