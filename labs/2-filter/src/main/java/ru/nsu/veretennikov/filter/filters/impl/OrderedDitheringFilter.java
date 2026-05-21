package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OrderedDitheringFilter implements Filter {

    @Override public String getName() { return "Упорядоченный дизеринг"; }

    @Override
    public List<FilterParameter> getParameters() {
        return Arrays.asList(
                new FilterParameter("levelsR", "Уровней (R)", 2, 128, 2),
                new FilterParameter("levelsG", "Уровней (G)", 2, 128, 2),
                new FilterParameter("levelsB", "Уровней (B)", 2, 128, 2)
        );
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        int levelsR = ((Number) params.getOrDefault("levelsR", 2)).intValue();
        int levelsG = ((Number) params.getOrDefault("levelsG", 2)).intValue();
        int levelsB = ((Number) params.getOrDefault("levelsB", 2)).intValue();

        int nR = matrixSize(levelsR);
        int nG = matrixSize(levelsG);
        int nB = matrixSize(levelsB);
        int[][] bayerR = generateBayer(nR);
        int[][] bayerG = generateBayer(nG);
        int[][] bayerB = generateBayer(nB);

        int w = src.getWidth(), h = src.getHeight();
        int[] srcPx = src.getRGB(0, 0, w, h, null, 0, w);
        int[] dstPx = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double tR = bayerR[y % nR][x % nR] / (double)(nR * nR);
                double tG = bayerG[y % nG][x % nG] / (double)(nG * nG);
                double tB = bayerB[y % nB][x % nB] / (double)(nB * nB);

                int rgb = srcPx[y * w + x];
                int r = ditherChannel((rgb >> 16) & 0xFF, levelsR, tR);
                int g = ditherChannel((rgb >> 8)  & 0xFF, levelsG, tG);
                int b = ditherChannel( rgb        & 0xFF, levelsB, tB);

                dstPx[y * w + x] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, dstPx, 0, w);
        return result;
    }

    private static int matrixSize(int levels) {
        double needed = 256.0 / levels;
        int n = 1;
        while ((long) n * n < (long) Math.ceil(needed)) {
            n *= 2;
        }
        return n;
    }

    private static int[][] generateBayer(int n) {
        if (n == 1) return new int[][]{{0}};

        int half = n / 2;
        int[][] small = generateBayer(half);
        int[][] result = new int[n][n];

        for (int y = 0; y < half; y++) {
            for (int x = 0; x < half; x++) {
                int v = small[y][x] * 4;
                result[y]        [x]        = v;
                result[y]        [x + half] = v + 2;
                result[y + half] [x]        = v + 3;
                result[y + half] [x + half] = v + 1;
            }
        }
        return result;
    }

    private static int ditherChannel(int value, int levels, double threshold) {
        if (levels <= 1) return 0;
        double v = value / 255.0;
        int q = (int)(v * (levels - 1) + threshold);
        q = Math.min(levels - 1, Math.max(0, q));
        return q * 255 / (levels - 1);
    }
}