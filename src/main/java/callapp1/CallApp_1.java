package callapp1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CallApp_1 {

    private static final String WWW_ROOT = "www";
    private static final List<String> messages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);
        System.out.println("WWW_ROOT == " + WWW_ROOT);

        while (true) {
            Socket client = serverSocket.accept();
            handleClient(client);
        }
    }

    private static void handleClient(Socket client) {
        try (
          BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
          OutputStream out = client.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println("requestLine == " + requestLine);

            // Consuma header
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {}

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                send400(out);
                return;
            }

            String method = parts[0];
            String path = parts[1];
            System.out.println("method == " + method);
            System.out.println("path == " + path);

            if (!method.equals("GET")) {
                send405(out);
                return;
            }

            // --- ROUTING CHAT: invio messaggio ---
            if (path.startsWith("/send")) {
                String msg = "";
                int q = path.indexOf("?");
                if (q != -1 && path.length() > q + 1) {
                    String query = path.substring(q + 1);
                    if (query.startsWith("msg=")) {
                        msg = URLDecoder.decode(query.substring(4), "UTF-8");
                    }
                }

                synchronized (messages) {
                    messages.add(msg); // aggiunge il messaggio alla lista globale
                }

                sendText(out, "OK");
                return;
            }

            // --- ROUTING CHAT: recupero messaggi ---
            if (path.equals("/messages")) {
                StringBuilder sb = new StringBuilder();
                synchronized (messages) {
                    for (String m : messages) {
                        sb.append(m).append("\n");
                    }
                }
                sendText(out, sb.toString());
                return;
            }

            // --- SERVE FILE STATICI ---
            if (path.equals("/")) {
                path = "/index.html";
            }

            Path filePath = Path.of(WWW_ROOT, path.substring(1));
            System.out.println("filePath == " + filePath);

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendText(OutputStream out, String body) throws IOException {
        byte[] content = body.getBytes("UTF-8");
        out.write(("HTTP/1.1 200 OK\r\n").getBytes());
        out.write(("Content-Type: text/plain; charset=UTF-8\r\n").getBytes());
        out.write(("Content-Length: " + content.length + "\r\n").getBytes());
        out.write(("Connection: close\r\n").getBytes());
        out.write(("\r\n").getBytes());
        out.write(content);
        out.flush();
    }

    private static void send404(OutputStream out) throws IOException {
        String body = "<h1>404 - Not Found!!!</h1>";
        out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
        out.write("Content-Type: text/html\r\n".getBytes());
        out.write(("Content-Length: " + body.length() + "\r\n").getBytes());
        out.write(("\r\n").getBytes());
        out.write(body.getBytes());
    }

    private static void send405(OutputStream out) throws IOException {
        String body = "<h1>405 - Method Not Allowed!!!!</h1>";
        out.write("HTTP/1.1 405 Method Not Allowed\r\n".getBytes());
        out.write("Content-Type: text/html\r\n".getBytes());
        out.write(("Content-Length: " + body.length() + "\r\n").getBytes());
        out.write(("\r\n").getBytes());
        out.write(body.getBytes());
    }

    private static void send400(OutputStream out) throws IOException {
        String body = "<h1>400 - Bad Request</h1>";
        out.write("HTTP/1.1 400 Bad Request\r\n".getBytes());
        out.write("Content-Type: text/html\r\n".getBytes());
        out.write(("Content-Length: " + body.length() + "\r\n").getBytes());
        out.write(("\r\n").getBytes());
        out.write(body.getBytes());
    }
}
