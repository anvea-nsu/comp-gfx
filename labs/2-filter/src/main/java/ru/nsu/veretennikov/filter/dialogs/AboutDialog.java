package ru.nsu.veretennikov.filter.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AboutDialog extends JDialog {
    public AboutDialog(Frame parent) {
        super(parent, "О программе", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("ECGFilter", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JTextArea info = new JTextArea(
            "Приложение для обработки растровых изображений.\n\n" +
            "Автор: Веретенников\n" +
            "Группа: ФИТ НГУ\n\n" +
            "Поддерживаемые форматы: PNG, JPEG, BMP, GIF\n\n" +
            "Реализованные фильтры:\n" +
            "  • Оттенки серого\n" +
            "  • Негатив\n" +
            "  • Сглаживание (Гаусс)\n" +
            "  • Повышение резкости\n" +
            "  • Тиснение\n" +
            "  • Гамма-коррекция\n" +
            "  • Оператор Робертса\n" +
            "  • Оператор Собеля\n" +
            "  • Дизеринг Флойда-Стейнберга\n" +
            "  • Упорядоченный дизеринг\n" +
            "  • Акварелизация\n" +
            "  • Поворот\n" +
            "  • Закручивание (Swirl) — авторский фильтр"
        );
        info.setEditable(false);
        info.setOpaque(false);
        info.setFont(UIManager.getFont("Label.font"));

        JButton ok = new JButton("ОК");
        ok.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(ok);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(ok);

        panel.add(title,    BorderLayout.NORTH);
        panel.add(info,     BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        setLocationRelativeTo(parent);
    }
}
