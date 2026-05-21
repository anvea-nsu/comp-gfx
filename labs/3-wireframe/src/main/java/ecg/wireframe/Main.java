package ecg.wireframe;

import ecg.wireframe.view.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            MainWindow win = new MainWindow();
            win.setVisible(true);
        });
    }
}
