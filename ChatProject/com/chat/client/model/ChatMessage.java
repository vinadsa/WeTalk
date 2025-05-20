package com.chat.client.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    public enum MessageType {
        USER_MESSAGE,   // Pesan dari pengguna lain
        SELF_MESSAGE,   // Pesan dari klien ini sendiri
        SYSTEM_MESSAGE, // Notifikasi sistem (bergabung/keluar)
        ERROR_MESSAGE,  // Pesan error
        SERVER_INFO     // Pesan info umum dari server
    }

    private final String sender;
    private final String content;
    private final MessageType type;
    private final LocalDateTime timestamp;

    public ChatMessage(String sender, String content, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // Konstruktor untuk mem-parse pesan mentah dari server
    public ChatMessage(String rawMessageFromServer, String currentUsername) {
        this.timestamp = LocalDateTime.now();
        if (rawMessageFromServer.startsWith("SYSTEM:")) {
            this.type = MessageType.SYSTEM_MESSAGE;
            this.sender = "SYSTEM"; // Atau bisa juga null/kosong untuk pesan sistem
            this.content = rawMessageFromServer.substring("SYSTEM:".length()).trim();
        } else {
            // Mencoba mem-parse format "[User]: Pesan"
            String[] parts = rawMessageFromServer.split(":", 2);
            if (parts.length == 2) {
                this.sender = parts[0].trim().replace("[", "").replace("]", ""); // Bersihkan [ dan ]
                this.content = parts[1].trim();
                if (this.sender.equals(currentUsername)) {
                    this.type = MessageType.SELF_MESSAGE;
                } else {
                    this.type = MessageType.USER_MESSAGE;
                }
            } else {
                // Jika format tidak dikenali, anggap sebagai info server umum
                this.type = MessageType.SERVER_INFO;
                this.sender = "SERVER";
                this.content = rawMessageFromServer;
            }
        }
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s): %s", type, getFormattedTimestamp(), sender, content);
    }
}
