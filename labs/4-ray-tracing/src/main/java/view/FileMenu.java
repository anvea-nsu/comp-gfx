package view;

import javax.swing.*;

public class FileMenu extends JMenu {
    private final JMenuItem openSceneItem;
    private final JMenuItem saveImageItem;

    public FileMenu() {
        setText("File");

        openSceneItem = new JMenuItem("Open Scene…");
        saveImageItem = new JMenuItem("Save Image…");

        add(openSceneItem);
        add(saveImageItem);
    }

    public JMenuItem getOpenSceneItem() { return openSceneItem; }
    public JMenuItem getSaveImageItem() { return saveImageItem; }
}
