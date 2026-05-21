package ru.nsu.veretennikov.paint.tools;

import ru.nsu.veretennikov.paint.algorithms.StampRenderer;
import ru.nsu.veretennikov.paint.model.AppState;
import ru.nsu.veretennikov.paint.ui.CanvasPanel;

public class StampTool implements Tool {
    @Override
    public void onMousePressed(CanvasPanel canvas, int x, int y) {
        AppState s = AppState.getInstance();
        StampRenderer.drawStamp(
                canvas.getImage(), x, y,
                s.getStampVertices(),
                s.getStampRadius(),
                s.getStampRotation(),
                s.getShapeType(),
                s.getCurrentColor()
        );
        canvas.repaint();
    }

    @Override public void reset() {}

    @Override public String getName() {
        return "Stamp";
    }

    @Override public String getToolTip() {
        return "Stamp: click to place shape at cursor position";
    }
}