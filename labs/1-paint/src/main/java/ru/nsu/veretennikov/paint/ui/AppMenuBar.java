package ru.nsu.veretennikov.paint.ui;

import ru.nsu.veretennikov.paint.model.AppState;
import ru.nsu.veretennikov.paint.ui.dialogs.*;
import ru.nsu.veretennikov.paint.ui.dialogs.AboutDialog;
import ru.nsu.veretennikov.paint.ui.dialogs.CanvasResizeDialog;
import ru.nsu.veretennikov.paint.ui.dialogs.LineSettingsDialog;
import ru.nsu.veretennikov.paint.ui.dialogs.StampSettingsDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class AppMenuBar extends JMenuBar implements AppState.ToolChangeListener {
    private final Map<AppState.ToolType, JRadioButtonMenuItem> toolItems =
            new EnumMap<>(AppState.ToolType.class);

    private final MainFrame   owner;
    private final CanvasPanel canvas;

    public AppMenuBar(MainFrame owner, CanvasPanel canvas, AppToolBar toolBar) {
        this.owner  = owner;
        this.canvas = canvas;

        add(buildFileMenu());
        add(buildEditMenu());
        add(buildToolsMenu());
        add(buildColorMenu(toolBar));
        add(buildHelpMenu());

        AppState.getInstance().addToolChangeListener(this);
    }

    private JMenu buildFileMenu() {
        JMenu menu = new JMenu("File");
        menu.setMnemonic('F');

        JMenuItem open = new JMenuItem("Open…");
        open.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        open.addActionListener(e -> openFile());
        menu.add(open);

        JMenuItem save = new JMenuItem("Save as PNG…");
        save.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        save.addActionListener(e -> saveFile());
        menu.add(save);

        menu.addSeparator();

        JMenuItem exit = new JMenuItem("Exit");
        exit.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exit.addActionListener(e -> System.exit(0));
        menu.add(exit);

        return menu;
    }

    private JMenu buildEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic('E');

        JMenuItem clear = new JMenuItem("Clear Canvas");
        clear.setAccelerator(KeyStroke.getKeyStroke("ctrl shift C"));
        clear.setToolTipText("Fill the entire canvas with white");
        clear.addActionListener(e -> canvas.clearCanvas());
        menu.add(clear);

        JMenuItem resize = new JMenuItem("Resize Canvas…");
        resize.addActionListener(e -> resizeCanvas());
        menu.add(resize);

        return menu;
    }

    private JMenu buildToolsMenu() {
        JMenu menu = new JMenu("Tools");
        menu.setMnemonic('T');

        ButtonGroup group = new ButtonGroup();

        addToolItem(menu, group, AppState.ToolType.LINE,  "Line",  "L");
        addToolItem(menu, group, AppState.ToolType.STAMP, "Stamp", "T");
        addToolItem(menu, group, AppState.ToolType.FILL,  "Fill",  "I");

        toolItems.get(AppState.getInstance().getCurrentTool()).setSelected(true);

        menu.addSeparator();

        JMenuItem lineSettings = new JMenuItem("Line Settings…");
        lineSettings.addActionListener(e -> openLineSettings());
        menu.add(lineSettings);

        JMenuItem stampSettings = new JMenuItem("Stamp Settings…");
        stampSettings.addActionListener(e -> openStampSettings());
        menu.add(stampSettings);

        return menu;
    }

    private void addToolItem(JMenu menu, ButtonGroup group,
                             AppState.ToolType type, String label, String accel) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
        item.setAccelerator(KeyStroke.getKeyStroke("ctrl " + accel));
        item.addActionListener(e -> AppState.getInstance().setCurrentTool(type));
        group.add(item);
        menu.add(item);
        toolItems.put(type, item);
    }

    private JMenu buildColorMenu(AppToolBar toolBar) {
        JMenu menu = new JMenu("Color");
        menu.setMnemonic('C');

        Color[] colors = AppToolBar.BASIC_COLORS;
        String[] names = AppToolBar.COLOR_NAMES;
        for (int i = 0; i < colors.length; i++) {
            final Color c = colors[i];
            JMenuItem item = new JMenuItem(names[i]);
            // small colour square as icon
            item.setIcon(makeColorIcon(c, 14));
            item.addActionListener(e -> AppState.getInstance().setCurrentColor(c));
            menu.add(item);
        }

        menu.addSeparator();

        JMenuItem custom = new JMenuItem("Custom Colour…");
        custom.setAccelerator(KeyStroke.getKeyStroke("ctrl shift K"));
        custom.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(owner, "Choose Colour",
                    AppState.getInstance().getCurrentColor());
            if (chosen != null) AppState.getInstance().setCurrentColor(chosen);
        });
        menu.add(custom);

        return menu;
    }

    private JMenu buildHelpMenu() {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic('H');

        JMenuItem about = new JMenuItem("About ICGPaint…");
        about.addActionListener(e -> new AboutDialog(owner).setVisible(true));
        menu.add(about);

        return menu;
    }

    private void openFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open Image");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Image files (PNG, JPEG, BMP, GIF)",
                "png", "jpg", "jpeg", "bmp", "gif"));
        if (fc.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try {
            BufferedImage loaded = ImageIO.read(file);
            if (loaded == null) {
                JOptionPane.showMessageDialog(owner,
                        "Could not read image: " + file.getName(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BufferedImage rgb = new BufferedImage(
                    loaded.getWidth(), loaded.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(loaded, 0, 0, null);
            g.dispose();

            canvas.setImage(rgb);
            owner.setTitle("ICGPaint – " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner,
                    "Failed to open file:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save as PNG");
        fc.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
        fc.setSelectedFile(new File("drawing.png"));
        if (fc.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getAbsolutePath() + ".png");
        }
        try {
            ImageIO.write(canvas.getImage(), "png", file);
            JOptionPane.showMessageDialog(owner,
                    "Saved to " + file.getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            owner.setTitle("ICGPaint – " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner,
                    "Failed to save file:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resizeCanvas() {
        CanvasResizeDialog dlg = new CanvasResizeDialog(owner,
                canvas.getImage().getWidth(), canvas.getImage().getHeight());
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            canvas.resizeCanvas(dlg.getCanvasWidth(), dlg.getCanvasHeight());
        }
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

    @Override
    public void onToolChanged(AppState.ToolType tool) {
        JRadioButtonMenuItem item = toolItems.get(tool);
        if (item != null) item.setSelected(true);
    }

    private static Icon makeColorIcon(Color color, int size) {
        return new Icon() {
            @Override public int getIconWidth()  { return size; }
            @Override public int getIconHeight() { return size; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, size, size);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, size - 1, size - 1);
            }
        };
    }
}
