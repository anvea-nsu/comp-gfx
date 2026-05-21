package ru.nsu.veretennikov.paint.ui.dialogs;

import ru.nsu.veretennikov.paint.model.AppState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class StampSettingsDialog extends JDialog {
    private final JRadioButton polygonBtn;
    private final JRadioButton starBtn;

    private final JSlider    verticesSlider;
    private final JTextField verticesField;

    private final JSlider    radiusSlider;
    private final JTextField radiusField;

    private final JSlider    rotationSlider;
    private final JTextField rotationField;

    private boolean confirmed = false;

    public StampSettingsDialog(Frame owner) {
        super(owner, "Stamp Settings", true);

        AppState s = AppState.getInstance();

        polygonBtn = new JRadioButton("Polygon", s.getShapeType() == AppState.ShapeType.POLYGON);
        starBtn    = new JRadioButton("Star",    s.getShapeType() == AppState.ShapeType.STAR);
        ButtonGroup shapeGroup = new ButtonGroup();
        shapeGroup.add(polygonBtn);
        shapeGroup.add(starBtn);

        verticesSlider  = new JSlider(3, 16, s.getStampVertices());
        verticesField   = new JTextField(String.valueOf(s.getStampVertices()), 4);

        radiusSlider    = new JSlider(5, 500, s.getStampRadius());
        radiusField     = new JTextField(String.valueOf(s.getStampRadius()), 4);

        rotationSlider  = new JSlider(0, 359, s.getStampRotation());
        rotationField   = new JTextField(String.valueOf(s.getStampRotation()), 4);

        wirePair(verticesSlider,  verticesField,  3,   16);
        wirePair(radiusSlider,    radiusField,    5,  500);
        wirePair(rotationSlider,  rotationField,  0,  359);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(4, 8, 4, 8);
        c.anchor  = GridBagConstraints.WEST;

        int row = 0;

        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Shape:"), c);
        JPanel shapePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        shapePanel.add(polygonBtn);
        shapePanel.add(starBtn);
        c.gridx = 1; c.gridwidth = 2;
        panel.add(shapePanel, c);

        row++;
        row = addSliderRow(panel, c, row, "Vertices (3–16):",   verticesSlider, verticesField);
        row = addSliderRow(panel, c, row, "Radius (5–500 px):", radiusSlider,   radiusField);
        row = addSliderRow(panel, c, row, "Rotation (0–359°):", rotationSlider, rotationField);

        JButton ok     = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> { confirmed = true; dispose(); });
        cancel.addActionListener(e -> dispose());
        JPanel south = new JPanel();
        south.add(ok);
        south.add(cancel);

        setLayout(new BorderLayout(4, 4));
        add(panel, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(420, 280));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void wirePair(JSlider slider, JTextField field, int min, int max) {
        slider.addChangeListener(e -> field.setText(String.valueOf(slider.getValue())));

        Runnable sync = () -> {
            String text = field.getText().trim();
            try {
                int val = Integer.parseInt(text);
                if (val < min || val > max) {
                    showError(min, max);
                    field.setText(String.valueOf(slider.getValue()));
                } else {
                    slider.setValue(val);
                }
            } catch (NumberFormatException ex) {
                showError(min, max);
                field.setText(String.valueOf(slider.getValue()));
            }
        };
        field.addActionListener(e -> sync.run());
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { sync.run(); }
        });
    }

    private int addSliderRow(JPanel panel, GridBagConstraints c,
                             int startRow, String label,
                             JSlider slider, JTextField field) {
        c.gridwidth = 1; c.fill = GridBagConstraints.NONE;

        c.gridx = 0; c.gridy = startRow;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        panel.add(field, c);

        startRow++;
        c.gridx = 0; c.gridy = startRow; c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(slider, c);

        return startRow + 1;
    }

    private void showError(int min, int max) {
        JOptionPane.showMessageDialog(this,
            "Value must be an integer between " + min + " and " + max + ".",
            "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public AppState.ShapeType getShapeType() {
        return polygonBtn.isSelected() ? AppState.ShapeType.POLYGON : AppState.ShapeType.STAR;
    }

    public int getVertices() {
        return verticesSlider.getValue();
    }

    public int getRadius() {
        return radiusSlider.getValue();
    }

    public int getRotation() {
        return rotationSlider.getValue();
    }
}
