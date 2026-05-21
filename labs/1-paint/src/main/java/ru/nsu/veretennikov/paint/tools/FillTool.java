package ru.nsu.veretennikov.paint.tools;

import ru.nsu.veretennikov.paint.algorithms.SpanFill;
import ru.nsu.veretennikov.paint.model.AppState;
import ru.nsu.veretennikov.paint.ui.CanvasPanel;

public class FillTool implements Tool {
    @Override
    public void onMousePressed(CanvasPanel canvas, int x, int y) {
        AppState s = AppState.getInstance();
        SpanFill.fill(canvas.getImage(), x, y, s.getCurrentColor());
        canvas.repaint();
    }

    @Override public void reset() {}

    @Override public String getName() {
        return "Fill";
    }

    @Override public String getToolTip() {
        return "Fill: click inside a region to flood-fill with current color";
    }
}
