import java.io.*;
import java.net.*;
import java.util.*;

class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            this.username = in.readLine(); // Klien mengirim username sebagai pesan pertama
            if (this.username == null || this.username.trim().isEmpty()) {
                System.out.println("Klien (" + socket.getRemoteSocketAddress() + ") tidak mengirim username, koneksi ditutup.");
                // Tidak perlu log di sini karena Server.addClient tidak akan dipanggil
                return; // Tutup koneksi jika tidak ada username
            }
            // Daftarkan klien ke server SETELAH username diterima
            Server.addClient(this);
            // Pesan bergabung akan disiarkan oleh Server.addClient ke semua, termasuk yang baru bergabung

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("/exit")) {
                    break;
                }
                // Format pesan yang akan dibroadcast: [Username]: Pesan
                String formattedMessage = "[" + this.username + "]: " + clientMessage;
                System.out.println("Pesan dari " + this.username + " (" + socket.getRemoteSocketAddress() + "): " + clientMessage);
                // Server akan menangani logging dan broadcasting ke semua klien
                Server.broadcastMessage(formattedMessage, this, false); // false karena ini pesan pengguna
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
