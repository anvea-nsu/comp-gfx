package ru.nsu.veretennikov.paint.tools;

import ru.nsu.veretennikov.paint.algorithms.BresenhamLine;
import ru.nsu.veretennikov.paint.model.AppState;
import ru.nsu.veretennikov.paint.ui.CanvasPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LineTool implements Tool {
    private Integer anchorX = null;
    private Integer anchorY = null;

    @Override
    public void onMousePressed(CanvasPanel canvas, int x, int y) {
        AppState state = AppState.getInstance();
        BufferedImage img = canvas.getImage();

        if (anchorX == null) {
            anchorX = x;
            anchorY = y;
            paintDot(img, x, y, state.getCurrentColor(), state.getLineThickness());
        } else {
            paintLine(img, anchorX, anchorY, x, y,
                      state.getCurrentColor(), state.getLineThickness());
            anchorX = x;
            anchorY = y;
        }
        canvas.repaint();
    }

    private void paintDot(BufferedImage img, int x, int y, Color color, int thickness) {
        if (thickness == 1) {
            if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
                img.setRGB(x, y, color.getRGB());
            }
        } else {
            Graphics2D g = img.createGraphics();
            g.setColor(color);
            int r = thickness / 2;
            g.fillOval(x - r, y - r, thickness, thickness);
            g.dispose();
        }
    }

    private void paintLine(BufferedImage img,
                           int x1, int y1, int x2, int y2,
                           Color color, int thickness) {
        if (thickness == 1) {
            BresenhamLine.drawLine(img, x1, y1, x2, y2, color);
        } else {
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setColor(color);
            g.setStroke(new BasicStroke(thickness,
                                        BasicStroke.CAP_ROUND,
                                        BasicStroke.JOIN_ROUND));
            g.drawLine(x1, y1, x2, y2);
            g.dispose();
        }
    }

    @Override
    public void reset() {
        anchorX = null;
        anchorY = null;
    }

    @Override
    public String getName() {
        return "Line";
    }

    @Override
    public String getToolTip() {
        return "Line: click to set anchor, click again to draw segment";
    }
}
