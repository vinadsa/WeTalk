import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Thread untuk mendengarkan pesan dari server
            new Thread(() -> {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        System.out.println(">> " + response);
                    }
                } catch (IOException e) {
                    System.out.println("Terputus dari server.");
                }
            }).start();

            // Kirim pesan ke server
            String message;
            while ((message = userInput.readLine()) != null) {
                out.println(message);
            }
        } catch (IOException e) {
            System.out.println("Error pada klien: " + e.getMessage());
            // e.printStackTrace(); // Uncomment untuk debugging lebih detail
        }
    }
}
