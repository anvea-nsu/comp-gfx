package ecg.wireframe.controller;

import ecg.wireframe.model.Scene;
import ecg.wireframe.model.SceneParameters;

import javax.swing.*;
import java.awt.event.*;

/**
 * Attaches mouse listeners to a component, updating Scene rotation and zoom.
 */
public class RenderController implements MouseListener, MouseMotionListener, MouseWheelListener {

    private final Scene scene;
    private int lastX, lastY;

    public RenderController(Scene scene, JComponent target) {
        this.scene = scene;
        target.addMouseListener(this);
        target.addMouseMotionListener(this);
        target.addMouseWheelListener(this);
    }

    @Override public void mousePressed(MouseEvent e)  { lastX = e.getX(); lastY = e.getY(); }
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - lastX;
        int dy = e.getY() - lastY;
        lastX = e.getX();
        lastY = e.getY();

        SceneParameters p = scene.getParams();
        // Horizontal drag → spin around Y (world up)
        // Vertical drag   → tilt around X (pitch up/down)
        // Rotation order in Scene is Y*X*Z, so these stay independent and intuitive.
        p.angleY += dx * 0.5;
        p.angleX += dy * 0.5;
        scene.rebuild();
    }

    @Override public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        SceneParameters p = scene.getParams();
        double delta = e.getWheelRotation() * 0.15;
        p.zn = Math.max(0.1, Math.min(50.0, p.zn + delta));
        scene.rebuild();
    }
}
