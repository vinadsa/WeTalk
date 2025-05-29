import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.sql.SQLException;

// Import model and controller classes
import com.chat.client.model.ChatClientModel;
import com.chat.client.model.DatabaseAuthenticator;
import com.chat.client.model.User;
import com.chat.client.controller.ChatClientController;
// Tambahkan import untuk styling teks
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.border.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatClientGUI extends JFrame {
    // Ganti JTextArea dengan JTextPane
    private JTextPane messagePane;
    private JTextField textField;
    private JButton sendButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String username;
    private String userPassword;
    private boolean isConnected = false;
    private JLabel statusLabel;
    private DatabaseAuthenticator dbAuth;
    private User currentUser;
    private ChatClientController controller;
    private ChatClientModel model;
    // Color scheme for the application
    private static final Color PRIMARY_COLOR = new Color(100, 149, 237); // Cornflower blue
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); // Alice blue
    private static final Color ACCENT_COLOR = new Color(65, 105, 225); // Royal blue
    private static final Color SELF_MESSAGE_COLOR = new Color(220, 248, 220); // Light green
    private static final Color OTHER_MESSAGE_COLOR = new Color(240, 240, 255); // Light blue
    private static final Color SYSTEM_MESSAGE_COLOR = new Color(255, 245, 230); // Light orange
    
    private static final Font APP_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    private static final String SERVER_ADDRESS = "129.150.37.67";
    private static final int SERVER_PORT = 1234;

    // Custom rounded button class
    private class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setBackground(PRIMARY_COLOR);
            setForeground(Color.WHITE);
            setFont(APP_FONT.deriveFont(Font.BOLD));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Create gradient 
            GradientPaint gp = new GradientPaint(
                0, 0, PRIMARY_COLOR, 
                0, getHeight(), ACCENT_COLOR
            );
            g2.setPaint(gp);
            
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(new Color(0, 0, 0, 50));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            
            super.paintComponent(g2);
            g2.dispose();
        }
    }
    
    // Custom styled text field
    private class StyledTextField extends JTextField {
        public StyledTextField(int columns) {
            super(columns);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            setFont(APP_FONT);
            setSelectionColor(PRIMARY_COLOR);
            setCaretColor(ACCENT_COLOR);
        }
    }
    
    public ChatClientGUI() {
        super("WeTalk Chat Client");
        
        // Set the look and feel to system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize model and controller
        this.model = new ChatClientModel();
        this.controller = new ChatClientController(model);
        
        // Initialize database authenticator
        this.dbAuth = DatabaseAuthenticator.getInstance();
        
        // Show authentication dialog first, before initializing the UI
        if (!authenticateUser()) {
            // If authentication fails or is cancelled, exit the application
            System.exit(0);
        }
        
        // Set title with username after successful authentication
        setTitle("WeTalk Chat Client - " + this.username);

        // Setup UI dengan JTextPane
        messagePane = new JTextPane();
        messagePane.setEditable(false);
        messagePane.setFont(APP_FONT);
        messagePane.setBackground(SECONDARY_COLOR);
        messagePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Customized scroll pane
        // Class for modern scroll bar UI
        class ModernScrollBarUI extends BasicScrollBarUI {
            private Color thumbColor = new Color(180, 180, 180);
            private Color trackColor = new Color(240, 240, 240);
            
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 180, 180);
                this.trackColor = new Color(240, 240, 240);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fill rounded rectangle for thumb
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, 
                                 thumbBounds.width, thumbBounds.height, 
                                 10, 10);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        }
        
        // Customized scroll pane
        JScrollPane scrollPane = new JScrollPane(messagePane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        textField = new StyledTextField(40);
        sendButton = new RoundedButton("Kirim");
        
        // Add padding to button
        sendButton.setMargin(new Insets(8, 15, 8, 15));
        
        // Input panel with modern styling
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout(10, 0));
        southPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        southPanel.setBackground(Color.WHITE);
        southPanel.add(textField, BorderLayout.CENTER);
        southPanel.add(sendButton, BorderLayout.EAST);

        // Main layout with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Subtle gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, Color.WHITE,
                    0, getHeight(), new Color(245, 247, 250)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Add header with username
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel userLabel = new JLabel("Logged in as: " + this.username);
        userLabel.setFont(HEADER_FONT);
        userLabel.setForeground(Color.WHITE);
        headerPanel.add(userLabel, BorderLayout.WEST);
        
        // Add status label
        statusLabel = new JLabel("Disconnected");
        statusLabel.setFont(APP_FONT);
        statusLabel.setForeground(Color.WHITE);
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel);
        headerPanel.add(statusPanel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        // Add shadow border to the main panel
        Border lineBorder = BorderFactory.createLineBorder(new Color(220, 220, 220), 1);
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));
        
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        ActionListener sendListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                if (message == null || message.trim().isEmpty()) {
                    // Don't do anything for empty messages
                    return;
                }
                
                if (isConnected && out != null) {
                    // Check if writer has error
                    if (out.checkError()) {
                        showErrorMessage("Connection error! Unable to send message.");
                        updateConnectionStatus(false, "Error");
                        displayReconnectOption();
                        return;
                    }
                    
                    // Send message
                    out.println(message); 
                    textField.setText("");
                } else if (!isConnected) {
                    JOptionPane.showMessageDialog(
                        ChatClientGUI.this, 
                        "Not connected to server. Would you like to reconnect?",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                    
                    // Ask to reconnect
                    int option = JOptionPane.showConfirmDialog(
                        ChatClientGUI.this,
                        "Would you like to reconnect to the server?",
                        "Reconnect",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (option == JOptionPane.YES_OPTION) {
                        connectToServer();
                    }
                }
            }
        };
        // Set up window closing to properly clean up resources
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Clean up resources before exit
                cleanupConnection();
                
                // Close database connection
                if (dbAuth != null) {
                    dbAuth.close();
                    System.out.println("Database connection closed.");
                }
                
                dispose();
                System.exit(0);
            }
        });
        textField.addActionListener(sendListener);
        sendButton.addActionListener(sendListener);
        setSize(600, 500); // Atur ukuran default yang lebih representatif
        setMinimumSize(new Dimension(400, 300));
        setLocationRelativeTo(null);
        
        // Set icon image if available
        try {
            // You can add your own icon file later
            // setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        } catch (Exception e) {
            // Ignore if icon not found
        }
        
        setVisible(true);

        // Connect to server
        connectToServer();
    }
    
    /**
     * Method to connect to chat server
     */
    private void connectToServer() {
        // Clean up any existing connection
        cleanupConnection();
        
        // Update UI for connection attempt
        // Disable input while connecting
        textField.setEnabled(false);
        sendButton.setEnabled(false);
        
        updateConnectionStatus(false, "Connecting...");
        appendToPane("SYSTEM", "Connecting to server...", SYSTEM_MESSAGE_COLOR, StyleConstants.ALIGN_CENTER, true, true);
        
        // Run connection in background thread to not freeze UI
        Thread connectionThread = new Thread(() -> {
            try {
                // Attempt to create socket connection
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Check that PrintWriter was initialized properly
                if (out == null || out.checkError()) {
                    throw new IOException("Failed to initialize PrintWriter");
                }
                
                // Connection successful, update status flag
                isConnected = true;
                
                // Send username to server
                out.println(this.username); // Kirim username ke server
                out.println(this.userPassword); // Kirim password ke server
                
                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    updateConnectionStatus(true, "Connected");
                    textField.setEnabled(true);
                    sendButton.setEnabled(true);
                    appendToPane("SYSTEM", "Connected to server successfully", new Color(100, 200, 100), StyleConstants.ALIGN_CENTER, true, true);
                });
                
                // Start reading incoming messages
                Thread readerThread = new Thread(new IncomingReader());
                readerThread.setDaemon(true); // Make thread daemon so it doesn't prevent app exit
                readerThread.start();
                
            } catch (UnknownHostException e) {
                // Handle host not found
                SwingUtilities.invokeLater(() -> {
                    updateConnectionStatus(false, "Disconnected");
                    appendToPane("SYSTEM", "Server tidak ditemukan: " + e.getMessage(), new Color(255, 100, 100), StyleConstants.ALIGN_CENTER, true, true);
                    displayReconnectOption();
                });
            } catch (ConnectException e) {
                // Specific handling for connection refused
                SwingUtilities.invokeLater(() -> {
                    updateConnectionStatus(false, "Failed");
                    appendToPane("SYSTEM", "Koneksi ditolak. Server mungkin tidak aktif.", new Color(255, 100, 100), StyleConstants.ALIGN_CENTER, true, true);
                    displayReconnectOption();
                });
            } catch (IOException e) {
                // Handle general I/O errors
                SwingUtilities.invokeLater(() -> {
                    updateConnectionStatus(false, "Error");
                    appendToPane("SYSTEM", "Error I/O: " + e.getMessage(), new Color(255, 100, 100), StyleConstants.ALIGN_CENTER, true, true);
                    displayReconnectOption();
                });
            }
        });
        connectionThread.setDaemon(true); // Make thread daemon so it doesn't prevent app exit
        connectionThread.start();
    }
    
    /**
     * Updates the connection status UI
     */
    private void updateConnectionStatus(boolean connected, String statusText) {
        // Only change isConnected flag if we're on the EDT, otherwise it should be set directly
        if (SwingUtilities.isEventDispatchThread()) {
            isConnected = connected;
        }
        
        if (connected) {
            statusLabel.setText(statusText);
            statusLabel.setForeground(new Color(150, 255, 150));
            textField.setEnabled(true);
            sendButton.setEnabled(true);
        } else {
            statusLabel.setText(statusText);
            statusLabel.setForeground(new Color(255, 150, 150));
            textField.setEnabled(false);
            sendButton.setEnabled(false);
        }
    }
    
    /**
     * Cleans up the connection resources
     */
    private void cleanupConnection() {
        // Set connection status first to prevent ongoing operations
        // Set connection status flag first to prevent ongoing operations from using resources
        boolean wasConnected = isConnected;
        isConnected = false;
        
        // If nothing to clean up, exit early
        if (socket == null && in == null && out == null) {
            return;
        }
        
        // Guard with try-catch blocks individually to ensure all cleanup happens
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
        
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing input stream: " + e.getMessage());
        }
        
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            System.out.println("Error closing output stream: " + e.getMessage());
        }
        
        // Set all connection objects to null
        socket = null;
        in = null;
        out = null;
        
        // Update UI if needed (in case this wasn't called from the EDT)
        SwingUtilities.invokeLater(() -> {
            updateConnectionStatus(false, "Disconnected");
        });
    }
    
    /**
     * Shows reconnect dialog
     */
    private void displayReconnectOption() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Would you like to reconnect to the server?",
            "Connection Failed",
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            // Set a small delay before reconnecting to avoid rapid reconnection attempts
            new Timer(1000, e -> {
                ((Timer)e.getSource()).stop();
                connectToServer();
            }).start();
        } else {
            appendToPane("SYSTEM", "Operating in offline mode. Reconnect manually when server is available.", 
                         SYSTEM_MESSAGE_COLOR, StyleConstants.ALIGN_CENTER, true, true);
            
            // Add a reconnect button to the bottom of the chat
            JButton reconnectBtn = createReconnectButton();
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnPanel.setOpaque(false);
            btnPanel.add(reconnectBtn);
            
            messagePane.setCaretPosition(messagePane.getDocument().getLength());
            messagePane.insertComponent(btnPanel);
            try {
                messagePane.getDocument().insertString(
                    messagePane.getDocument().getLength(), "\n", null);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Shows a visual error message in the chat
     */
    private void showErrorMessage(String message) {
        appendToPane("ERROR", message, new Color(255, 200, 200), 
                     StyleConstants.ALIGN_CENTER, true, true);
    }
    
    // Custom chat bubble panel for messages
    private class ChatBubblePanel extends JPanel {
        private final Color bubbleColor;
        private final String senderName;
        private final String messageText;
        private final boolean isRightAligned;
        private final boolean isSystemMessage;
        
        public ChatBubblePanel(String sender, String message, Color color, boolean rightAlign, boolean system) {
            this.bubbleColor = color;
            this.senderName = sender;
            this.messageText = message;
            this.isRightAligned = rightAlign;
            this.isSystemMessage = system;
            
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setLayout(new BorderLayout());
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Content area
            int width = getWidth() - 20; // Bubble padding
            int height = getHeight() - 10;
            int x = isRightAligned ? getWidth() - width - 10 : 10;
            int y = 5;
            
            if (isSystemMessage) {
                // System message - subtle background, centered
                g2.setColor(new Color(bubbleColor.getRed(), bubbleColor.getGreen(), bubbleColor.getBlue(), 40));
                g2.fillRoundRect(10, y, getWidth() - 20, height, 10, 10);
                g2.setColor(new Color(bubbleColor.getRed(), bubbleColor.getGreen(), bubbleColor.getBlue(), 90));
                g2.drawRoundRect(10, y, getWidth() - 20, height, 10, 10);
            } else {
                // Regular chat bubble
                g2.setColor(bubbleColor);
                g2.fillRoundRect(x, y, width, height, 15, 15);
                
                // Little triangle for chat bubble
                int triangleSize = 8;
                int triangleX = isRightAligned ? x : x + width;
                int[] xPoints = new int[3];
                int[] yPoints = new int[3];
                
                if (isRightAligned) {
                    xPoints[0] = triangleX;
                    xPoints[1] = triangleX + triangleSize;
                    xPoints[2] = triangleX;
                } else {
                    xPoints[0] = triangleX;
                    xPoints[1] = triangleX - triangleSize;
                    xPoints[2] = triangleX;
                }
                
                yPoints[0] = y + 15;
                yPoints[1] = y + 20;
                yPoints[2] = y + 25;
                
                g2.fillPolygon(xPoints, yPoints, 3);
            }
            
            g2.dispose();
        }
    }
    
    // Metode untuk menambahkan teks dengan style ke JTextPane
    private void appendToPane(String sender, String message, Color bubbleColor, int alignment, boolean senderBold, boolean isSystemMessage) {
        SwingUtilities.invokeLater(() -> { // Pastikan update UI di EDT
            // Create a bubble panel for the message
            JPanel bubblePanel = new ChatBubblePanel(sender, message, bubbleColor, 
                    alignment == StyleConstants.ALIGN_RIGHT, isSystemMessage);
            bubblePanel.setLayout(new BorderLayout(5, 5));
            
            // Create sender label
            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(APP_FONT.deriveFont(senderBold ? Font.BOLD : Font.PLAIN));
            senderLabel.setForeground(isSystemMessage ? new Color(100, 100, 100) : 
                    new Color(bubbleColor.getRed() - 40, bubbleColor.getGreen() - 40, bubbleColor.getBlue() - 40));
            
            // Create message label with word wrap
            JTextArea messageArea = new JTextArea(message);
            messageArea.setFont(APP_FONT);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setEditable(false);
            messageArea.setOpaque(false);
            messageArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            if (isSystemMessage) {
                messageArea.setFont(APP_FONT.deriveFont(Font.ITALIC));
                messageArea.setForeground(new Color(90, 90, 90));
            } else {
                messageArea.setForeground(Color.BLACK);
            }
            
            // Add components to bubble panel
            JPanel contentPanel = new JPanel(new BorderLayout(2, 2));
            contentPanel.setOpaque(false);
            contentPanel.add(senderLabel, BorderLayout.NORTH);
            contentPanel.add(messageArea, BorderLayout.CENTER);
            
            if (alignment == StyleConstants.ALIGN_RIGHT) {
                bubblePanel.add(contentPanel, BorderLayout.EAST);
            } else if (alignment == StyleConstants.ALIGN_CENTER) {
                bubblePanel.add(contentPanel, BorderLayout.CENTER);
            } else {
                bubblePanel.add(contentPanel, BorderLayout.WEST);
            }
            
            // Insert the panel into the text pane
            messagePane.setCaretPosition(messagePane.getDocument().getLength());
            messagePane.insertComponent(bubblePanel);
            
            // Add a newline after the component
            try {
                messagePane.getDocument().insertString(
                    messagePane.getDocument().getLength(), "\n", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            
            // Auto-scroll to the bottom
            messagePane.setCaretPosition(messagePane.getDocument().getLength());
        });
    }

    private class IncomingReader implements Runnable {
        public void run() {
            String lineFromServer;
            try {
                while ((lineFromServer = in.readLine()) != null) {
                    if (lineFromServer.startsWith("SYSTEM:")) {
                        String systemMsg = lineFromServer.substring("SYSTEM:".length()).trim();
                        appendToPane("SYSTEM", systemMsg, SYSTEM_MESSAGE_COLOR, StyleConstants.ALIGN_CENTER, true, true);
                    } else {
                        String[] parts = lineFromServer.split(":", 2);
                        if (parts.length == 2) {
                            String msgSender = parts[0].trim();
                            String msgContent = parts[1].trim();
                            if (msgSender.equals(username)) {
                                // Pesan dari diri sendiri
                                appendToPane(username, msgContent, SELF_MESSAGE_COLOR, StyleConstants.ALIGN_RIGHT, true, false); // Warna pesan sendiri, rata kanan
                            } else {
                                // Pesan dari orang lain
                                appendToPane(msgSender, msgContent, OTHER_MESSAGE_COLOR, StyleConstants.ALIGN_LEFT, true, false); // Warna pesan lain, rata kiri
                            }
                        } else {
                            // Pesan tidak terformat, tampilkan sebagai pesan sistem/info
                             appendToPane("SERVER", lineFromServer, SYSTEM_MESSAGE_COLOR, StyleConstants.ALIGN_CENTER, false, true);
                        }
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    if (socket != null && !socket.isClosed()) {
                        appendToPane("SYSTEM", "Koneksi ke server terputus: " + e.getMessage(), new Color(255, 100, 100), StyleConstants.ALIGN_CENTER, true, true);
                        updateConnectionStatus(false, "Disconnected");
                        displayReconnectOption();
                    }
                });
            } finally {
                // Clean up resources, but make sure we only do this once
                boolean wasConnected = isConnected;
                cleanupConnection();
                
                // Only show messages if we were actually connected before
                if (wasConnected) {
                    SwingUtilities.invokeLater(() -> {
                        textField.setEnabled(false);
                        sendButton.setEnabled(false);
                        appendToPane("SYSTEM", "Anda telah keluar dari chat.", SYSTEM_MESSAGE_COLOR, StyleConstants.ALIGN_CENTER, true, true);
                    });
                }
            }
        }
    }

    /**
     * Handles user authentication (login/registration)
     * 
     * @return true if authentication was successful, false otherwise
     */
    private boolean authenticateUser() {
        try {
            // Test database connection first
            if (!dbAuth.testConnection()) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to connect to database. Please check your database configuration.",
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Create users table if it doesn't exist
            dbAuth.createUsersTableIfNotExists();
            
            // Show login/registration dialog
            return showAuthDialog();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Show authentication dialog with login and registration options
     * 
     * @return true if authentication succeeded, false otherwise
     */
    private boolean showAuthDialog() {
        // Create option buttons
        String[] options = {"Login", "Register", "Cancel"};
        
        int choice = JOptionPane.showOptionDialog(this,
            "Welcome to WeTalk Chat. Please select an option:",
            "WeTalk Authentication",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
            
        switch (choice) {
            case 0: // Login
                return showLoginDialog();
            case 1: // Register
                return showRegistrationDialog();
            default: // Cancel or closed
                return false;
        }
    }
    
    /**
     * Show login dialog and authenticate user
     * 
     * @return true if login succeeded, false otherwise
     */
    private boolean showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Login", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Username and password cannot be empty",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
                return showLoginDialog(); // Show dialog again
            }
            
            try {
                if (dbAuth.validateUser(username, password)) {
                    // Login successful
                    this.currentUser = dbAuth.getUserByUsername(username);
                    this.username = username; // Set the username for the chat
                    this.userPassword = password; 
                    return true;
                } else {
                    // Login failed
                    JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                    return showLoginDialog(); // Show dialog again
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return false; // Cancelled
    }
    
    /**
     * Show registration dialog and register new user
     * 
     * @return true if registration succeeded, false otherwise
     */
    private boolean showRegistrationDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);
        
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPasswordField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Register", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            // Validate input
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Username and password cannot be empty",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
                return showRegistrationDialog(); // Show dialog again
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                    "Passwords do not match",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
                return showRegistrationDialog(); // Show dialog again
            }
            
            try {
                // Check if username already exists
                if (dbAuth.usernameExists(username)) {
                    JOptionPane.showMessageDialog(this,
                        "Username already exists",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                    return showRegistrationDialog(); // Show dialog again
                }
                
                // Register new user
                if (dbAuth.registerUser(username, password)) {
                    // Registration successful
                    JOptionPane.showMessageDialog(this,
                        "Registration successful. Please login.",
                        "Registration Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    return showLoginDialog(); // Show login dialog
                } else {
                    // Registration failed
                    JOptionPane.showMessageDialog(this,
                        "Registration failed. Please try again.",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                    return showRegistrationDialog(); // Show dialog again
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return false; // Cancelled
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ChatClientGUI();
            }
        });
    }

    /**
     * Add a reconnect button to the GUI
     */
    private JButton createReconnectButton() {
        JButton reconnectButton = new RoundedButton("Reconnect to Server");
        reconnectButton.setMargin(new Insets(8, 15, 8, 15));
        reconnectButton.addActionListener(e -> {
            // Disable the button to prevent multiple clicks
            reconnectButton.setEnabled(false);
            reconnectButton.setText("Connecting...");
            
            // Create a timer to enable the button again if connection fails
            Timer enableTimer = new Timer(5000, evt -> {
                ((Timer)evt.getSource()).stop();
                if (!isConnected) {
                    reconnectButton.setEnabled(true);
                    reconnectButton.setText("Reconnect to Server");
                }
            });
            enableTimer.setRepeats(false);
            enableTimer.start();
            
            // Try to connect
            connectToServer();
        });
        return reconnectButton;
    }
}
