package com.chat.client.controller;

import com.chat.client.model.ChatClientModel;
import com.chat.client.view.ChatClientView;
// import com.chat.client.model.ChatMessage; // Tidak digunakan secara langsung di sini

import javax.swing.JOptionPane;

public class ChatClientController {
    private ChatClientModel model;
    private ChatClientView view;

    public ChatClientController(ChatClientModel model) {
        this.model = model;
    }
    
    public void setView(ChatClientView view) {
        this.view = view;
        this.model.addPropertyChangeListener(this.view); 
    }

    public void startApplication() {
        String usernameInput = JOptionPane.showInputDialog(view, "Masukkan username Anda:", "Username", JOptionPane.PLAIN_MESSAGE);
        if (usernameInput == null || usernameInput.trim().isEmpty()) {
            usernameInput = "Guest" + (int) (Math.random() * 1000);
            JOptionPane.showMessageDialog(view, "Username tidak dimasukkan. Menggunakan default: " + usernameInput, "Info Username", JOptionPane.INFORMATION_MESSAGE);
        }
        model.setUsername(usernameInput);
        if (view != null) { // Pastikan view sudah di-set
            view.setTitle("Chat Klien - " + model.getUsername());
            view.updateMessages(model.getMessages()); 
            view.setInteractionEnabled(false); 
        }
        model.connect(); 
    }

    public void sendMessageRequested(String messageContent) {
        if (model.isConnected()) {
            model.sendMessage(messageContent);
        } else {
            if (view != null) {
                JOptionPane.showMessageDialog(view, "Tidak terhubung ke server. Pesan tidak terkirim.", "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            }
            // Untuk konsistensi, pesan error bisa juga dikirim melalui model agar view menampilkannya
            // model.addMessageInternal(new ChatMessage("SYSTEM", "Pesan tidak terkirim, tidak ada koneksi.", ChatMessage.MessageType.ERROR_MESSAGE));
        }
    }

    public void disconnectRequested() {
        model.disconnect();
    }
    
    public String getClientUsername() {
        return model.getUsername();
    }
}
