package com.chat.client.view;

import com.chat.client.controller.ChatClientController;
import com.chat.client.model.ChatMessage; // Import ChatMessage
import com.chat.client.model.ChatClientModel; // Import ChatClientModel untuk konstanta

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ChatClientView extends JFrame implements PropertyChangeListener {
    private JTextPane messagePane;
    private JTextField textField;
    private JButton sendButton;
    private ChatClientController controller; // Controller di-inject

    public ChatClientView() {
        super("Chat Klien");
        initializeUI();
    }
    
    public void setController(ChatClientController controller) {
        this.controller = controller;
    }

    private void initializeUI() {
        messagePane = new JTextPane();
        messagePane.setEditable(false);
        messagePane.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(messagePane);
        scrollPane.setPreferredSize(new Dimension(580, 300));

        textField = new JTextField(35);
        sendButton = new JButton("Kirim");
        sendButton.setPreferredSize(new Dimension(80, 25));

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        southPanel.add(new JLabel("Pesan:"));
        southPanel.add(textField);
        southPanel.add(sendButton);

        Runnable sendMessageAction = () -> {
            if (controller != null) {
                String messageText = textField.getText();
                if (!messageText.trim().isEmpty()) {
                    controller.sendMessageRequested(messageText);
                    textField.setText("");
                }
            }
        };
        sendButton.addActionListener(e -> sendMessageAction.run());
        textField.addActionListener(e -> sendMessageAction.run());

        setLayout(new BorderLayout(5, 5));
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (controller != null) {
                    controller.disconnectRequested();
                }
                dispose();
                System.exit(0);
            }
        });

        pack();
        setMinimumSize(new Dimension(400, 300));
        setLocationRelativeTo(null);
    }

    public void updateMessages(List<ChatMessage> messages) {
        SwingUtilities.invokeLater(() -> {
            messagePane.setText("");
            for (ChatMessage msg : messages) {
                appendSingleChatMessage(msg);
            }
        });
    }
    
    public void appendSingleChatMessage(ChatMessage msg) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = messagePane.getStyledDocument();
            SimpleAttributeSet senderAttrs = new SimpleAttributeSet();
            SimpleAttributeSet contentAttrs = new SimpleAttributeSet();
            SimpleAttributeSet timestampAttrs = new SimpleAttributeSet();
            SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();

            StyleConstants.setForeground(timestampAttrs, Color.GRAY);
            StyleConstants.setFontSize(timestampAttrs, 10);

            String senderDisplayName = msg.getSender();
            String formattedTimestamp = msg.getFormattedTimestamp() + " ";

            // Default content color
            StyleConstants.setForeground(contentAttrs, Color.BLACK);

            switch (msg.getType()) {
                case SELF_MESSAGE:
                    StyleConstants.setForeground(senderAttrs, new Color(0, 128, 0));
                    StyleConstants.setBold(senderAttrs, true);
                    StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_RIGHT);
                    senderDisplayName = "Anda";
                    break;
                case USER_MESSAGE:
                    StyleConstants.setForeground(senderAttrs, new Color(0, 0, 205));
                    StyleConstants.setBold(senderAttrs, true);
                    StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_LEFT);
                    break;
                case SYSTEM_MESSAGE:
                    StyleConstants.setForeground(contentAttrs, Color.DARK_GRAY); // Pesan sistem di content
                    StyleConstants.setItalic(contentAttrs, true);
                    StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_CENTER);
                    senderDisplayName = "";
                    formattedTimestamp = "";
                    break;
                case ERROR_MESSAGE:
                    StyleConstants.setForeground(contentAttrs, Color.RED);
                    StyleConstants.setBold(contentAttrs, true);
                    StyleConstants.setItalic(contentAttrs, true);
                    StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_CENTER);
                    senderDisplayName = ""; // Error message content already describes it
                    formattedTimestamp = "";
                    break;
                case SERVER_INFO:
                    StyleConstants.setForeground(contentAttrs, new Color(255, 140, 0));
                    StyleConstants.setItalic(contentAttrs, true);
                    StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_CENTER);
                    senderDisplayName = ""; // Server info content already describes it
                    formattedTimestamp = "";
                    break;
                default:
                    StyleConstants.setAlignment(paragraphAttrs, StyleConstants.ALIGN_LEFT);
            }

            try {
                int startOffset = doc.getLength();
                if (!formattedTimestamp.isEmpty()) {
                    doc.insertString(doc.getLength(), formattedTimestamp, timestampAttrs);
                }
                if (!senderDisplayName.isEmpty()) {
                     doc.insertString(doc.getLength(), senderDisplayName + ": ", senderAttrs);
                }
                doc.insertString(doc.getLength(), msg.getContent() + "\n", contentAttrs);
                doc.setParagraphAttributes(startOffset, doc.getLength() - startOffset, paragraphAttrs, false);
                messagePane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            String propertyName = evt.getPropertyName();
            if (ChatClientModel.NEW_MESSAGE_PROPERTY.equals(propertyName)) {
                if (evt.getNewValue() instanceof ChatMessage) {
                    appendSingleChatMessage((ChatMessage) evt.getNewValue());
                }
            } else if (ChatClientModel.CONNECTION_STATUS_PROPERTY.equals(propertyName)) {
                boolean connected = (boolean) evt.getNewValue();
                setInteractionEnabled(connected);
                // Pesan status koneksi (connected/disconnected) akan datang sebagai ChatMessage dari Model
            } else if (ChatClientModel.ALL_MESSAGES_PROPERTY.equals(propertyName)) {
                if (evt.getNewValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<ChatMessage> allMessages = (List<ChatMessage>) evt.getNewValue();
                    updateMessages(allMessages);
                }
            }
        });
    }
    
    public void setInteractionEnabled(boolean enabled) {
        textField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        if (enabled) {
            textField.requestFocusInWindow();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Tidak dapat mengatur Look and Feel sistem: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            com.chat.client.model.ChatClientModel model = new com.chat.client.model.ChatClientModel();
            com.chat.client.controller.ChatClientController controller = new com.chat.client.controller.ChatClientController(model);
            ChatClientView view = new ChatClientView(); // View ada di package ini sendiri
            
            view.setController(controller);
            controller.setView(view);
            
            view.setVisible(true);
            controller.startApplication();
        });
    }
}
