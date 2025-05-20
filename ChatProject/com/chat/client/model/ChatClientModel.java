package com.chat.client.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// Import ChatMessage dari package yang sama (model)
// import com.chat.client.model.ChatMessage; // Tidak perlu jika dalam package yang sama

public class ChatClientModel {
    private String username;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final List<ChatMessage> messages;
    private final PropertyChangeSupport support;
    private volatile boolean connected = false;

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;

    public static final String NEW_MESSAGE_PROPERTY = "newMessage";
    public static final String CONNECTION_STATUS_PROPERTY = "connectionStatus";
    public static final String ALL_MESSAGES_PROPERTY = "allMessages";

    public ChatClientModel() {
        this.messages = new CopyOnWriteArrayList<>();
        this.support = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }
    
    public boolean isConnected() {
        return connected;
    }

    public void connect() {
        if (connected) return;
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(username);
            
            boolean oldConnected = this.connected;
            this.connected = true;
            support.firePropertyChange(CONNECTION_STATUS_PROPERTY, oldConnected, this.connected);
            addMessageInternal(new ChatMessage("SYSTEM", "Berhasil terhubung ke server.", ChatMessage.MessageType.SYSTEM_MESSAGE));

            Thread readerThread = new Thread(new IncomingReader());
            readerThread.setDaemon(true);
            readerThread.start();

        } catch (UnknownHostException e) {
            handleConnectionError("Server tidak ditemukan: " + e.getMessage());
        } catch (IOException e) {
            handleConnectionError("Error I/O saat menghubungkan: " + e.getMessage());
        }
    }
    
    private void handleConnectionError(String errorMessage) {
        boolean oldConnected = this.connected;
        this.connected = false;
        support.firePropertyChange(CONNECTION_STATUS_PROPERTY, oldConnected, this.connected);
        addMessageInternal(new ChatMessage("SYSTEM", errorMessage, ChatMessage.MessageType.ERROR_MESSAGE));
    }

    public void sendMessage(String content) {
        if (out != null && connected && content != null && !content.trim().isEmpty()) {
            out.println(content);
        }
    }
    
    private void addMessageInternal(ChatMessage message) {
        synchronized (messages) {
            messages.add(message);
        }
        support.firePropertyChange(NEW_MESSAGE_PROPERTY, null, message);
    }

    public void disconnect() {
        if (!connected && socket == null) return;
        
        boolean oldConnected = this.connected;
        this.connected = false; // Set connected to false first
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); 
            }
        } catch (IOException e) {
            // Log error if necessary
        } finally {
            socket = null;
            in = null;
            out = null;
            support.firePropertyChange(CONNECTION_STATUS_PROPERTY, oldConnected, this.connected);
            // Pesan disconnect akan ditangani oleh IncomingReader atau jika server mengirimkannya
        }
    }

    private class IncomingReader implements Runnable {
        @Override
        public void run() {
            String serverLine;
            try {
                if (in == null && connected) {
                     addMessageInternal(new ChatMessage("SYSTEM", "Error internal: Input stream tidak tersedia.", ChatMessage.MessageType.ERROR_MESSAGE));
                     // Tidak memanggil disconnect() dari sini untuk menghindari potensi rekursi jika disconnect() juga gagal
                     // Cukup pastikan status connected diubah oleh blok finally luar.
                     return;
                }
                // Loop hanya jika 'in' tidak null
                while (in != null && (serverLine = in.readLine()) != null) {
                    ChatMessage receivedMessage = new ChatMessage(serverLine, username);
                    addMessageInternal(receivedMessage);
                }
            } catch (IOException e) {
                if (connected) { 
                    addMessageInternal(new ChatMessage("SYSTEM", "Koneksi ke server terputus: " + e.getMessage(), ChatMessage.MessageType.ERROR_MESSAGE));
                }
            } finally {
                // Jika loop berakhir, pastikan status koneksi diperbarui jika belum.
                // Ini penting jika disconnect() tidak dipanggil secara eksplisit.
                if (connected) { // Jika kita masih menganggap diri kita terhubung
                    boolean oldStatus = connected;
                    connected = false;
                    support.firePropertyChange(CONNECTION_STATUS_PROPERTY, oldStatus, connected);
                    // addMessageInternal(new ChatMessage("SYSTEM", "Koneksi ditutup.", ChatMessage.MessageType.SYSTEM_MESSAGE));
                }
                 // Pastikan socket dan stream ditutup jika belum oleh disconnect()
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (socket != null && !socket.isClosed()) socket.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }
}
