package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.ConvolutionFilter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GaussianBlurFilter extends ConvolutionFilter {
    @Override public String getName() { return "Сглаживание (Гаусс)"; }

    @Override
    public List<FilterParameter> getParameters() {
        return List.of(
            new FilterParameter("size", "Размер окна",
                Arrays.asList("3×3", "5×5", "7×7", "9×9", "11×11"), 0)
        );
    }

    @Override
    protected float[][] getKernel(Map<String, Object> params) {
        String choice = (String) params.getOrDefault("size", "3×3");
        int size;
        switch (choice) {
            case "5×5":  size = 5;  break;
            case "7×7":  size = 7;  break;
            case "9×9":  size = 9;  break;
            case "11×11": size = 11; break;
            default:      size = 3;  break;
        }
        return buildGaussianKernel(size);
    }

    private float[][] buildGaussianKernel(int size) {
        float[][] kernel = new float[size][size];
        int half = size / 2;
        double sigma = size / 3.0;
        double sigma2 = 2.0 * sigma * sigma;
        double sum = 0;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - half, dy = y - half;
                kernel[y][x] = (float) Math.exp(-(dx * dx + dy * dy) / sigma2);
                sum += kernel[y][x];
            }
        }
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                kernel[y][x] /= (float) sum;

        return kernel;
    }
}
