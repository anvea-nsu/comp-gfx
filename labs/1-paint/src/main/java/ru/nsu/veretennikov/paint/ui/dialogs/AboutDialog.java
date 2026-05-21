package ru.nsu.veretennikov.paint.ui.dialogs;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {
    public AboutDialog(Frame owner) {
        super(owner, "About ECGPaint", true);

        JTextArea text = new JTextArea(
            "ECGPaint — 2D Drawing Application\n" +
            "Version 1.0\n\n" +
            "Author: Veretennikov A.A.\n\n" +
            "+----------------------------------+\n" +
            "| TOOLS\n|\n" +
            "| • Line  – Click to set the first anchor point; each subsequent\n" +
            "|   click draws a segment from the previous point to the new one\n" +
            "|   (polyline). Switching tools resets the anchor.\n|\n" +
            "| • Stamp – Click to stamp a polygon or star centred on the cursor.\n" +
            "|   Configure vertices, radius, and rotation in Stamp Settings.\n|\n" +
            "| • Fill  – Click inside any region to flood-fill it with the\n" +
            "|   current colour using the Span-fill algorithm.\n|\n" +
            "+----------------------------------+\n" +
            "| CANVAS\n|\n" +
            "| • Clear Canvas – resets the entire canvas to white.\n" +
            "| • Resize Canvas – enlarge (or shrink) the canvas; existing\n" +
            "|   content is preserved in the top-left corner.\n" +
            "| • Scrollbars appear automatically when the window is smaller\n" +
            "|   than the canvas.\n|\n" +
            "+----------------------------------+\n" +
            "| COLOUR\n|\n" +
            "| • Click any colour swatch in the toolbar for instant selection.\n" +
            "| • Use the colour-picker button (🎨) to choose any arbitrary RGB\n" +
            "|   colour from the system dialog.\n|\n" +
            "+----------------------------------+\n" +
            "| FILE\n|\n" +
            "| • Save – exports the canvas as PNG.\n" +
            "| • Open – loads images; the image is fitted to\n" +
            "|   the current canvas.\n" +
            "+----------------------------------+\n"
        );

        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        text.setBackground(UIManager.getColor("Panel.background"));
        text.setMargin(new Insets(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(520, 440));

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JPanel south = new JPanel();
        south.add(close);

        setLayout(new BorderLayout(4, 4));
        add(scroll, BorderLayout.CENTER);
        add(south,  BorderLayout.SOUTH);

        pack();
        setResizable(true);
        setLocationRelativeTo(owner);
    }
}
