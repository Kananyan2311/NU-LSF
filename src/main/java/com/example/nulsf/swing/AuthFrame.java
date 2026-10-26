package com.example.nulsf.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.example.nulsf.services.ApiService;

public class AuthFrame extends JFrame {
    private final ApiService apiService = new ApiService();

    public AuthFrame() {
        setTitle("Lost & Found System - Admin Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 450); // Increased height for more fields

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Login", createLoginPanel());
        tabbedPane.addTab("Register", createRegisterPanel());

        add(tabbedPane);
        setLocationRelativeTo(null);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JTextField emailField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");

        addComponents(panel, "Email:", emailField, 0);
        addComponents(panel, "Password:", passField, 1);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 2; gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(loginBtn, gbc);

        // For login: user, fullName, and contact are null
        loginBtn.addActionListener(e -> handleAuth(emailField.getText(), new String(passField.getPassword()), null, null, false));
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JTextField nameField = new JTextField(15); // Matches full_name
        JTextField emailField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JTextField contactField = new JTextField(15); // Matches contact_number
        JButton regBtn = new JButton("Register");

        addComponents(panel, "Full Name:", nameField, 0);
        addComponents(panel, "Email:", emailField, 1);
        addComponents(panel, "Password:", passField, 2);
        addComponents(panel, "Contact #:", contactField, 3);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 4; gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(regBtn, gbc);

        regBtn.addActionListener(e -> handleAuth(
            emailField.getText(), 
            new String(passField.getPassword()), 
            nameField.getText(), 
            contactField.getText(), 
            true
        ));
        return panel;
    }

    private void addComponents(JPanel panel, String label, JComponent field, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void handleAuth(String email, String pass, String fullName, String contact, boolean isReg) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Modified to send full MySQL data
                return apiService.sendAuthRequest(email, pass, fullName, contact, isReg);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        if (isReg) {
                            JOptionPane.showMessageDialog(null, "Account Created! Please Login.");
                        } else {
                            JOptionPane.showMessageDialog(null, "Login Successful!");
                            // Transition to the Social Catalog Tabs
                            new CatalogFrame().setVisible(true);
                            dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed. Check your details.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }
}