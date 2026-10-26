package com.example.nulsf.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Admin messaging interface - pure Java/Swing, no JavaScript.
 * Connects to REST API for admin-user messaging.
 */
public class MessagesFrame extends JFrame {
    private static final String API_BASE = "http://localhost:8080/api";
    
    private JTextField adminIdField;
    private JList<String> conversationList;
    private DefaultListModel<String> conversationModel;
    private JTextArea messagesArea;
    private JTextField messageInput;
    private JButton sendButton;
    private JLabel otherUserLabel;
    
    private Long currentAdminId;
    private Long selectedConversationId;
    private List<ConversationData> conversations = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public MessagesFrame() {
        setTitle("Admin Messages - Lost & Found System");
        setSize(900, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel: Admin ID input
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Admin User ID:"));
        adminIdField = new JTextField("1", 10);
        topPanel.add(adminIdField);
        JButton loadButton = new JButton("Load Conversations");
        loadButton.addActionListener(e -> loadConversations());
        topPanel.add(loadButton);
        JButton newConvButton = new JButton("New Conversation");
        newConvButton.addActionListener(e -> showNewConversationDialog());
        topPanel.add(newConvButton);
        add(topPanel, BorderLayout.NORTH);
        
        // Center: Split pane for conversations and messages
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        // Left: Conversation list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Conversations"));
        conversationModel = new DefaultListModel<>();
        conversationList = new JList<>(conversationModel);
        conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conversationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = conversationList.getSelectedIndex();
                if (index >= 0 && index < conversations.size()) {
                    selectConversation(conversations.get(index));
                }
            }
        });
        leftPanel.add(new JScrollPane(conversationList), BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);
        
        // Right: Messages panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Messages"));
        
        otherUserLabel = new JLabel("Select a conversation");
        otherUserLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        rightPanel.add(otherUserLabel, BorderLayout.NORTH);
        
        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        messagesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        rightPanel.add(new JScrollPane(messagesArea), BorderLayout.CENTER);
        
        // Bottom: Send message
        JPanel sendPanel = new JPanel(new BorderLayout(5, 5));
        sendPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messageInput = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        messageInput.addActionListener(e -> sendMessage());
        sendPanel.add(messageInput, BorderLayout.CENTER);
        sendPanel.add(sendButton, BorderLayout.EAST);
        rightPanel.add(sendPanel, BorderLayout.SOUTH);
        
        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadConversations() {
        try {
            String adminIdStr = adminIdField.getText().trim();
            if (adminIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Admin User ID", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentAdminId = Long.parseLong(adminIdStr);
            
            URL url = URI.create(API_BASE + "/conversations?userId=" + currentAdminId).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            int code = conn.getResponseCode();
            if (code == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    
                    List<Map<String, Object>> jsonArray = objectMapper.readValue(response.toString(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                    conversations.clear();
                    conversationModel.clear();
                    
                    for (Map<String, Object> obj : jsonArray) {
                        ConversationData conv = new ConversationData();
                        conv.conversationId = ((Number) obj.get("conversationId")).longValue();
                        conv.itemId = ((Number) obj.get("itemId")).intValue();
                        conv.otherParticipantName = (String) obj.get("otherParticipantName");
                        conversations.add(conv);
                        conversationModel.addElement(conv.otherParticipantName + " (Item #" + conv.itemId + ")");
                    }
                    
                    if (conversations.isEmpty()) {
                        conversationModel.addElement("No conversations yet");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load conversations. Code: " + code, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void selectConversation(ConversationData conv) {
        selectedConversationId = conv.conversationId;
        otherUserLabel.setText("Conversation with: " + conv.otherParticipantName + " (Item #" + conv.itemId + ")");
        
        try {
            URL url = URI.create(API_BASE + "/conversations/" + conv.conversationId + "/messages?userId=" + currentAdminId).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            int code = conn.getResponseCode();
            if (code == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    
                    List<Map<String, Object>> jsonArray = objectMapper.readValue(response.toString(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                    messagesArea.setText("");
                    
                    for (Map<String, Object> msg : jsonArray) {
                        Long senderId = ((Number) msg.get("senderId")).longValue();
                        String text = (String) msg.get("messageText");
                        String sentAt = msg.get("sentAt") != null ? msg.get("sentAt").toString() : "";
                        boolean isSent = senderId.equals(currentAdminId);
                        
                        String prefix = isSent ? "[You] " : "[" + conv.otherParticipantName + "] ";
                        messagesArea.append(prefix + text + "\n");
                        if (!sentAt.isEmpty()) {
                            messagesArea.append("  (" + sentAt + ")\n");
                        }
                        messagesArea.append("\n");
                    }
                    
                    messagesArea.setCaretPosition(messagesArea.getDocument().getLength());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading messages: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        if (selectedConversationId == null || currentAdminId == null) {
            JOptionPane.showMessageDialog(this, "Please select a conversation first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String text = messageInput.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        
        try {
            URL url = URI.create(API_BASE + "/conversations/" + selectedConversationId + "/messages").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
                Map<String, Object> json = Map.of(
                    "senderId", currentAdminId,
                    "messageText", text
                );
                
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(objectMapper.writeValueAsBytes(json));
                }
            
            int code = conn.getResponseCode();
            if (code == 200) {
                messageInput.setText("");
                int selectedIndex = conversationList.getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < conversations.size()) {
                    selectConversation(conversations.get(selectedIndex));
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }
                    JOptionPane.showMessageDialog(this, "Failed to send: " + error.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error sending message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showNewConversationDialog() {
        JDialog dialog = new JDialog(this, "New Conversation", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1;
        JTextField itemIdField = new JTextField(15);
        panel.add(itemIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("User ID (to message):"), gbc);
        gbc.gridx = 1;
        JTextField userIdField = new JTextField(15);
        panel.add(userIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            try {
                int itemId = Integer.parseInt(itemIdField.getText().trim());
                long userId = Long.parseLong(userIdField.getText().trim());
                long adminId = currentAdminId != null ? currentAdminId : Long.parseLong(adminIdField.getText().trim());
                
                URL url = URI.create(API_BASE + "/conversations").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                Map<String, Object> json = Map.of(
                    "itemId", itemId,
                    "currentUserId", adminId,
                    "otherParticipantId", userId
                );
                
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(objectMapper.writeValueAsBytes(json));
                }
                
                int code = conn.getResponseCode();
                if (code == 200) {
                    dialog.dispose();
                    loadConversations();
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder error = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            error.append(line);
                        }
                        JOptionPane.showMessageDialog(dialog, "Failed: " + error.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(createButton, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private static class ConversationData {
        Long conversationId;
        Integer itemId;
        String otherParticipantName;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MessagesFrame frame = new MessagesFrame();
            frame.setVisible(true);
        });
    }
}
