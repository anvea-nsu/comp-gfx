package ru.nsu.veretennikov.paint.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class AppState {
    public enum ToolType { LINE, STAMP, FILL }
    public enum ShapeType { POLYGON, STAR }

    public interface ToolChangeListener {
        void onToolChanged(ToolType tool);
    }

    public interface ColorChangeListener {
        void onColorChanged(Color color);
    }

    private static final AppState INSTANCE = new AppState();

    public static AppState getInstance() { return INSTANCE; }

    private AppState() {}

    private ToolType currentTool   = ToolType.LINE;
    private Color    currentColor  = Color.BLACK;

    private int lineThickness = 1;

    private ShapeType shapeType    = ShapeType.POLYGON;
    private int stampVertices      = 5;
    private int stampRadius        = 50;
    private int stampRotation      = 0;

    private final List<ToolChangeListener>  toolListeners  = new ArrayList<>();
    private final List<ColorChangeListener> colorListeners = new ArrayList<>();

    public void addToolChangeListener(ToolChangeListener l)  { toolListeners.add(l); }
    public void addColorChangeListener(ColorChangeListener l) { colorListeners.add(l); }

    public ToolType getCurrentTool() { return currentTool; }
    public void setCurrentTool(ToolType t) {
        currentTool = t;
        toolListeners.forEach(l -> l.onToolChanged(t));
    }

    public Color getCurrentColor() { return currentColor; }
    public void setCurrentColor(Color c) {
        currentColor = c;
        colorListeners.forEach(l -> l.onColorChanged(c));
    }

    public int getLineThickness()          { return lineThickness; }
    public void setLineThickness(int t)    { lineThickness = t; }

    public ShapeType getShapeType()        { return shapeType; }
    public void setShapeType(ShapeType s)  { shapeType = s; }

    public int getStampVertices()          { return stampVertices; }
    public void setStampVertices(int v)    { stampVertices = v; }

    public int getStampRadius()            { return stampRadius; }
    public void setStampRadius(int r)      { stampRadius = r; }

    public int getStampRotation()          { return stampRotation; }
    public void setStampRotation(int r)    { stampRotation = r; }
}
