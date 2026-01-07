package callapp1;

import java.io.*;
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
            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println(requestLine);

            // Consuma header
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
            }

            String method = requestLine.split(" ")[0];

            // ===== CORS / PREFLIGHT =====
            if (method.equals("OPTIONS")) {
                out.write("HTTP/1.1 204 No Content\r\n");
                writeCorsHeaders(out);
                out.write("Connection: close\r\n");
                out.write("\r\n");
                out.flush();
                return;
            }

            // ===== HEAD =====
            if (method.equals("HEAD")) {
                out.write("HTTP/1.1 200 OK\r\n");
                writeCorsHeaders(out);
                out.write("Content-Length: 0\r\n");
                out.write("Connection: close\r\n");
                out.write("\r\n");
                out.flush();
                return;
            }

            // ===== GET =====
            String body = "{\"message\":\"Ciao dal server Java 21 🚀\"}";

            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: application/json\r\n");
            out.write("Content-Length: " + body.getBytes().length + "\r\n");
            writeCorsHeaders(out);
            out.write("Connection: close\r\n");
            out.write("\r\n");
            out.write(body);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeCorsHeaders(BufferedWriter out) throws IOException {
        out.write("Access-Control-Allow-Origin: *\r\n");
        out.write("Access-Control-Allow-Methods: GET, HEAD, OPTIONS\r\n");
        out.write("Access-Control-Allow-Headers: Content-Type\r\n");
    }
}
