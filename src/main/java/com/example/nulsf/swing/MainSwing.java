package com.example.nulsf.swing;

import javax.swing.*;

public class MainSwing {
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        // Apply a modern System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Launch the AuthFrame (which contains Login and Register tabs)
        SwingUtilities.invokeLater(() -> {
            AuthFrame authFrame = new AuthFrame();
            authFrame.setVisible(true);
        });
    }
}