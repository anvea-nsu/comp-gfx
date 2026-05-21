package ru.nsu.veretennikov.paint.ui.dialogs;

import ru.nsu.veretennikov.paint.model.AppState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LineSettingsDialog extends JDialog {
    private static final int MIN = 1;
    private static final int MAX = 50;

    private final JSlider    slider;
    private final JTextField field;
    private boolean confirmed = false;

    public LineSettingsDialog(Frame owner) {
        super(owner, "Line Settings", true);

        int current = AppState.getInstance().getLineThickness();
        slider = new JSlider(MIN, MAX, current);
        field  = new JTextField(String.valueOf(current), 5);

        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        slider.addChangeListener(e ->
            field.setText(String.valueOf(slider.getValue())));

        Runnable syncField = () -> applyFieldValue();
        field.addActionListener(e -> syncField.run());
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { syncField.run(); }
        });

        JPanel centre = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);

        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        centre.add(new JLabel("Thickness (" + MIN + "–" + MAX + " px):"), c);

        c.gridx = 1;
        centre.add(field, c);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        centre.add(slider, c);

        JButton ok     = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> { applyFieldValue(); confirmed = true; dispose(); });
        cancel.addActionListener(e -> dispose());

        JPanel south = new JPanel();
        south.add(ok);
        south.add(cancel);

        setLayout(new BorderLayout(4, 4));
        add(centre, BorderLayout.CENTER);
        add(south,  BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void applyFieldValue() {
        String text = field.getText().trim();
        try {
            int val = Integer.parseInt(text);
            if (val < MIN || val > MAX) {
                showRangeError();
                field.setText(String.valueOf(slider.getValue()));
            } else {
                slider.setValue(val);
            }
        } catch (NumberFormatException ex) {
            showRangeError();
            field.setText(String.valueOf(slider.getValue()));
        }
    }

    private void showRangeError() {
        JOptionPane.showMessageDialog(this,
            "Please enter an integer between " + MIN + " and " + MAX + ".",
            "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getThickness() {
        return slider.getValue();
    }
}
