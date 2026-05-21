package ru.nsu.veretennikov.filter;

import ru.nsu.veretennikov.filter.dialogs.AboutDialog;
import ru.nsu.veretennikov.filter.dialogs.FilterParamDialog;
import ru.nsu.veretennikov.filter.dialogs.InterpolationSettingsDialog;
import ru.nsu.veretennikov.filter.filters.Filter;
import ru.nsu.veretennikov.filter.filters.FilterParameter;
import ru.nsu.veretennikov.filter.filters.impl.*;
import ru.nsu.veretennikov.filter.model.ImageModel;
import ru.nsu.veretennikov.filter.view.ImagePanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    private final ImageModel model = new ImageModel();
    private final ImagePanel imagePanel = new ImagePanel();

    private final Map<String, Map<String, Object>> lastParams = new HashMap<>();

    private final List<Filter> filters = buildFilterList();

    private JRadioButtonMenuItem menuRealSize;
    private JRadioButtonMenuItem menuFitScreen;
    private JRadioButtonMenuItem menuShowOriginal;
    private JRadioButtonMenuItem menuShowProcessed;

    private JToggleButton tbRealSize;
    private JToggleButton tbFitScreen;

    private final JFileChooser fileChooser;

    public MainFrame() {
        super("ECGFilter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(1000, 700));

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Изображения (PNG, JPEG, BMP, GIF)",
                "png", "jpg", "jpeg", "bmp", "gif"));

        setJMenuBar(buildMenuBar());
        add(buildToolBar(), BorderLayout.NORTH);

        imagePanel.setBackground(new Color(192, 192, 192));
        imagePanel.setToggleCallback(this::toggleShowOriginal);
        add(imagePanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private static List<Filter> buildFilterList() {
        return Arrays.asList(
            new GrayscaleFilter(),
            new NegativeFilter(),
            new GaussianBlurFilter(),
            new SharpenFilter(),
            new EmbossFilter(),
            new GammaFilter(),
            new RobertsFilter(),
            new SobelFilter(),
            new FloydSteinbergFilter(),
            new OrderedDitheringFilter(),
            new WatercolorFilter(),
            new RotationFilter(),
            new SwirlFilter()
        );
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.add(buildFileMenu());
        bar.add(buildImageMenu());
        bar.add(buildFiltersMenu());
        bar.add(buildSettingsMenu());
        bar.add(buildHelpMenu());

        return bar;
    }

    private JMenu buildFileMenu() {
        JMenu menu = new JMenu("Файл");
        menu.setMnemonic(KeyEvent.VK_F);

        JMenuItem open = new JMenuItem("Открыть...");
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        open.addActionListener(e -> doOpen());

        JMenuItem save = new JMenuItem("Сохранить результат...");
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        save.addActionListener(e -> doSave());

        JMenuItem exit = new JMenuItem("Выход");
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exit.addActionListener(e -> System.exit(0));

        menu.add(open);
        menu.add(save);
        menu.addSeparator();
        menu.add(exit);

        return menu;
    }

    private JMenu buildImageMenu() {
        JMenu menu = new JMenu("Изображение");

        ButtonGroup modeGroup = new ButtonGroup();
        menuRealSize = new JRadioButtonMenuItem("Реальный размер", true);
        menuFitScreen = new JRadioButtonMenuItem("Подогнать под экран", false);
        menuRealSize.addActionListener(e -> setDisplayMode(ImagePanel.DisplayMode.REAL_SIZE));
        menuFitScreen.addActionListener(e -> setDisplayMode(ImagePanel.DisplayMode.FIT_TO_SCREEN));
        modeGroup.add(menuRealSize);
        modeGroup.add(menuFitScreen);

        menu.add(menuRealSize);
        menu.add(menuFitScreen);
        menu.addSeparator();

        ButtonGroup viewGroup = new ButtonGroup();
        menuShowOriginal = new JRadioButtonMenuItem("Показать оригинал", true);
        menuShowProcessed = new JRadioButtonMenuItem("Показать результат", false);
        menuShowOriginal.addActionListener(e -> setShowOriginal(true));
        menuShowProcessed.addActionListener(e -> setShowOriginal(false));
        viewGroup.add(menuShowOriginal);
        viewGroup.add(menuShowProcessed);

        menu.add(menuShowOriginal);
        menu.add(menuShowProcessed);

        return menu;
    }

    private JMenu buildFiltersMenu() {
        JMenu menu = new JMenu("Фильтры");
        for (Filter f : filters) {
            JMenuItem item = new JMenuItem(f.getName());
            item.addActionListener(e -> applyFilter(f));
            menu.add(item);
        }
        return menu;
    }

    private JMenu buildSettingsMenu() {
        JMenu menu = new JMenu("Настройки");
        JMenuItem interpolationItem = new JMenuItem("Интерполяция...");
        interpolationItem.addActionListener(e -> showInterpolationDialog());
        menu.add(interpolationItem);

        return menu;
    }

    private JMenu buildHelpMenu() {
        JMenu menu = new JMenu("Справка");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e -> new AboutDialog(this).setVisible(true));
        menu.add(aboutItem);

        return menu;
    }

    private JToolBar buildToolBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        bar.add(makeButton("Открыть", "Открыть файл изображения", e -> doOpen()));
        bar.add(makeButton("Сохранить", "Сохранить результат в PNG", e -> doSave()));
        bar.addSeparator();

        ButtonGroup tg = new ButtonGroup();
        tbRealSize = makeToggle("1:1", "Режим реального размера");
        tbFitScreen = makeToggle("По экрану", "Подогнать под размер окна");
        tbRealSize.setSelected(true);
        tbRealSize.addActionListener(e -> setDisplayMode(ImagePanel.DisplayMode.REAL_SIZE));
        tbFitScreen.addActionListener(e -> setDisplayMode(ImagePanel.DisplayMode.FIT_TO_SCREEN));
        tg.add(tbRealSize);
        tg.add(tbFitScreen);

        bar.add(tbRealSize);
        bar.add(tbFitScreen);
        bar.addSeparator();

        for (Filter f : filters) {
            String shortName = shortName(f.getName());
            bar.add(makeButton(shortName, f.getName(), e -> applyFilter(f)));
        }

        bar.addSeparator();
        bar.add(makeButton("О прогр.", "О программе",
                e -> new AboutDialog(this).setVisible(true)));

        return bar;
    }

    private void doOpen() {
        int r = fileChooser.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        try {
            BufferedImage img = ImageIO.read(file);

            if (img == null) {
                JOptionPane.showMessageDialog(this,
                        "Не удалось прочитать файл:\n" + file.getName(),
                        "Ошибка открытия", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (img.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(),
                        BufferedImage.TYPE_INT_RGB);

                Graphics2D g = rgb.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, img.getWidth(), img.getHeight());
                g.drawImage(img, 0, 0, null);
                g.dispose();

                img = rgb;
            }

            model.setOriginal(img, file);
            imagePanel.setImage(model.getDisplayImage());
            syncViewToggle();
            setTitle("ECGFilter – " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка чтения файла:\n" + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doSave() {
        if (!model.hasProcessed()) {
            JOptionPane.showMessageDialog(this, "Нет обработанного изображения для сохранения.",
                    "Сохранение", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("PNG изображение", "png"));
        fc.setSelectedFile(new File("result.png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File dest = fc.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".png")) {
            dest = new File(dest.getAbsolutePath() + ".png");
        }

        try {
            ImageIO.write(model.getProcessed(), "PNG", dest);
            JOptionPane.showMessageDialog(this, "Файл сохранён:\n" + dest.getAbsolutePath(),
                    "Готово", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка записи:\n" + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter(Filter filter) {
        if (!model.hasImage()) {
            JOptionPane.showMessageDialog(this, "Сначала откройте изображение.",
                    "Нет изображения", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<FilterParameter> params = filter.getParameters();
        Map<String, Object> previous = lastParams.getOrDefault(filter.getName(), null);

        Map<String, Object> chosen;
        if (params.isEmpty()) {
            chosen = Collections.emptyMap();
        } else {
            chosen = FilterParamDialog.show(this, filter.getName(), params, previous);
            if (chosen == null) {
                return;
            }
        }
        lastParams.put(filter.getName(), chosen);

        final Map<String, Object> finalParams = chosen;
        final BufferedImage original = model.getOriginal();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() {
                return filter.apply(original, finalParams);
            }

            @Override
            protected void done() {
                try {
                    model.setProcessed(get());
                    imagePanel.setImage(model.getDisplayImage());
                    syncViewToggle();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ошибка фильтрации:\n" + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }.execute();
    }

    private void setDisplayMode(ImagePanel.DisplayMode m) {
        imagePanel.setDisplayMode(m);
        tbRealSize.setSelected(m == ImagePanel.DisplayMode.REAL_SIZE);
        tbFitScreen.setSelected(m == ImagePanel.DisplayMode.FIT_TO_SCREEN);
        menuRealSize.setSelected(m == ImagePanel.DisplayMode.REAL_SIZE);
        menuFitScreen.setSelected(m == ImagePanel.DisplayMode.FIT_TO_SCREEN);
    }

    private void setShowOriginal(boolean showOrig) {
        if (!model.hasProcessed()) {
            return;
        }
        model.setShowOriginal(showOrig);
        imagePanel.setImage(model.getDisplayImage());
        syncViewToggle();
    }

    private void toggleShowOriginal() {
        if (!model.hasProcessed()) {
            return;
        }
        model.toggleShowOriginal();
        imagePanel.setImage(model.getDisplayImage());
        syncViewToggle();
    }

    private void syncViewToggle() {
        boolean showOrig = !model.hasProcessed() || model.isShowOriginal();
        menuShowOriginal.setSelected(showOrig);
        menuShowProcessed.setSelected(!showOrig);
    }

    private void showInterpolationDialog() {
        new InterpolationSettingsDialog(this, imagePanel.getInterpolation(),
                imagePanel::setInterpolation).setVisible(true);
    }

    private static JButton makeButton(String text, String tooltip, ActionListener al) {
        JButton b = new JButton(text);

        b.setToolTipText(tooltip);
        b.addActionListener(al);
        b.setFocusable(false);

        return b;
    }

    private static JToggleButton makeToggle(String text, String tooltip) {
        JToggleButton b = new JToggleButton(text);

        b.setToolTipText(tooltip);
        b.setFocusable(false);

        return b;
    }

    private static String shortName(String name) {
        if (name.length() <= 10) {
            return name;
        }
        int space = name.indexOf(' ');
        if (space > 0 && space <= 10) {
            return name.substring(0, space);
        }
        return name.substring(0, 9) + "…";
    }
}
