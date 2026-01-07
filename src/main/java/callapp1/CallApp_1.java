package callapp1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class CallApp_1 {

    private static final String WWW_ROOT = "www";

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);

        while (true) {
            Socket client = serverSocket.accept();
            handleClient(client);
        }
    }

    private static void handleClient(Socket client) {
        try (
          BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
          OutputStream out = client.getOutputStream()
        )
        {
            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println(requestLine);

            // Consuma header
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {}

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            if (!method.equals("GET")) {
                send405(out);
                return;
            }

            if (path.equals("/")) {
                path = "/index.html";
            }

            Path filePath = Path.of(WWW_ROOT + path);

            if (!Files.exists(filePath)) {
                send404(out);
                return;
            }

            byte[] content = Files.readAllBytes(filePath);

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "text/html";
            }

            out.write(("HTTP/1.1 200 OK\r\n").getBytes());
            out.write(("Content-Type: " + contentType + "\r\n").getBytes());
            out.write(("Content-Length: " + content.length + "\r\n").getBytes());
            out.write(("Connection: close\r\n").getBytes());
            out.write(("\r\n").getBytes());
            out.write(content);
            out.flush();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void send404(OutputStream out) throws IOException {
        String body = "<h1>404 - Not Found!!!</h1>";
        out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
        out.write("Content-Type: text/html\r\n".getBytes());
        out.write(("Content-Length: " + body.length() + "\r\n").getBytes());
        out.write("\r\n".getBytes());
        out.write(body.getBytes());
    }

    private static void send405(OutputStream out) throws IOException {
        String body = "<h1>405 - Method Not Allowed!!!!</h1>";
        out.write("HTTP/1.1 405 Method Not Allowed\r\n".getBytes());
        out.write("Content-Type: text/html\r\n".getBytes());
        out.write(("Content-Length: " + body.length() + "\r\n").getBytes());
        out.write("\r\n".getBytes());
        out.write(body.getBytes());
    }
}
