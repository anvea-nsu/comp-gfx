package ru.nsu.veretennikov.paint.ui;

import ru.nsu.veretennikov.paint.model.AppState;
import ru.nsu.veretennikov.paint.ui.dialogs.LineSettingsDialog;
import ru.nsu.veretennikov.paint.ui.dialogs.StampSettingsDialog;
import ru.nsu.veretennikov.paint.ui.dialogs.AboutDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

public class AppToolBar extends JToolBar
        implements AppState.ToolChangeListener,
                   AppState.ColorChangeListener {
    private final Map<AppState.ToolType, JToggleButton> toolButtons =
            new EnumMap<>(AppState.ToolType.class);

    private JPanel colorIndicator;

    private final MainFrame owner;
    private final CanvasPanel canvas;

    static final Color[] BASIC_COLORS = {
        Color.BLACK,
        Color.RED,
        Color.YELLOW,
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.MAGENTA,
        Color.WHITE
    };

    static final String[] COLOR_NAMES = {
        "Black", "Red", "Yellow (R+G)", "Green", "Cyan (G+B)",
        "Blue", "Magenta (R+B)", "White"
    };

    public AppToolBar(MainFrame owner, CanvasPanel canvas) {
        super("Tools");
        this.owner  = owner;
        this.canvas = canvas;

        setFloatable(false);
        buildUI();

        AppState s = AppState.getInstance();
        s.addToolChangeListener(this);
        s.addColorChangeListener(this);
    }

    private void buildUI() {
        ButtonGroup toolGroup = new ButtonGroup();

        addToolButton(AppState.ToolType.LINE,  "Line",
                    "Line tool (L): click to set anchor, click to draw segment", toolGroup);
        addToolButton(AppState.ToolType.STAMP, "Stamp",
                    "Stamp tool (S): click to place shape",                      toolGroup);
        addToolButton(AppState.ToolType.FILL,  "Fill",
                    "Fill tool (F): flood-fill region with current colour",      toolGroup);

        toolButtons.get(AppState.getInstance().getCurrentTool()).setSelected(true);

        addSeparator();

        JButton lineSettings  = makeButton("Line…",  "Configure line thickness",  () -> openLineSettings());
        JButton stampSettings = makeButton("Stamp…", "Configure stamp parameters", () -> openStampSettings());
        add(lineSettings);
        add(stampSettings);

        addSeparator();

        JButton clearBtn = makeButton("Clear", "Clear canvas (fill with white)", () -> canvas.clearCanvas());
        add(clearBtn);

        addSeparator();

        add(new JLabel(" Color: "));

        for (int i = 0; i < BASIC_COLORS.length; i++) {
            add(makeColorSwatch(BASIC_COLORS[i], COLOR_NAMES[i]));
        }

        JButton pickerBtn = new JButton("🎨");
        pickerBtn.setToolTipText("Choose custom colour…");
        pickerBtn.setFocusPainted(false);
        pickerBtn.addActionListener(e -> openColorPicker());
        add(pickerBtn);

        addSeparator();

        add(new JLabel(" Current: "));

        colorIndicator = new JPanel();
        colorIndicator.setPreferredSize(new Dimension(28, 28));
        colorIndicator.setMaximumSize(new Dimension(28, 28));
        colorIndicator.setBackground(AppState.getInstance().getCurrentColor());
        colorIndicator.setBorder(new LineBorder(Color.DARK_GRAY, 1));
        colorIndicator.setToolTipText("Current colour");
        add(colorIndicator);

        addSeparator();

        JButton about = makeButton("About", "About ICGPaint",
            () -> new AboutDialog(owner).setVisible(true));
        add(about);
    }

    private void addToolButton(AppState.ToolType type, String label,
                                String tip, ButtonGroup group) {
        JToggleButton btn = new JToggleButton(label);
        btn.setToolTipText(tip);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> AppState.getInstance().setCurrentTool(type));
        group.add(btn);
        toolButtons.put(type, btn);
        add(btn);
    }

    private JButton makeButton(String label, String tip, Runnable action) {
        JButton btn = new JButton(label);
        btn.setToolTipText(tip);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JButton makeColorSwatch(Color color, String name) {
        JButton btn = new JButton();
        btn.setBackground(color);
        btn.setOpaque(true);
        btn.setBorder(new LineBorder(Color.DARK_GRAY, 1));
        btn.setPreferredSize(new Dimension(22, 22));
        btn.setMaximumSize(new Dimension(22, 22));
        btn.setToolTipText(name);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> AppState.getInstance().setCurrentColor(color));
        return btn;
    }

    private void openLineSettings() {
        LineSettingsDialog dlg = new LineSettingsDialog(owner);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            AppState.getInstance().setLineThickness(dlg.getThickness());
        }
    }

    private void openStampSettings() {
        StampSettingsDialog dlg = new StampSettingsDialog(owner);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            AppState s = AppState.getInstance();
            s.setShapeType(dlg.getShapeType());
            s.setStampVertices(dlg.getVertices());
            s.setStampRadius(dlg.getRadius());
            s.setStampRotation(dlg.getRotation());
        }
    }

    private void openColorPicker() {
        Color chosen = JColorChooser.showDialog(owner, "Choose Colour",
                AppState.getInstance().getCurrentColor());
        if (chosen != null) {
            AppState.getInstance().setCurrentColor(chosen);
        }
    }

    @Override
    public void onToolChanged(AppState.ToolType tool) {
        JToggleButton btn = toolButtons.get(tool);
        if (btn != null) btn.setSelected(true);
    }

    @Override
    public void onColorChanged(Color color) {
        colorIndicator.setBackground(color);
        colorIndicator.repaint();
    }
}
