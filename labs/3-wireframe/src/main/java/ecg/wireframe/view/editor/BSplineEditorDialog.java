package ecg.wireframe.view.editor;

import ecg.wireframe.model.Scene;
import ecg.wireframe.model.math.Point2D;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog containing the B-spline editor.
 * Has OK / Cancel / Apply buttons.
 */
public class BSplineEditorDialog extends JDialog {

    private final Scene              scene;
    private final BSplineEditorPanel editorPanel;

    // Snapshot of control points at dialog open time (for Cancel)
    private final List<Point2D> originalPoints;

    public BSplineEditorDialog(Frame owner, Scene scene) {
        super(owner, "B-Spline Generatrix Editor", true);
        this.scene = scene;

        // Snapshot
        originalPoints = copyPoints(scene.getControlPoints());

        editorPanel = new BSplineEditorPanel();
        editorPanel.setControlPoints(scene.getControlPoints());

        // Live update when points change in editor
        editorPanel.setChangeListener(this::applyToScene);

        // ── Toolbar ─────────────────────────────────────────────────────────
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        JButton btnCenter = new JButton("Center");
        btnCenter.setToolTipText("Center view on control points");
        btnCenter.addActionListener(e -> editorPanel.centerView());

        JButton btnNorm = new JButton("Auto-Fit");
        btnNorm.setToolTipText("Scale and center to fit all control points");
        btnNorm.addActionListener(e -> editorPanel.autoNormalize());

        tb.add(btnCenter);
        tb.add(btnNorm);

        // ── Button panel ────────────────────────────────────────────────────
        JButton btnApply = new JButton("Apply");
        btnApply.setToolTipText("Apply changes without closing");
        btnApply.addActionListener(e -> applyToScene());

        JButton btnOk = new JButton("OK");
        btnOk.setToolTipText("Apply changes and close");
        btnOk.addActionListener(e -> { applyToScene(); dispose(); });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setToolTipText("Discard changes and close");
        btnCancel.addActionListener(e -> {
            scene.setControlPoints(originalPoints);
            dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnApply);
        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);

        // ── Status label ────────────────────────────────────────────────────
        JLabel hint = new JLabel("Right-click: add/remove point   |   Left-drag: move point   |   Wheel: zoom");
        hint.setForeground(Color.GRAY);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        // ── Layout ──────────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(tb,         BorderLayout.NORTH);
        add(editorPanel, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(hint,     BorderLayout.WEST);
        south.add(btnPanel, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(640, 520));
        setLocationRelativeTo(owner);

        // Center/fit once the dialog is visible and sized
        SwingUtilities.invokeLater(editorPanel::autoNormalize);
    }

    private void applyToScene() {
        scene.setControlPoints(editorPanel.getControlPoints());
    }

    private List<Point2D> copyPoints(List<Point2D> src) {
        java.util.List<Point2D> copy = new java.util.ArrayList<>();
        for (Point2D p : src) copy.add(p.copy());
        return copy;
    }
}
