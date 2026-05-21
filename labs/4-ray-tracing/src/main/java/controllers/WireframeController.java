package controllers;

import model.Camera;
import view.MainPanel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.BooleanSupplier;

public class WireframeController extends MouseAdapter {
    private final MainPanel panel;
    private final Camera camera;
    private final BooleanSupplier cameraControlEnabled;

    private int lastX;
    private int lastY;
    private boolean dragging = false;

    public WireframeController(MainPanel panel, Camera camera, BooleanSupplier cameraControlEnabled) {
        this.panel = panel;
        this.camera = camera;
        this.cameraControlEnabled = cameraControlEnabled;

        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);

        panel.setFocusable(true);
        panel.requestFocusInWindow();

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
        });
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!cameraControlEnabled.getAsBoolean()) {
            return;
        }

        panel.requestFocusInWindow();

        lastX = e.getX();
        lastY = e.getY();
        dragging = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!dragging || !cameraControlEnabled.getAsBoolean()) {
            return;
        }

        int currentX = e.getX();
        int currentY = e.getY();

        int dx = currentX - lastX;
        int dy = currentY - lastY;

        camera.rotateAroundView(dx, dy);

        lastX = currentX;
        lastY = currentY;

        panel.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!cameraControlEnabled.getAsBoolean()) {
            return;
        }

        if (e.isControlDown()) {
            camera.moveAlongView(e.getWheelRotation());
        } else {
            camera.zoom(e.getWheelRotation());
        }

        panel.repaint();
    }

    private void handleKeyPressed(KeyEvent e) {
        if (!cameraControlEnabled.getAsBoolean()) {
            return;
        }

        int keyCode = e.getKeyCode();

        switch (keyCode) {
            case KeyEvent.VK_LEFT -> camera.moveHorizontal(-1);
            case KeyEvent.VK_RIGHT -> camera.moveHorizontal(1);
            case KeyEvent.VK_UP -> camera.moveVertical(-1);
            case KeyEvent.VK_DOWN -> camera.moveVertical(1);
            default -> {
                return;
            }
        }

        panel.repaint();
    }
}