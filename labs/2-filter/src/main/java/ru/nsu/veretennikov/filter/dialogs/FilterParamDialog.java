package ru.nsu.veretennikov.filter.dialogs;

import ru.nsu.veretennikov.filter.filters.FilterParameter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FilterParamDialog extends JDialog {
    private final Map<String, Object> result = new LinkedHashMap<>();
    private boolean confirmed = false;

    private boolean[] valid;

    public static Map<String, Object> show(Frame parent, String filterName,
                                           List<FilterParameter> params,
                                           Map<String, Object> lastParams) {
        if (params == null || params.isEmpty()) return Collections.emptyMap();
        FilterParamDialog dlg = new FilterParamDialog(parent, filterName, params, lastParams);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(420, dlg.getHeight()));
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        return dlg.confirmed ? dlg.result : null;
    }

    private FilterParamDialog(Frame parent, String filterName,
                               List<FilterParameter> params,
                               Map<String, Object> lastParams) {
        super(parent, "Параметры: " + filterName, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        valid = new boolean[params.size()];
        Arrays.fill(valid, true);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel paramsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JButton ok     = new JButton("ОК");
        JButton cancel = new JButton("Отмена");

        for (int row = 0; row < params.size(); row++) {
            FilterParameter p = params.get(row);
            Object initValue = (lastParams != null && lastParams.containsKey(p.getKey()))
                    ? lastParams.get(p.getKey())
                    : p.getTypedDefault();

            gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
            paramsPanel.add(new JLabel(p.getLabel() + ":"), gc);

            if (p.getType() == FilterParameter.Type.CHOICE) {
                buildChoiceRow(paramsPanel, gc, row, p, initValue);
            } else {
                buildSliderRow(paramsPanel, gc, row, p, initValue, ok);
            }
        }

        ok.addActionListener(e -> {
            for (boolean v : valid) {
                if (!v) {
                    JOptionPane.showMessageDialog(this,
                            "Исправьте выделенные поля перед продолжением.",
                            "Неверные значения", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            confirmed = true;
            dispose();
        });
        cancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(ok);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.add(ok);
        btnPanel.add(cancel);

        content.add(paramsPanel, BorderLayout.CENTER);
        content.add(btnPanel,    BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void buildChoiceRow(JPanel panel, GridBagConstraints gc,
                                int row, FilterParameter p, Object initValue) {
        JComboBox<String> combo = new JComboBox<>(p.getChoices().toArray(new String[0]));
        String def = (initValue instanceof String) ? (String) initValue : p.getChoices().get(0);
        combo.setSelectedItem(def);
        result.put(p.getKey(), combo.getSelectedItem());

        gc.gridx = 1; gc.gridwidth = 2; gc.weightx = 1;
        panel.add(combo, gc);
        gc.gridwidth = 1;

        combo.addActionListener(e -> result.put(p.getKey(), combo.getSelectedItem()));
    }

    private void buildSliderRow(JPanel panel, GridBagConstraints gc,
                                int row, FilterParameter p, Object initValue,
                                JButton okButton) {
        boolean isInt = p.getType() == FilterParameter.Type.INT;
        double  min   = p.getMin(), max = p.getMax();
        double  init  = Math.min(max, Math.max(min, ((Number) initValue).doubleValue()));

        JSlider    slider = buildSlider(min, max, init, isInt);
        JTextField field  = new JTextField(formatValue(init, isInt), 8);
        field.setHorizontalAlignment(JTextField.RIGHT);

        result.put(p.getKey(), isInt ? (int) init : init);

        boolean[] updating = {false};

        slider.addChangeListener(e -> {
            if (updating[0]) return;
            updating[0] = true;
            double val = sliderToValue(slider.getValue(), min, max, isInt);
            field.setText(formatValue(val, isInt));
            field.setBackground(Color.WHITE);
            result.put(p.getKey(), isInt ? (int) val : val);
            valid[row] = true;
            updating[0] = false;
        });

        String rangeMsg = isInt
                ? String.format("Допустимый диапазон: [%d, %d]", (int) min, (int) max)
                : String.format("Допустимый диапазон: [%.2f, %.2f]", min, max);

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { check(); }
            @Override public void removeUpdate(DocumentEvent e)  { check(); }
            @Override public void changedUpdate(DocumentEvent e) { check(); }

            void check() {
                if (updating[0]) return;
                String txt = field.getText().trim();
                try {
                    double val = Double.parseDouble(txt);
                    if (val < min - 1e-9 || val > max + 1e-9) throw new NumberFormatException();
                    field.setBackground(Color.WHITE);
                    valid[row] = true;
                    result.put(p.getKey(), isInt ? (int) val : val);
                    updating[0] = true;
                    slider.setValue(valueToSlider(val, min, max, isInt));
                    updating[0] = false;
                } catch (NumberFormatException ex) {
                    field.setBackground(new Color(255, 160, 160));
                    valid[row] = false;
                }
            }
        });

        field.setToolTipText(rangeMsg);

        gc.gridx = 1; gc.weightx = 1; panel.add(slider, gc);
        gc.gridx = 2; gc.weightx = 0; panel.add(field,  gc);
    }

    private static final int SLIDER_STEPS = 1000;

    private static JSlider buildSlider(double min, double max, double init, boolean isInt) {
        JSlider s;
        if (isInt) {
            s = new JSlider((int) min, (int) max, (int) init);
        } else {
            s = new JSlider(0, SLIDER_STEPS, valueToSlider(init, min, max, false));
        }
        s.setPreferredSize(new Dimension(220, s.getPreferredSize().height));
        return s;
    }

    private static int valueToSlider(double val, double min, double max, boolean isInt) {
        if (isInt) return (int) val;
        if (max == min) return 0;
        return (int) Math.round((val - min) / (max - min) * SLIDER_STEPS);
    }

    private static double sliderToValue(int sv, double min, double max, boolean isInt) {
        if (isInt) return sv;
        return min + sv / (double) SLIDER_STEPS * (max - min);
    }

    private static String formatValue(double val, boolean isInt) {
        if (isInt) return String.valueOf((int) val);
        String s = String.format("%.3f", val);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }
}
