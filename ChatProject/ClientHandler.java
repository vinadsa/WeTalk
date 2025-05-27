import java.io.*;
import java.net.*;
import java.sql.SQLException;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Read username and password
            this.username = in.readLine();
            String password = in.readLine();
            
            // Validate input
            if (username == null || username.trim().isEmpty() || password == null) {
                out.println("ERROR: Username dan password diperlukan");
                System.out.println("Klien (" + socket.getRemoteSocketAddress() + ") tidak mengirim username/password, koneksi ditutup.");
                return;
            }
            
            // Authenticate user with database
            try {
                if (DatabaseHandler.validateLogin(username, password)) {
                    // Login successful
                    out.println("LOGIN_SUCCESS");
                    Server.addClient(this);
                    
                    // Handle chat messages
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {
                        if (clientMessage.equalsIgnoreCase("/exit")) {
                            break;
                        }
                        String formattedMessage = "[" + username + "]: " + clientMessage;
                        System.out.println("Pesan dari " + this.username + " (" + socket.getRemoteSocketAddress() + "): " + clientMessage);
                        Server.broadcastMessage(formattedMessage, this, false);
                    }
                } else {
                    // Login failed
                    out.println("ERROR: Username atau password salah");
                    System.out.println("Klien (" + socket.getRemoteSocketAddress() + ") gagal login dengan username: " + this.username);
                    return;
                }
            } catch (SQLException e) {
                out.println("ERROR: Database error");
                System.err.println("Database error during login: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } catch (IOException e) {
            if (this.username != null) {
                System.out.println("Koneksi dengan " + this.username + " (" + socket.getRemoteSocketAddress() + ") terputus: " + e.getMessage());
            } else {
                System.out.println("Koneksi dengan klien (" + socket.getRemoteSocketAddress() + ") terputus sebelum username diterima: " + e.getMessage());
            }
            // Server.log akan dipanggil dari Server.removeClient jika username ada
        } finally {
            // Pesan meninggalkan chat akan disiarkan oleh Server.removeClient
            Server.removeClient(this); // Hapus klien dari daftar server
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // Abaikan error saat menutup sumber daya
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null && !socket.isClosed()) { // Pastikan output stream ada dan socket terbuka
            out.println(message);
        }
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }
}
