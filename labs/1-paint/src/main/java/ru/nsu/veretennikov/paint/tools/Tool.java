package ru.nsu.veretennikov.paint.tools;

import ru.nsu.veretennikov.paint.ui.CanvasPanel;

public interface Tool {
    void onMousePressed(CanvasPanel canvas, int x, int y);

    void reset();

    String getName();
    String getToolTip();
}
