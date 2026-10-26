package com.example.nulsf.swing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

public class AdminDashboard extends JFrame {
    private JdbcTemplate jdbcTemplate;
    private JTable claimsTable, surrenderTable;
    private DefaultTableModel claimsModel, surrenderModel;
    private JLabel statsLabel;
    private JPanel catalogGrid;

    public AdminDashboard(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        setTitle("NU-LSF Admin Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: CLAIM REQUESTS ---
        JPanel claimsPanel = new JPanel(new BorderLayout(10, 10));
        String[] claimCols = {"Claim ID", "Item ID", "Item Name", "Requester", "Claimer Email", "Date", "Status"};
        claimsModel = new DefaultTableModel(claimCols, 0);
        claimsTable = new JTable(claimsModel);
        claimsPanel.add(new JScrollPane(claimsTable), BorderLayout.CENTER);
        
        JPanel claimActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApprove = createStyledButton("Approve Claim", new Color(46, 204, 113));
        JButton btnReject = createStyledButton("Reject", new Color(231, 76, 60));
        claimActions.add(btnReject);
        claimActions.add(btnApprove);
        claimsPanel.add(claimActions, BorderLayout.SOUTH);
        
        // --- TAB 2: PENDING INTAKE ---
        JPanel surrenderPanel = new JPanel(new BorderLayout(10, 10));
        String[] surrenderCols = {"Item ID", "Item Name", "Location", "Reporter", "Reporter Email", "Action Required"};
        surrenderModel = new DefaultTableModel(surrenderCols, 0);
        surrenderTable = new JTable(surrenderModel);
        surrenderPanel.add(new JScrollPane(surrenderTable), BorderLayout.CENTER);

        JPanel surrenderActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnReceive = createStyledButton("Approve / Mark as Received", new Color(52, 152, 219));
        surrenderActions.add(btnReceive);
        surrenderPanel.add(surrenderActions, BorderLayout.SOUTH);

        // --- TAB 3: VISUAL CATALOG ---
        catalogGrid = new JPanel(new GridLayout(0, 3, 15, 15));
        catalogGrid.setBackground(new Color(236, 240, 241));
        JScrollPane catalogScroll = new JScrollPane(catalogGrid);
        catalogScroll.getVerticalScrollBar().setUnitIncrement(16);

        tabbedPane.addTab("Claim Requests", claimsPanel);
        tabbedPane.addTab("Pending Intake", surrenderPanel);
        tabbedPane.addTab("System Catalog View", catalogScroll);
        
        add(tabbedPane, BorderLayout.CENTER);

        setupHeader();
        setupFooter();

        btnApprove.addActionListener(e -> handleClaimAction("APPROVED"));
        btnReject.addActionListener(e -> handleClaimAction("REJECTED"));
        btnReceive.addActionListener(e -> handleSurrenderAction());

        refreshData();
    }

    private void refreshData() {
        claimsModel.setRowCount(0);
        String claimSql = "SELECT c.claim_id, c.item_id, i.item_name, u.full_name, u.email, c.claim_date, c.claim_status " +
                         "FROM claims c JOIN items i ON c.item_id = i.item_id " +
                         "JOIN users u ON c.claimer_id = u.user_id WHERE c.claim_status = 'PENDING'";
        try {
            List<Map<String, Object>> claims = jdbcTemplate.queryForList(claimSql);
            for (Map<String, Object> row : claims) {
                claimsModel.addRow(new Object[]{
                    row.get("claim_id"), row.get("item_id"), row.get("item_name"), 
                    row.get("full_name"), row.get("email"), row.get("claim_date"), row.get("claim_status")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }

        surrenderModel.setRowCount(0);
        String intakeSql = "SELECT i.item_id, i.item_name, i.location, i.status, u.full_name, u.email " +
                           "FROM items i LEFT JOIN users u ON i.reported_by = u.user_id " +
                           "WHERE i.status IN ('PENDING_SURRENDER', 'PENDING_CLAIM')";
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(intakeSql);
            for (Map<String, Object> row : results) {
                String displayStatus = "PENDING_SURRENDER".equals(row.get("status")) ? "FOUND (Needs Surrender)" : "LOST (Needs Approval)";
                surrenderModel.addRow(new Object[]{
                    row.get("item_id"), row.get("item_name"), row.get("location"), 
                    row.get("full_name"), row.get("email"), displayStatus
                });
            }
            statsLabel.setText(" Pending Items: " + results.size());
        } catch (Exception e) { e.printStackTrace(); }

        refreshCatalogView();
    }

    private void refreshCatalogView() {
        catalogGrid.removeAll();
        // Added 'description' to the query so we can display it when clicked
        String sql = "SELECT i.item_name, i.status, i.image_path, i.location, i.description, u.email " +
                     "FROM items i LEFT JOIN users u ON i.reported_by = u.user_id ORDER BY i.item_id DESC";
        try {
            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> item : items) {
                catalogGrid.add(createItemCard(item));
            }
        } catch (Exception e) { e.printStackTrace(); }
        catalogGrid.revalidate();
        catalogGrid.repaint();
    }

    private JPanel createItemCard(Map<String, Object> data) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Shows hand icon on hover

        String imgPath = (String) data.get("image_path");
        JLabel imgLabel = new JLabel("No Image", SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(150, 150));
        
        if (imgPath != null && !imgPath.isEmpty()) {
            try {
                String filename = imgPath.substring(imgPath.lastIndexOf("/") + 1);
                File file = new File("C:/NULSFimage/" + filename);
                if (file.exists()) {
                    Image rawImage = ImageIO.read(file);
                    Image scaled = rawImage.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    imgLabel.setIcon(new ImageIcon(scaled));
                    imgLabel.setText("");
                }
            } catch (Exception e) { imgLabel.setText("Load Error"); }
        }

        // Mouse Listener for Click Event
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showItemDetails(data);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            }
        });

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setBackground(Color.WHITE);
        JLabel name = new JLabel(" " + data.get("item_name").toString().toUpperCase());
        name.setFont(new Font("Arial", Font.BOLD, 13));
        
        JLabel statusLabelItem = new JLabel(" Status: " + data.get("status"));
        statusLabelItem.setForeground("AVAILABLE".equals(data.get("status")) ? new Color(39, 174, 96) : Color.GRAY);
        
        JLabel clickHint = new JLabel(" (Click to view details) ");
        clickHint.setFont(new Font("Arial", Font.ITALIC, 10));
        clickHint.setForeground(Color.LIGHT_GRAY);

        info.add(name);
        info.add(statusLabelItem);
        info.add(clickHint);

        card.add(imgLabel, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);
        return card;
    }

    private void showItemDetails(Map<String, Object> data) {
        String details = String.format(
            "Item: %s\nStatus: %s\nLocation: %s\nReporter: %s\n\nDescription:\n%s",
            data.get("item_name"),
            data.get("status"),
            data.get("location"),
            data.get("email") != null ? data.get("email") : "Unknown",
            data.get("description") != null ? data.get("description") : "No description provided."
        );
        
        JTextArea textArea = new JTextArea(details);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Item Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // ... (Keep handleSurrenderAction, handleClaimAction, createStyledButton, setupHeader, setupFooter unchanged)

    private void handleSurrenderAction() {
        int row = surrenderTable.getSelectedRow();
        if (row == -1) return;
        Object id = surrenderModel.getValueAt(row, 0);
        jdbcTemplate.update("UPDATE items SET status = 'AVAILABLE' WHERE item_id = ?", id);
        JOptionPane.showMessageDialog(this, "Item updated to AVAILABLE.");
        refreshData();
    }

    private void handleClaimAction(String newStatus) {
        int row = claimsTable.getSelectedRow();
        if (row == -1) return;
        Object claimId = claimsModel.getValueAt(row, 0);
        Object itemId = claimsModel.getValueAt(row, 1);

        try {
            jdbcTemplate.update("UPDATE claims SET claim_status = ? WHERE claim_id = ?", newStatus, claimId);
            if ("APPROVED".equals(newStatus)) {
                jdbcTemplate.update("UPDATE items SET status = 'CLAIMED' WHERE item_id = ?", itemId);
            }
            refreshData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    private void setupHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(41, 128, 185));
        JLabel title = new JLabel(" NU-LSF Admin Management Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);
        add(header, BorderLayout.NORTH);
    }

    private void setupFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        statsLabel = new JLabel(" System Ready");
        JButton btnRefresh = new JButton("Refresh All");
        btnRefresh.addActionListener(e -> refreshData());
        footer.add(statsLabel, BorderLayout.WEST);
        footer.add(btnRefresh, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }
}