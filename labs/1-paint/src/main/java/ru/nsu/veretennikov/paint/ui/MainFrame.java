package ru.nsu.veretennikov.paint.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CanvasPanel canvasPanel;

    public MainFrame() {
        super("ECGPaint");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));

        canvasPanel = new CanvasPanel(800, 600);

        JScrollPane scroll = new JScrollPane(canvasPanel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(Color.LIGHT_GRAY);

        AppToolBar toolBar = new AppToolBar(this, canvasPanel);

        setJMenuBar(new AppMenuBar(this, canvasPanel, toolBar));
        add(toolBar,  BorderLayout.NORTH);
        add(scroll,   BorderLayout.CENTER);

        setPreferredSize(new Dimension(1000, 720));
        pack();
        setLocationRelativeTo(null);
    }

    public CanvasPanel getCanvasPanel() {
        return canvasPanel;
    }
}
