package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.ConvolutionFilter;

import java.util.Map;

public class SharpenFilter extends ConvolutionFilter {
    private static final float[][] KERNEL = {
        { 0, -1,  0},
        {-1,  5, -1},
        { 0, -1,  0}
    };

    @Override public String getName() {
        return "Повышение резкости";
    }

    @Override
    protected float[][] getKernel(Map<String, Object> params) {
        return KERNEL;
    }
}
