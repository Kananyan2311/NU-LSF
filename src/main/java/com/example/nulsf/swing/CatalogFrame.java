package com.example.nulsf.swing;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URL;

public class CatalogFrame extends JFrame {
    public CatalogFrame() {
        setTitle("Item Catalog");
        setSize(450, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Lost Items", createFeedPanel("LOST"));
        tabs.addTab("Found Items", createFeedPanel("FOUND"));

        add(tabs);
        setLocationRelativeTo(null);
    }

    private JScrollPane createFeedPanel(String type) {
        JPanel feed = new JPanel();
        feed.setLayout(new BoxLayout(feed, BoxLayout.Y_AXIS));
        feed.setBackground(new Color(240, 242, 245)); // Social media light gray bg

        // Simulate items from DB
        for (int i = 1; i <= 5; i++) {
            feed.add(createItemCard(type + " Item #" + i, "This is a description of the " + type.toLowerCase() + " item.", "Contact: 09123...", null));
            feed.add(Box.createRigidArea(new Dimension(0, 10))); // Social feed gap
        }

        return new JScrollPane(feed);
    }

    private JPanel createItemCard(String title, String desc, String meta, String imagePath) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setMaximumSize(new Dimension(400, 350));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.setBackground(Color.WHITE);

        if (imagePath != null && !imagePath.isBlank()) {
            try {
                URL imgUrl = URI.create("http://localhost:8080/uploads/" + imagePath).toURL();
                ImageIcon icon = new ImageIcon(imgUrl);
                if (icon.getImage() != null) {
                    Image img = icon.getImage().getScaledInstance(360, 120, Image.SCALE_SMOOTH);
                    JLabel imgLabel = new JLabel(new ImageIcon(img));
                    center.add(imgLabel, BorderLayout.NORTH);
                }
            } catch (Exception ignored) { /* no image if URL fails */ }
        }

        JTextArea descArea = new JTextArea(desc);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBackground(Color.WHITE);
        center.add(descArea, BorderLayout.CENTER);

        JLabel metaLbl = new JLabel(meta);
        metaLbl.setForeground(Color.GRAY);
        metaLbl.setFont(new Font("SansSerif", Font.ITALIC, 12));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(metaLbl, BorderLayout.SOUTH);

        return card;
    }
    public static void main(String[] args) {
        CatalogFrame catalogFrame = new CatalogFrame();
        catalogFrame.setVisible(true);
    }
}

