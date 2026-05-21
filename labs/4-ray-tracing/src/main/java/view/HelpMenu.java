package view;

import javax.swing.*;

public class HelpMenu extends JMenu {
    private final JMenuItem aboutItem;

    public HelpMenu() {
        setText("Help");

        aboutItem = new JMenuItem("About…");

        add(aboutItem);
    }

    public JMenuItem getAboutItem() { return aboutItem; }
}
