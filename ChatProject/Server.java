import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 1234;
    static Vector<ClientHandler> clients = new Vector<>();
    private static final String LOG_FILE_PATH = "c:\\CODING\\Proyek_PBO\\ChatProject\\chat_log.txt";
    private static PrintWriter logWriter;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server berjalan di port " + PORT + " menunggu koneksi...");
            logWriter = new PrintWriter(new FileWriter(LOG_FILE_PATH, true), true); // true untuk append mode, true untuk autoFlush
            log("SERVER", "Server dimulai.");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientThread = new ClientHandler(socket);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error pada server utama: " + e.getMessage());
            e.printStackTrace();
            log("SERVER_ERROR", "Server utama gagal: " + e.getMessage());
        } finally {
            if (logWriter != null) {
                log("SERVER", "Server dihentikan.");
                logWriter.close();
            }
        }
    }

    // Metode untuk menyiarkan pesan ke semua klien
    public static synchronized void broadcastMessage(String message, ClientHandler sender, boolean isSystemMessage) {
        String messageToBroadcast;
        String logMessage;

        if (isSystemMessage) {
            messageToBroadcast = "SYSTEM: " + message; // Prefix untuk klien agar tahu ini pesan sistem
            logMessage = "SYSTEM: " + message;
        } else {
            // Pesan dari pengguna, formatnya sudah [Username]: PesanKonten
            messageToBroadcast = message; // Klien akan mem-parse username dari sini
            logMessage = message; // Log pesan pengguna apa adanya
        }

        System.out.println("Broadcasting: " + messageToBroadcast);
        log("BROADCAST", messageToBroadcast);

        Vector<ClientHandler> currentClients = new Vector<>(clients);
        for (ClientHandler client : currentClients) {
            client.sendMessage(messageToBroadcast);
        }
    }

    // Metode untuk menambahkan klien setelah username divalidasi
    public static synchronized void addClient(ClientHandler client) {
        if (client.getUsername() != null && !client.getUsername().trim().isEmpty()) {
            clients.add(client);
            String joinMessage = client.getUsername() + " (" + client.getSocket().getRemoteSocketAddress() + ") telah bergabung.";
            System.out.println(joinMessage);
            log("CONNECTION", joinMessage);
            // Siarkan pesan bergabung ke SEMUA klien, termasuk yang baru bergabung
            broadcastMessage(client.getUsername() + " telah bergabung dengan chat!", client, true);
        } else {
            String failedJoinMsg = "Upaya menambahkan klien tanpa username yang valid dari " + client.getSocket().getRemoteSocketAddress();
            System.out.println(failedJoinMsg);
            log("CONNECTION_ERROR", failedJoinMsg);
        }
    }

    // Metode untuk menghapus klien
    public static synchronized void removeClient(ClientHandler client) {
        boolean removed = clients.remove(client);
        if (removed && client.getUsername() != null) {
            String leaveMessage = client.getUsername() + " (" + client.getSocket().getRemoteSocketAddress() + ") telah meninggalkan chat.";
            System.out.println(leaveMessage);
            log("CONNECTION", leaveMessage);
            // Siarkan pesan keluar ke klien yang TERSISA
            broadcastMessage(client.getUsername() + " telah meninggalkan chat.", client, true);
        } else if (removed) {
            String anonLeaveMsg = "Klien anonim (" + client.getSocket().getRemoteSocketAddress() + ") terputus.";
            System.out.println(anonLeaveMsg);
            log("CONNECTION", anonLeaveMsg);
        }
    }

    // Metode untuk logging
    private static synchronized void log(String type, String message) {
        if (logWriter != null) {
            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            logWriter.println(timestamp + " [" + type + "] " + message);
        }
    }
}
