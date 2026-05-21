package ru.nsu.veretennikov.paint.ui.dialogs;

import javax.swing.*;
import java.awt.*;

public class CanvasResizeDialog extends JDialog {
    private final JTextField widthField;
    private final JTextField heightField;
    private boolean confirmed = false;

    public CanvasResizeDialog(Frame owner, int currentWidth, int currentHeight) {
        super(owner, "Resize Canvas", true);

        widthField  = new JTextField(String.valueOf(currentWidth),  6);
        heightField = new JTextField(String.valueOf(currentHeight), 6);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(6, 8, 6, 8);
        c.anchor  = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Width  (1–10000 px):"), c);
        c.gridx = 1;
        form.add(widthField, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("Height (1–10000 px):"), c);
        c.gridx = 1;
        form.add(heightField, c);

        JButton ok     = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> { if (validateInput()) { confirmed = true; dispose(); } });
        cancel.addActionListener(e -> dispose());

        JPanel south = new JPanel();
        south.add(ok);
        south.add(cancel);

        setLayout(new BorderLayout(4, 4));
        add(form,  BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private boolean validateInput() {
        try {
            int w = Integer.parseInt(widthField.getText().trim());
            int h = Integer.parseInt(heightField.getText().trim());
            if (w < 1 || w > 10000 || h < 1 || h > 10000) {
                JOptionPane.showMessageDialog(this,
                        "Width and height must be integers between 1 and 10000.",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid integer values.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getCanvasWidth() {
        return Integer.parseInt(widthField.getText().trim());
    }

    public int getCanvasHeight() {
        return Integer.parseInt(heightField.getText().trim());
    }
}