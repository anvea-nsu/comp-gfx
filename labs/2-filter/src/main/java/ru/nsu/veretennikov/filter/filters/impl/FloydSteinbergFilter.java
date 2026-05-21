package ru.nsu.veretennikov.filter.filters.impl;

import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FloydSteinbergFilter implements Filter {
    @Override public String getName() {
        return "Дизеринг Флойда-Стейнберга";
    }

    @Override
    public List<FilterParameter> getParameters() {
        return Arrays.asList(
            new FilterParameter("levelsR", "Уровней (R)", 2, 128, 3),
            new FilterParameter("levelsG", "Уровней (G)", 2, 128, 3),
            new FilterParameter("levelsB", "Уровней (B)", 2, 128, 3)
        );
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        int levelsR = ((Number) params.getOrDefault("levelsR", 3)).intValue();
        int levelsG = ((Number) params.getOrDefault("levelsG", 3)).intValue();
        int levelsB = ((Number) params.getOrDefault("levelsB", 3)).intValue();

        int w = src.getWidth(), h = src.getHeight();

        float[] fr = new float[w * h];
        float[] fg = new float[w * h];
        float[] fb = new float[w * h];

        int[] srcPx = src.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < srcPx.length; i++) {
            fr[i] = (srcPx[i] >> 16) & 0xFF;
            fg[i] = (srcPx[i] >> 8)  & 0xFF;
            fb[i] =  srcPx[i]        & 0xFF;
        }

        int[] dstPx = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;

                float r = Math.min(255, Math.max(0, fr[idx]));
                float g = Math.min(255, Math.max(0, fg[idx]));
                float b = Math.min(255, Math.max(0, fb[idx]));

                int qr = quantise(r, levelsR);
                int qg = quantise(g, levelsG);
                int qb = quantise(b, levelsB);

                dstPx[idx] = 0xFF000000 | (qr << 16) | (qg << 8) | qb;

                float er = r - qr, eg = g - qg, eb = b - qb;

                spreadError(fr, fg, fb, x + 1, y,     w, h, er * 7/16f, eg * 7/16f, eb * 7/16f);
                spreadError(fr, fg, fb, x - 1, y + 1, w, h, er * 3/16f, eg * 3/16f, eb * 3/16f);
                spreadError(fr, fg, fb, x,     y + 1, w, h, er * 5/16f, eg * 5/16f, eb * 5/16f);
                spreadError(fr, fg, fb, x + 1, y + 1, w, h, er * 1/16f, eg * 1/16f, eb * 1/16f);
            }
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, w, h, dstPx, 0, w);
        return result;
    }

    private static int quantise(float v, int levels) {
        if (levels == 1) return 0;
        int q = Math.round(v / 255.0f * (levels - 1));
        q = Math.min(levels - 1, Math.max(0, q));
        return q * 255 / (levels - 1);
    }

    private static void spreadError(float[] fr, float[] fg, float[] fb,
                                    int x, int y, int w, int h,
                                    float er, float eg, float eb) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            int i = y * w + x;
            fr[i] += er;
            fg[i] += eg;
            fb[i] += eb;
        }
    }
}
