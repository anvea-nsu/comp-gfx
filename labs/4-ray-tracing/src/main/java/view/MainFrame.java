package view;

import model.Camera;
import model.RenderModel;
import model.SceneModel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final MyMenuBar menuBar;
    private final MainPanel mainPanel;
    private final JToolBar toolBar;

    // Toolbar buttons accessible to the controller
    private final JButton openButton;
    private final JButton viewButton;
    private final JButton renderButton;
    private final JButton saveImageButton;
    private final JButton renderSettingsButton;
    private final JButton resetCameraButton;
    private final JButton aboutButton;

    public MainFrame(SceneModel sceneModel, RenderModel renderModel, Camera camera) {
        menuBar   = new MyMenuBar();
        mainPanel = new MainPanel(sceneModel, renderModel, camera);
        toolBar   = new JToolBar();
        toolBar.setFloatable(false);

        openButton          = makeButton("Open Scene",       "Open Scene");
        viewButton          = makeButton("Wireframe View",   "View");
        renderButton        = makeButton("Render",           "Render");
        saveImageButton     = makeButton("Save Image",       "Save Image");
        renderSettingsButton = makeButton("Render Settings", "Render Settings");
        resetCameraButton   = makeButton("Reset Camera",     "Reset Camera");
        aboutButton         = makeButton("About",            "About");

        toolBar.add(openButton);
        toolBar.addSeparator();
        toolBar.add(viewButton);
        toolBar.add(renderButton);
        toolBar.addSeparator();
        toolBar.add(saveImageButton);
        toolBar.addSeparator();
        toolBar.add(renderSettingsButton);
        toolBar.add(resetCameraButton);
        toolBar.addSeparator();
        toolBar.add(aboutButton);

        setTitle("ECG Raytracing");
        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);
        setJMenuBar(menuBar);

        add(toolBar,   BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private JButton makeButton(String tooltip, String label) {
        JButton btn = new JButton(label);
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        return btn;
    }

    public MyMenuBar getMyMenuBar()          { return menuBar;  }
    public MainPanel getMainPanel()          { return mainPanel; }

    public JButton getOpenButton()           { return openButton;           }
    public JButton getViewButton()           { return viewButton;           }
    public JButton getRenderButton()         { return renderButton;         }
    public JButton getSaveImageButton()      { return saveImageButton;      }
    public JButton getRenderSettingsButton() { return renderSettingsButton; }
    public JButton getResetCameraButton()    { return resetCameraButton;    }
    public JButton getAboutButton()          { return aboutButton;          }
}
