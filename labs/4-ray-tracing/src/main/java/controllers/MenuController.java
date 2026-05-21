package controllers;

import io.RenderFileParser;
import io.RenderFileSaver;
import io.SceneFileParser;
import model.Camera;
import model.Point3D;
import model.RenderModel;
import model.SceneModel;
import render.RayTracer;
import view.MainFrame;
import view.MyMenuBar;
import view.RenderSettingsDialog;
import view.MainPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class MenuController {

    private static final String MODE_VIEW   = "VIEW";
    private static final String MODE_RENDER = "RENDER";

    private final MainFrame frame;
    private final SceneModel sceneModel;
    private final RenderModel renderModel;
    private final Camera camera;

    private Camera initialCamera;

    private String mode = MODE_VIEW;
    private boolean rendering = false;
    private SwingWorker<BufferedImage, Void> renderWorker;

    public MenuController(MainFrame frame, SceneModel sceneModel, RenderModel renderModel, Camera camera) {
        this.frame = frame;
        this.sceneModel = sceneModel;
        this.renderModel = renderModel;
        this.camera = camera;

        bindActions(frame.getMyMenuBar());
        bindToolBar();
    }

    public void bindActions(MyMenuBar menuBar) {
        menuBar.getFileMenu().getOpenSceneItem().addActionListener(e -> onOpen());
        menuBar.getFileMenu().getSaveImageItem().addActionListener(e -> onSaveImage());

        menuBar.getSettingsMenu().getResetCameraItem().addActionListener(e -> onResetCamera());
        menuBar.getSettingsMenu().getLoadRenderSettingsItem().addActionListener(e -> onLoadRenderSettings());
        menuBar.getSettingsMenu().getSaveRenderSettingsItem().addActionListener(e -> onSaveRenderSettings());
        menuBar.getSettingsMenu().getRenderSettingsItem().addActionListener(e -> onRenderSettings());

        menuBar.getModeMenu().getViewModeItem().addActionListener(e -> onSelectView());
        menuBar.getModeMenu().getRenderModeItem().addActionListener(e -> onRender());

        menuBar.getHelpMenu().getAboutItem().addActionListener(e -> onAbout());
    }

    private void bindToolBar() {
        frame.getOpenButton().addActionListener(e -> onOpen());
        frame.getViewButton().addActionListener(e -> onSelectView());
        frame.getRenderButton().addActionListener(e -> onRender());
        frame.getSaveImageButton().addActionListener(e -> onSaveImage());
        frame.getRenderSettingsButton().addActionListener(e -> onRenderSettings());
        frame.getResetCameraButton().addActionListener(e -> onResetCamera());
        frame.getAboutButton().addActionListener(e -> onAbout());
    }

    private void onOpen() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter(".scene", "scene"));

        int result = fileChooser.showOpenDialog(frame);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File sceneFile = fileChooser.getSelectedFile();

        if (sceneFile == null) {
            return;
        }

        try {
            sceneModel.clear();
            renderModel.clear();

            initialCamera = null;

            SceneFileParser sceneFileParser = new SceneFileParser(sceneFile.getAbsolutePath(), sceneModel);
            sceneFileParser.parseSceneFileHeader();

            mode = MODE_VIEW;
            frame.getMyMenuBar().getModeMenu().getViewModeItem().setSelected(true);

            File renderFile = getRenderFileForScene(sceneFile);

            if (renderFile.exists() && renderFile.isFile()) {
                RenderFileParser renderFileParser = new RenderFileParser(renderFile.getAbsolutePath(), renderModel, camera);

                renderFileParser.parseRenderFile();
                camera.recalculate();
            } else {
                camera.setEye(new Point3D(-25, 0, 4));
                camera.setView(new Point3D(0, 0, 1));
                camera.setUp(new Point3D(0, 0, 1));

                camera.setZn(8);
                camera.setZf(60);

                camera.setSw(12);
                camera.setSh(9);

                camera.recalculate();
            }

            camera.updateAspectRatio(frame.getMainPanel().getWidth(), frame.getMainPanel().getHeight());

            saveInitialCamera();

            mode = MODE_VIEW;
            rendering = false;
            frame.getMainPanel().clearRenderedImage();

            frame.getMainPanel().repaint();

        } catch (Exception e) {
            sceneModel.clear();
            renderModel.clear();
            initialCamera = null;

            JOptionPane.showMessageDialog(frame, "Can't open file:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLoadRenderSettings() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter(".render", "render"));

        int result = fileChooser.showOpenDialog(frame);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();

        if (file == null) {
            return;
        }

        try {
            RenderFileParser renderFileParser = new RenderFileParser(
                    file.getAbsolutePath(),
                    renderModel,
                    camera
            );

            renderFileParser.parseRenderFile();
            camera.recalculate();

            camera.updateAspectRatio(frame.getMainPanel().getWidth(), frame.getMainPanel().getHeight());

            saveInitialCamera();

            frame.getMainPanel().clearRenderedImage();
            frame.getMainPanel().setBackground(new Color(renderModel.getBackgroundR(), renderModel.getBackgroundG(), renderModel.getBackgroundB()));
            frame.getMainPanel().repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can't open render settings:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveInitialCamera() {
        if (camera.getEye() == null || camera.getView() == null || camera.getUp() == null) {
            return;
        }

        camera.recalculate();
        initialCamera = camera.copy();
    }

    private void onResetCamera() {
        if (initialCamera != null) {
            camera.copyFrom(initialCamera);
        }

        camera.recalculate();
        frame.getMainPanel().repaint();
    }

    private File getRenderFileForScene(File sceneFile) {
        String sceneName = sceneFile.getName();

        int dotIndex = sceneName.lastIndexOf('.');

        String baseName;
        if (dotIndex == -1) {
            baseName = sceneName;
        } else {
            baseName = sceneName.substring(0, dotIndex);
        }

        return new File(sceneFile.getParentFile(), baseName + ".render");
    }

    private void onSaveRenderSettings() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter(".render", "render"));

        int result = fileChooser.showSaveDialog(frame);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();

        if (file == null) {
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".render")) {
            file = new File(file.getParentFile(), file.getName() + ".render");
        }

        try {
            RenderFileSaver saver = new RenderFileSaver(file.getAbsolutePath(), renderModel, camera);

            saver.save();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can't save render settings:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSaveImage() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG image", "png"));

        int result = fileChooser.showSaveDialog(frame);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();

        if (file == null) {
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getParentFile(), file.getName() + ".png");
        }

        try {
            frame.getMainPanel().saveImage(file.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Can't save image:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRenderSettings() {
        RenderSettingsDialog dialog = new RenderSettingsDialog(frame, renderModel);
        dialog.setVisible(true);

        if (dialog.isApplied()) {
            frame.getMainPanel().setBackground(new Color(renderModel.getBackgroundR(), renderModel.getBackgroundG(), renderModel.getBackgroundB()));

            frame.getMainPanel().repaint();
        }
    }

    private void onSelectView() {
        if (rendering) {
            return;
        }

        mode = MODE_VIEW;

        frame.getMainPanel().clearRenderedImage();
        frame.getMainPanel().repaint();
    }

    private void onRender() {
        if (rendering) {
            return;
        }

        rendering = true;
        mode = MODE_RENDER;

        MainPanel mainPanel = frame.getMainPanel();

        // ── Progress dialog ──────────────────────────────────────────────
        JDialog progressDialog = new JDialog(frame, "Rendering", false);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setResizable(false);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        JLabel statusLabel = new JLabel("Трассировка лучей…", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 16));

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        content.add(statusLabel, BorderLayout.NORTH);
        content.add(progressBar, BorderLayout.CENTER);

        progressDialog.setContentPane(content);
        progressDialog.pack();
        progressDialog.setSize(340, 110);
        progressDialog.setLocationRelativeTo(frame);
        // ────────────────────────────────────────────────────────────────

        renderWorker = new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() {
                RayTracer rayTracer = new RayTracer(sceneModel, renderModel, camera);

                return rayTracer.render(
                        mainPanel.getWidth(),
                        mainPanel.getHeight(),
                        this::setProgress
                );
            }

            @Override
            protected void done() {
                try {
                    BufferedImage image = get();
                    mainPanel.setRenderedImage(image);
                } catch (Exception e) {
                    mode = MODE_VIEW;
                    mainPanel.clearRenderedImage();

                    JOptionPane.showMessageDialog(
                            frame,
                            "Render error:\n" + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    rendering = false;
                    progressDialog.dispose();
                }
            }
        };

        renderWorker.addPropertyChangeListener(event -> {
            if ("progress".equals(event.getPropertyName())) {
                int progress = (int) event.getNewValue();
                progressBar.setValue(progress);
                statusLabel.setText("Трассировка лучей… " + progress + "%");
            }
        });

        renderWorker.execute();
        progressDialog.setVisible(true);
    }

    private void onAbout() {
        String message = """
        ECG Ray tracing
        
        Computer Graphics Laboratory Work.
        
        A 3D scene rendering application using ray tracing method.
        
        Supported primitives:
            - Sphere (SPHERE);
            - Box (BOX);
            - Triangle (TRIANGLE);
            - Quadrangle (QUADRANGLE).
        
        Features:
            - Load scene from .scene file;
            - Load and save render settings from/to .render file;
            - Wireframe preview of the scene;
            - Interactive camera control: rotate (mouse drag), zoom (mouse wheel), pan (keyboard arrows);
            - Adjust background color, gamma correction, and ray tracing depth;
            - Render image using ray tracing with:
                * Phong lighting model (ambient, diffuse, specular);
                * Hard shadows;
                * Recursive reflections (configurable depth);
            - Save wireframe or rendered image as PNG.
        
        Author: Andrey Veretennikov
        NSU, group 23208
        Discipline: Computer Graphics
        """;

        JOptionPane.showMessageDialog(
                frame,
                message,
                "About ICGRaytracing",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public boolean canControlCamera() {
        return MODE_VIEW.equals(mode) && !rendering;
    }
}
