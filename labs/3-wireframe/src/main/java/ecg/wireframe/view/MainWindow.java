package ecg.wireframe.view;

import ecg.wireframe.controller.RenderController;
import ecg.wireframe.io.SceneFileManager;
import ecg.wireframe.model.Scene;
import ecg.wireframe.model.SceneParameters;
import ecg.wireframe.view.dialogs.AboutDialog;
import ecg.wireframe.view.dialogs.ParametersDialog;
import ecg.wireframe.view.editor.BSplineEditorDialog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class MainWindow extends JFrame {

    private final Scene       scene;
    private final RenderPanel renderPanel;

    public MainWindow() {
        super("ECGWireframe — 3D Revolution Body Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));

        scene       = new Scene();
        renderPanel = new RenderPanel(scene);

        // Wire up render interaction controller
        new RenderController(scene, renderPanel);

        // Update scene view size when panel resizes
        renderPanel.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                scene.setViewSize(renderPanel.getWidth(), renderPanel.getHeight());
                scene.rebuild();
            }
        });

        setJMenuBar(buildMenuBar());
        add(buildToolBar(), BorderLayout.NORTH);
        add(renderPanel,    BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    // ── Menu ─────────────────────────────────────────────────────────────────

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        // File
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(menuItem("Open…",            "Open scene from file",  KeyEvent.VK_O, e -> doOpen()));
        fileMenu.add(menuItem("Save…",            "Save scene to file",    KeyEvent.VK_S, e -> doSave()));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Exit",             "Exit application",      KeyEvent.VK_Q, e -> System.exit(0)));
        mb.add(fileMenu);

        // Edit
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(menuItem("Generatrix Editor…", "Open the B-spline generatrix editor", KeyEvent.VK_G, e -> doOpenEditor()));
        editMenu.add(menuItem("Parameters…",         "Edit rendering parameters",           KeyEvent.VK_P, e -> doOpenParams()));
        mb.add(editMenu);

        // View
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        viewMenu.add(menuItem("Reset Rotation", "Reset all rotation angles to zero", KeyEvent.VK_R, e -> doResetRotation()));
        mb.add(viewMenu);

        // Help
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(menuItem("About…", "Show information about the program", KeyEvent.VK_A, e -> doAbout()));
        mb.add(helpMenu);

        return mb;
    }

    private JMenuItem menuItem(String text, String tooltip, int mnemonic, ActionListener al) {
        JMenuItem item = new JMenuItem(text);
        item.setToolTipText(tooltip);
        item.setMnemonic(mnemonic);
        item.addActionListener(al);
        return item;
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private JToolBar buildToolBar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tb.add(toolBtn("Open",      "Open scene from file",               e -> doOpen()));
        tb.add(toolBtn("Save",      "Save scene to file",                 e -> doSave()));
        tb.addSeparator();
        tb.add(toolBtn("Editor",    "Open the B-spline generatrix editor", e -> doOpenEditor()));
        tb.add(toolBtn("Params",    "Edit rendering parameters",           e -> doOpenParams()));
        tb.addSeparator();
        tb.add(toolBtn("Reset Rot", "Reset all rotation angles to zero",   e -> doResetRotation()));
        tb.addSeparator();
        tb.add(toolBtn("About",     "Show information about the program",  e -> doAbout()));

        return tb;
    }

    private JButton toolBtn(String text, String tooltip, ActionListener al) {
        JButton b = new JButton(text);
        b.setToolTipText(tooltip);
        b.addActionListener(al);
        return b;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void doOpen() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("ECGWireframe scene (*.ecg)", "ecg"));
        fc.setDialogTitle("Open Scene");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try {
            SceneFileManager.load(f, scene);
        } catch (SceneFileManager.InvalidFileException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid scene file:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not read file:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doSave() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("ECGWireframe scene (*.ecg)", "ecg"));
        fc.setDialogTitle("Save Scene");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().endsWith(".ecg")) f = new File(f.getAbsolutePath() + ".ecg");
        try {
            SceneFileManager.save(scene, f);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not save file:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doOpenEditor() {
        BSplineEditorDialog dlg = new BSplineEditorDialog(this, scene);
        dlg.setVisible(true);
    }

    private void doOpenParams() {
        SceneParameters current = scene.getParams();
        ParametersDialog dlg = new ParametersDialog(this, current);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            dlg.applyTo(current);
            scene.setParams(current);
        }
    }

    private void doResetRotation() {
        SceneParameters p = scene.getParams();
        p.angleX = 0;
        p.angleY = 0;
        p.angleZ = 0;
        scene.rebuild();
    }

    private void doAbout() {
        new AboutDialog(this).setVisible(true);
    }
}
