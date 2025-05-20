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
// Tambahkan import untuk styling teks
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

public class ChatClientGUI extends JFrame {
    // Ganti JTextArea dengan JTextPane
    private JTextPane messagePane;
    private JTextField textField;
    private JButton sendButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String username;

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;

    public ChatClientGUI() {
        super("Chat Klien");

        this.username = JOptionPane.showInputDialog(this, "Masukkan username Anda:", "Username", JOptionPane.PLAIN_MESSAGE);
        if (this.username == null || this.username.trim().isEmpty()) {
            this.username = "Guest" + (int)(Math.random() * 1000);
            JOptionPane.showMessageDialog(this, "Username tidak dimasukkan. Menggunakan default: " + this.username, "Info Username", JOptionPane.INFORMATION_MESSAGE);
        }
        setTitle("Chat Klien - " + this.username);

        // Setup UI dengan JTextPane
        messagePane = new JTextPane();
        messagePane.setEditable(false);
        // doc = messagePane.getStyledDocument(); // StyledDocument diambil langsung dari JTextPane
        JScrollPane scrollPane = new JScrollPane(messagePane);
        textField = new JTextField(40);
        sendButton = new JButton("Kirim");

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout());
        southPanel.add(textField);
        southPanel.add(sendButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        ActionListener sendListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                if (message != null && !message.trim().isEmpty()) {
                    out.println(message); // Kirim pesan mentah ke server
                    textField.setText("");
                }
            }
        };
        sendButton.addActionListener(sendListener);
        textField.addActionListener(sendListener);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setSize(600, 400); // Atur ukuran default yang lebih representatif
        setLocationRelativeTo(null);
        setVisible(true);

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(this.username); // Kirim username ke server
            new Thread(new IncomingReader()).start();
        } catch (UnknownHostException e) {
            appendToPane("SYSTEM", "Server tidak ditemukan: " + e.getMessage(), Color.DARK_GRAY, StyleConstants.ALIGN_CENTER, true, true);
            // JOptionPane.showMessageDialog(this, "Server tidak ditemukan: " + e.getMessage(), "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            // System.exit(1);
        } catch (IOException e) {
            appendToPane("SYSTEM", "Error I/O: " + e.getMessage(), Color.DARK_GRAY, StyleConstants.ALIGN_CENTER, true, true);
            // JOptionPane.showMessageDialog(this, "Error I/O: " + e.getMessage(), "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            // System.exit(1);
        }
    }

    // Metode untuk menambahkan teks dengan style ke JTextPane
    private void appendToPane(String sender, String message, Color senderColor, int alignment, boolean senderBold, boolean isSystemMessage) {
        SwingUtilities.invokeLater(() -> { // Pastikan update UI di EDT
            StyledDocument doc = messagePane.getStyledDocument();
            SimpleAttributeSet senderAttrs = new SimpleAttributeSet();
            StyleConstants.setForeground(senderAttrs, senderColor);
            StyleConstants.setBold(senderAttrs, senderBold);

            SimpleAttributeSet messageAttrs = new SimpleAttributeSet();
            StyleConstants.setForeground(messageAttrs, Color.BLACK); // Warna default untuk isi pesan

            SimpleAttributeSet paragraphAttrs = new SimpleAttributeSet();
            StyleConstants.setAlignment(paragraphAttrs, alignment);

            try {
                int P_START_OFFSET = doc.getLength();
                if (isSystemMessage) {
                    StyleConstants.setItalic(messageAttrs, true); // Pesan sistem miring
                    StyleConstants.setForeground(messageAttrs, Color.GRAY); // Warna pesan sistem
                    doc.insertString(doc.getLength(), message + "\n", messageAttrs);
                } else {
                    doc.insertString(doc.getLength(), sender + ": ", senderAttrs);
                    doc.insertString(doc.getLength(), message + "\n", messageAttrs);
                }
                doc.setParagraphAttributes(P_START_OFFSET, doc.getLength() - P_START_OFFSET, paragraphAttrs, false);
                messagePane.setCaretPosition(doc.getLength()); // Auto-scroll
            } catch (BadLocationException e) {
                e.printStackTrace(); // Seharusnya tidak terjadi
            }
        });
    }

    private class IncomingReader implements Runnable {
        public void run() {
            String lineFromServer;
            try {
                while ((lineFromServer = in.readLine()) != null) {
                    if (lineFromServer.startsWith("SYSTEM:")) {
                        String systemMsg = lineFromServer.substring("SYSTEM:".length()).trim();
                        appendToPane("SYSTEM", systemMsg, Color.DARK_GRAY, StyleConstants.ALIGN_CENTER, true, true);
                    } else {
                        String[] parts = lineFromServer.split(":", 2);
                        if (parts.length == 2) {
                            String msgSender = parts[0].trim();
                            String msgContent = parts[1].trim();
                            if (msgSender.equals(username)) {
                                // Pesan dari diri sendiri
                                appendToPane(username, msgContent, new Color(0, 100, 0), StyleConstants.ALIGN_RIGHT, true, false); // Hijau tua, rata kanan
                            } else {
                                // Pesan dari orang lain
                                appendToPane(msgSender, msgContent, Color.BLUE, StyleConstants.ALIGN_LEFT, true, false); // Biru, rata kiri
                            }
                        } else {
                            // Pesan tidak terformat, tampilkan sebagai pesan sistem/info
                             appendToPane("SERVER", lineFromServer, Color.ORANGE, StyleConstants.ALIGN_CENTER, false, true);
                        }
                    }
                }
            } catch (IOException e) {
                if (socket != null && !socket.isClosed()) {
                     appendToPane("SYSTEM", "Koneksi ke server terputus.", Color.RED, StyleConstants.ALIGN_CENTER, true, true);
                }
            } finally {
                try {
                    if (socket != null) socket.close();
                    if (in != null) in.close();
                    if (out != null) out.close();
                } catch (IOException ex) { /* Abaikan */ }
                textField.setEditable(false);
                sendButton.setEnabled(false);
                appendToPane("SYSTEM", "Anda telah keluar dari chat.", Color.DARK_GRAY, StyleConstants.ALIGN_CENTER, true, true);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ChatClientGUI();
            }
        });
    }
}
