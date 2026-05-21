package ru.nsu.veretennikov.filter.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.RenderingHints;
import java.util.function.Consumer;

public class InterpolationSettingsDialog extends JDialog {
    public static final Object NEAREST  = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    public static final Object BILINEAR = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    public static final Object BICUBIC  = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

    private Object selected;

    public InterpolationSettingsDialog(Frame parent, Object current,
                                       Consumer<Object> onChange) {
        super(parent, "Интерполяция (режим «Подогнать»)", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        this.selected = current;

        ButtonGroup group = new ButtonGroup();
        JRadioButton bNearest  = new JRadioButton("Ближайший сосед");
        JRadioButton bBilinear = new JRadioButton("Билинейная (по умолчанию)");
        JRadioButton bBicubic  = new JRadioButton("Бикубическая");

        group.add(bNearest); group.add(bBilinear); group.add(bBicubic);

        if (current == NEAREST)       bNearest.setSelected(true);
        else if (current == BICUBIC)  bBicubic.setSelected(true);
        else                          bBilinear.setSelected(true);

        bNearest.addActionListener(e  -> { selected = NEAREST;  onChange.accept(NEAREST);  });
        bBilinear.addActionListener(e -> { selected = BILINEAR; onChange.accept(BILINEAR); });
        bBicubic.addActionListener(e  -> { selected = BICUBIC;  onChange.accept(BICUBIC);  });

        JPanel panel = new JPanel(new GridLayout(3, 1, 4, 4));
        panel.setBorder(new EmptyBorder(16, 20, 8, 20));
        panel.add(bNearest);
        panel.add(bBilinear);
        panel.add(bBicubic);

        JButton ok = new JButton("Закрыть");
        ok.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(ok);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(ok);

        JPanel content = new JPanel(new BorderLayout());
        content.add(panel,    BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(parent);
    }

    public Object getSelected() { return selected; }
}
