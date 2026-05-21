package ecg.wireframe.view.dialogs;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {

    public AboutDialog(Frame owner) {
        super(owner, "About ECGWireframe", true);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 16, 30));

        String html = "<html><center>"
            + "<h2>ECGWireframe</h2>"
            + "<p>3D Wireframe Revolution Body Viewer</p>"
            + "<br>"
            + "<p>Renders a B-spline generatrix revolved around an axis,<br>"
            + "displayed using a perspective projection with depth coloring.</p>"
            + "<br>"
            + "<p><b>Controls:</b></p>"
            + "<p>Left-drag — rotate figure &nbsp;|&nbsp; Scroll — zoom</p>"
            + "</center></html>";

        JLabel text = new JLabel(html);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(text, new GridBagConstraints());

        JButton ok = new JButton("Close");
        ok.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(ok);

        setLayout(new BorderLayout());
        add(panel,    BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(ok);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }
}
