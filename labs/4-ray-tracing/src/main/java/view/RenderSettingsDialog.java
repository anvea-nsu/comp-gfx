package view;

import model.RenderModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class RenderSettingsDialog extends JDialog {
    private final RenderModel renderModel;

    private final JSpinner gammaSpinner;
    private final JSpinner depthSpinner;

    private Color selectedBackgroundColor;
    private final JButton colorButton;

    private boolean applied = false;

    public RenderSettingsDialog(JFrame owner, RenderModel renderModel) {
        super(owner, "Render Settings", true);

        this.renderModel = renderModel;

        selectedBackgroundColor = new Color(
                renderModel.getBackgroundR(),
                renderModel.getBackgroundG(),
                renderModel.getBackgroundB()
        );

        gammaSpinner = new JSpinner(new SpinnerNumberModel(renderModel.getGamma(), 0.0, 10.0, 0.1));
        depthSpinner = new JSpinner(new SpinnerNumberModel((int) renderModel.getDepth(), 1, 10, 1));

        colorButton = new JButton("Choose…");
        colorButton.setBackground(selectedBackgroundColor);
        colorButton.setOpaque(true);
        colorButton.setPreferredSize(new Dimension(120, 28));

        initLayout();
        initListeners();

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initLayout() {
        // ── Parameters panel ────────────────────────────────────────────
        JPanel paramsPanel = new JPanel(new GridBagLayout());
        paramsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Parameters",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 8, 6, 8);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        // Row 0 – background colour
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        paramsPanel.add(new JLabel("Background colour:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        paramsPanel.add(colorButton, gbc);

        // Row 1 – gamma
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        paramsPanel.add(new JLabel("Gamma:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        paramsPanel.add(gammaSpinner, gbc);

        // Row 2 – depth
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        paramsPanel.add(new JLabel("Trace depth:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        paramsPanel.add(depthSpinner, gbc);

        // ── Buttons panel ────────────────────────────────────────────────
        JButton okButton     = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.setPreferredSize(new Dimension(80, 28));
        cancelButton.setPreferredSize(new Dimension(80, 28));

        okButton.addActionListener(e -> onOk());
        cancelButton.addActionListener(e -> onCancel());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        // ── Root layout ──────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout(0, 4));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        root.add(paramsPanel,  BorderLayout.CENTER);
        root.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(okButton);
    }

    private void initListeners() {
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(
                    this, "Choose background colour", selectedBackgroundColor);

            if (newColor != null) {
                selectedBackgroundColor = newColor;
                colorButton.setBackground(selectedBackgroundColor);
            }
        });
    }

    private void onOk() {
        double gamma = ((Number) gammaSpinner.getValue()).doubleValue();
        int    depth = ((Number) depthSpinner.getValue()).intValue();

        if (gamma < 0.0 || gamma > 10.0) {
            JOptionPane.showMessageDialog(this,
                    "Gamma must be in range 0..10", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (depth < 1 || depth > 10) {
            JOptionPane.showMessageDialog(this,
                    "Depth must be in range 1..10", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        renderModel.setBackgroundR(selectedBackgroundColor.getRed());
        renderModel.setBackgroundG(selectedBackgroundColor.getGreen());
        renderModel.setBackgroundB(selectedBackgroundColor.getBlue());

        renderModel.setGamma(gamma);
        renderModel.setDepth(depth);

        applied = true;
        dispose();
    }

    private void onCancel() {
        applied = false;
        dispose();
    }

    public boolean isApplied() {
        return applied;
    }
}
