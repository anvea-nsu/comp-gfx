package ru.nsu.veretennikov.filter.filters;

import java.util.List;

public class FilterParameter {
    public enum Type { INT, DOUBLE, CHOICE }

    private final String key;
    private final String label;
    private final Type type;
    private final double min;
    private final double max;
    private final double defaultValue;
    private final List<String> choices;

    public FilterParameter(String key, String label, int min, int max, int defaultValue) {
        this.key = key;
        this.label = label;
        this.type = Type.INT;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.choices = null;
    }

    public FilterParameter(String key, String label, double min, double max, double defaultValue) {
        this.key = key;
        this.label = label;
        this.type = Type.DOUBLE;
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.choices = null;
    }

    public FilterParameter(String key, String label, List<String> choices, int defaultIndex) {
        this.key = key;
        this.label = label;
        this.type = Type.CHOICE;
        this.min = 0;
        this.max = choices.size() - 1;
        this.defaultValue = defaultIndex;
        this.choices = choices;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public Type getType() { return type; }
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getDefaultValue() { return defaultValue; }
    public List<String> getChoices() { return choices; }

    public Object getTypedDefault() {
        switch (type) {
            case INT:    return (int) defaultValue;
            case DOUBLE: return defaultValue;
            case CHOICE: return choices.get((int) defaultValue);
            default:     return defaultValue;
        }
    }
}
