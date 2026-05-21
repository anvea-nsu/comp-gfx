package ru.nsu.veretennikov.filter.view;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    public enum DisplayMode { REAL_SIZE, FIT_TO_SCREEN }

    private final DisplayCanvas canvas;
    private final JScrollPane   scrollPane;

    private DisplayMode mode           = DisplayMode.REAL_SIZE;
    private Object      interpolation  = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    private Runnable    toggleCallback = null;

    public ImagePanel() {
        setLayout(new BorderLayout());
        setBorder(new DashedBorder(4));

        canvas     = new DisplayCanvas();
        scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(192, 192, 192));

        add(scrollPane, BorderLayout.CENTER);
        installMouseHandlers();
    }

    public void setImage(BufferedImage img) {
        canvas.setImage(img);
        applyScrollPolicy();
    }

    public void setDisplayMode(DisplayMode m) {
        this.mode = m;
        canvas.setDisplayMode(m);
        applyScrollPolicy();
    }

    public DisplayMode getDisplayMode() { return mode; }

    public void setInterpolation(Object hint) {
        this.interpolation = hint;
        canvas.setInterpolation(hint);
    }

    public Object getInterpolation() { return interpolation; }

    public void setToggleCallback(Runnable cb) { this.toggleCallback = cb; }

    private void applyScrollPolicy() {
        if (mode == DisplayMode.REAL_SIZE) {
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy  (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        } else {
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy  (JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        }
        canvas.revalidate();
        canvas.repaint();
    }

    private void installMouseHandlers() {
        final Point[] pressPoint   = {null};
        final Point[] viewPosAtPress = {null};

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressPoint[0]    = e.getPoint();
                viewPosAtPress[0] = scrollPane.getViewport().getViewPosition();
                if (mode == DisplayMode.REAL_SIZE) {
                    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                canvas.setCursor(Cursor.getDefaultCursor());
                if (pressPoint[0] != null) {
                    int dx = e.getX() - pressPoint[0].x;
                    int dy = e.getY() - pressPoint[0].y;
                    if (Math.abs(dx) <= 4 && Math.abs(dy) <= 4 && toggleCallback != null) {
                        toggleCallback.run();
                    }
                }
                pressPoint[0] = null;
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressPoint[0] == null || mode != DisplayMode.REAL_SIZE) return;
                JViewport vp = scrollPane.getViewport();
                int dx = e.getX() - pressPoint[0].x;
                int dy = e.getY() - pressPoint[0].y;
                Dimension view   = vp.getViewSize();
                Dimension extent = vp.getExtentSize();
                int nx = clamp(viewPosAtPress[0].x - dx, 0, Math.max(0, view.width  - extent.width));
                int ny = clamp(viewPosAtPress[0].y - dy, 0, Math.max(0, view.height - extent.height));
                vp.setViewPosition(new Point(nx, ny));
            }
        });
    }

    private static int clamp(int v, int lo, int hi) { return v < lo ? lo : (Math.min(v, hi)); }

    class DisplayCanvas extends JPanel implements Scrollable {

        private BufferedImage image;
        private DisplayMode   mode          = DisplayMode.REAL_SIZE;
        private Object        interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;

        void setImage(BufferedImage img)   { image = img; revalidate(); repaint(); }
        void setDisplayMode(DisplayMode m) { mode  = m;   revalidate(); repaint(); }
        void setInterpolation(Object hint) { interpolation = hint; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(192, 192, 192));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (image == null) return;

            if (mode == DisplayMode.REAL_SIZE) {
                g2.drawImage(image, 0, 0, null);
            } else {
                double scaleX = (double) getWidth()  / image.getWidth();
                double scaleY = (double) getHeight() / image.getHeight();
                double scale  = Math.min(scaleX, scaleY);
                int w = (int)(image.getWidth()  * scale);
                int h = (int)(image.getHeight() * scale);
                int x = (getWidth()  - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2.drawImage(image, x, y, w, h, null);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (mode == DisplayMode.REAL_SIZE && image != null) {
                return new Dimension(image.getWidth(), image.getHeight());
            }
            return new Dimension(0, 0); // let viewport size dominate in FIT mode
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d)  { return 16;  }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 128; }
        @Override public boolean getScrollableTracksViewportWidth()  { return mode == DisplayMode.FIT_TO_SCREEN; }
        @Override public boolean getScrollableTracksViewportHeight() { return mode == DisplayMode.FIT_TO_SCREEN; }
    }

    private static class DashedBorder extends AbstractBorder {
        private final int inset;
        DashedBorder(int inset) { this.inset = inset; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(90, 90, 90));
            float[] dash = {7f, 5f};
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.drawRect(x + 1, y + 1, w - 3, h - 3);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(inset, inset, inset, inset); }
        @Override public Insets getBorderInsets(Component c, Insets ins) {
            ins.set(inset, inset, inset, inset); return ins;
        }
        @Override public boolean isBorderOpaque() { return false; }
    }
}
