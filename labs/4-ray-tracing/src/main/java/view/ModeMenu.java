package view;

import javax.swing.*;

public class ModeMenu extends JMenu {
    private final JRadioButtonMenuItem viewModeItem;
    private final JRadioButtonMenuItem renderModeItem;
    private final ButtonGroup buttonGroup;

    public ModeMenu() {
        setText("Mode");

        viewModeItem   = new JRadioButtonMenuItem("Wireframe View");
        renderModeItem = new JRadioButtonMenuItem("Ray-trace Render");

        viewModeItem.setSelected(true);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(viewModeItem);
        buttonGroup.add(renderModeItem);

        add(viewModeItem);
        add(renderModeItem);
    }

    public JRadioButtonMenuItem getViewModeItem()   { return viewModeItem;   }
    public JRadioButtonMenuItem getRenderModeItem() { return renderModeItem; }
    public ButtonGroup getButtonGroup()             { return buttonGroup;    }
}
