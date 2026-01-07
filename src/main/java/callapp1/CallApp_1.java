package callapp1;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class CallApp_1 {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(
          System.getenv().getOrDefault("PORT", "8080")
        );

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);

        while (true) {
            Socket client = serverSocket.accept();
            handleClient(client);
        }
    }

    private static void handleClient(Socket client) {
        try (
          BufferedReader in = new BufferedReader(
            new InputStreamReader(client.getInputStream()));
          BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(client.getOutputStream()))
        ) {
            String line;

            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
            }

            String body = "{\"message\":\"Ciao dal server Java 21 🚀\"}";

            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: application/json\r\n");
            out.write("Content-Length: " + body.length() + "\r\n");
            out.write("Connection: close\r\n");
            out.write("\r\n");
            out.write(body);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
