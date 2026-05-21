package ru.nsu.veretennikov.filter.filters;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public interface Filter {
    String getName();
    List<FilterParameter> getParameters();

    BufferedImage apply(BufferedImage src, Map<String, Object> params);
}
