package view;

import javax.swing.*;

public class MyMenuBar extends JMenuBar {
    private final FileMenu fileMenu;
    private final SettingsMenu settingsMenu;
    private final ModeMenu modeMenu;
    private final HelpMenu helpMenu;

    public MyMenuBar() {
        fileMenu = new FileMenu();
        settingsMenu = new SettingsMenu();
        modeMenu = new ModeMenu();
        helpMenu = new HelpMenu();

        add(fileMenu);
        add(settingsMenu);
        add(modeMenu);
        add(helpMenu);
    }

    public FileMenu getFileMenu() {
        return fileMenu;
    }

    public SettingsMenu getSettingsMenu() {
        return settingsMenu;
    }

    public ModeMenu getModeMenu() {
        return modeMenu;
    }

    public HelpMenu getHelpMenu() {
        return helpMenu;
    }
}
