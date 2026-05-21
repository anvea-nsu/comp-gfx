package ecg.wireframe.view.dialogs;

import ecg.wireframe.model.SceneParameters;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog to edit rendering parameters: N, M, M1, zn.
 */
public class ParametersDialog extends JDialog {

    private boolean confirmed = false;

    private final JSpinner spN, spM, spM1;
    private final JSpinner spZn;

    public ParametersDialog(Frame owner, SceneParameters current) {
        super(owner, "Visualization Parameters", true);

        spN  = new JSpinner(new SpinnerNumberModel(current.N,  1, 200, 1));
        spM  = new JSpinner(new SpinnerNumberModel(current.M,  2, 360, 1));
        spM1 = new JSpinner(new SpinnerNumberModel(current.M1, 1, 50,  1));
        spZn = new JSpinner(new SpinnerNumberModel(current.zn, 0.1, 50.0, 0.1));
        ((JSpinner.NumberEditor) spZn.getEditor()).getFormat().setMinimumFractionDigits(1);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = 0; lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 4, 4, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1; fc.gridy = 0; fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0; fc.insets = new Insets(4, 0, 4, 4);

        addRow(form, lc, fc, "N — segments per B-spline section (≥ 1):", spN);
        addRow(form, lc, fc, "M — number of generatrices (≥ 2):",         spM);
        addRow(form, lc, fc, "M1 — circle segments between generatrices (≥ 1):", spM1);
        addRow(form, lc, fc, "Zoom (near plane distance):",                spZn);

        JButton ok     = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.setPreferredSize(cancel.getPreferredSize());
        ok.addActionListener(e -> { confirmed = true; dispose(); });
        cancel.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(ok);
        btnPanel.add(cancel);

        setLayout(new BorderLayout());
        add(form,     BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(ok);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel p, GridBagConstraints lc, GridBagConstraints fc,
                        String label, JComponent field) {
        p.add(new JLabel(label), lc);
        p.add(field, fc);
        lc.gridy++;
        fc.gridy++;
    }

    public boolean isConfirmed() { return confirmed; }

    public void applyTo(SceneParameters params) {
        params.N  = (Integer) spN.getValue();
        params.M  = (Integer) spM.getValue();
        params.M1 = (Integer) spM1.getValue();
        params.zn = (Double)  spZn.getValue();
    }
}
