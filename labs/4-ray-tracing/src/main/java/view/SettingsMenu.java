package view;

import javax.swing.*;

public class SettingsMenu extends JMenu {
    private final JMenuItem loadRenderSettingsItem;
    private final JMenuItem saveRenderSettingsItem;
    private final JMenuItem renderSettingsItem;
    private final JMenuItem resetCameraItem;

    public SettingsMenu() {
        setText("Settings");

        loadRenderSettingsItem = new JMenuItem("Load Render Settings…");
        saveRenderSettingsItem = new JMenuItem("Save Render Settings…");
        renderSettingsItem     = new JMenuItem("Render Settings…");
        resetCameraItem        = new JMenuItem("Reset Camera");

        add(loadRenderSettingsItem);
        add(saveRenderSettingsItem);
        addSeparator();
        add(renderSettingsItem);
        addSeparator();
        add(resetCameraItem);
    }

    public JMenuItem getLoadRenderSettingsItem() { return loadRenderSettingsItem; }
    public JMenuItem getSaveRenderSettingsItem() { return saveRenderSettingsItem; }
    public JMenuItem getRenderSettingsItem()     { return renderSettingsItem;     }
    public JMenuItem getResetCameraItem()        { return resetCameraItem;        }
}
