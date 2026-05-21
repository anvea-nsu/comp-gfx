package ru.nsu.veretennikov.paint.ui;

import ru.nsu.veretennikov.paint.model.AppState;
import ru.nsu.veretennikov.paint.tools.*;
import ru.nsu.veretennikov.paint.tools.FillTool;
import ru.nsu.veretennikov.paint.tools.LineTool;
import ru.nsu.veretennikov.paint.tools.StampTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class CanvasPanel extends JPanel implements AppState.ToolChangeListener {
    private BufferedImage image;

    private final LineTool lineTool  = new LineTool();
    private final StampTool stampTool = new StampTool();
    private final FillTool fillTool  = new FillTool();

    public CanvasPanel(int width, int height) {
        image = createBlankImage(width, height);
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.LIGHT_GRAY);

        AppState.getInstance().addToolChangeListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dispatchToTool(e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public void onToolChanged(AppState.ToolType tool) {
        lineTool.reset();
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage img) {
        image = img;
        setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        revalidate();
        repaint();
    }

    public void clearCanvas() {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
        lineTool.reset();
        repaint();
    }

    public void resizeCanvas(int newW, int newH) {
        BufferedImage next = createBlankImage(newW, newH);
        Graphics2D g = next.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        image = next;
        setPreferredSize(new Dimension(newW, newH));
        lineTool.reset();
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }

    private void dispatchToTool(int x, int y) {
        switch (AppState.getInstance().getCurrentTool()) {
            case LINE:  lineTool.onMousePressed(this, x, y);  break;
            case STAMP: stampTool.onMousePressed(this, x, y); break;
            case FILL:  fillTool.onMousePressed(this, x, y);  break;
        }
    }

    private static BufferedImage createBlankImage(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }
}
