package ru.nsu.veretennikov.filter.filters;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class ConvolutionFilter implements Filter {
    @Override
    public List<FilterParameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public BufferedImage apply(BufferedImage src, Map<String, Object> params) {
        float[][] kernel = getKernel(params);
        return convolve(src, kernel, getBias());
    }

    protected abstract float[][] getKernel(Map<String, Object> params);

    protected int getBias() { return 0; }

    protected BufferedImage convolve(BufferedImage src, float[][] kernel, int bias) {
        int width  = src.getWidth();
        int height = src.getHeight();
        int kSize  = kernel.length;
        int kHalf  = kSize / 2;

        int[] srcPixels = src.getRGB(0, 0, width, height, null, 0, width);
        int[] dstPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0;

                for (int ky = 0; ky < kSize; ky++) {
                    int sy = clamp(y + ky - kHalf, 0, height - 1);
                    for (int kx = 0; kx < kSize; kx++) {
                        int sx  = clamp(x + kx - kHalf, 0, width - 1);
                        int rgb = srcPixels[sy * width + sx];
                        float w = kernel[ky][kx];
                        r += ((rgb >> 16) & 0xFF) * w;
                        g += ((rgb >> 8)  & 0xFF) * w;
                        b += ( rgb        & 0xFF) * w;
                    }
                }

                int ri = clamp((int)(r + bias), 0, 255);
                int gi = clamp((int)(g + bias), 0, 255);
                int bi = clamp((int)(b + bias), 0, 255);
                dstPixels[y * width + x] = 0xFF000000 | (ri << 16) | (gi << 8) | bi;
            }
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, width, height, dstPixels, 0, width);
        return result;
    }

    protected static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : Math.min(v, hi);
    }
}
