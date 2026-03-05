package com.chat.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Model: server socket logic and message distribution.
 * Independent of the UI (Separation of Concerns).
 */
public class ServerModel {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String CMD_ALL_USERS = "allUsers";
    private static final String CMD_END = "end";
    private static final String CMD_BYE = "bye";

    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private volatile boolean running;
    private ServerModelListener listener;

    public interface ServerModelListener {
        void onLog(String message);
        void onClientJoined(String username, String colorHex);
        void onClientLeft(String username);
        void onServerStarted(int port);
        void onServerStopped();
    }

    public void setListener(ServerModelListener listener) {
        this.listener = listener;
    }

    public void start(String host, int port) throws IOException {
        InetAddress bindAddr = (host == null || host.isEmpty() || "0.0.0.0".equals(host))
                ? null : InetAddress.getByName(host);
        serverSocket = bindAddr != null ? new ServerSocket(port, 50, bindAddr) : new ServerSocket(port);
        running = true;
        if (listener != null) {
            listener.onServerStarted(port);
            listener.onLog("Waiting for clients...");
        }
        Thread acceptThread = new Thread(this::acceptLoop);
        acceptThread.setDaemon(false);
        acceptThread.start();
    }

    private void acceptLoop() {
        while (running && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleNewClient(clientSocket)).start();
            } catch (IOException e) {
                if (running) {
                    log("Accept error: " + e.getMessage());
                }
                break;
            }
        }
    }

    private void handleNewClient(Socket socket) {
        String username = null;
        boolean readOnly = true;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String firstLine = in.readLine();
            if (firstLine != null) {
                firstLine = firstLine.trim();
                if (!firstLine.isEmpty()) {
                    username = firstLine;
                    readOnly = false;
                }
            }
            if (username == null || username.isEmpty()) {
                username = "Guest-" + socket.getInetAddress().getHostAddress();
            }
            String colorHex = randomColorHex();
            ClientHandler handler = new ClientHandler(socket, username, readOnly, colorHex, in, out);
            clients.add(handler);
            if (listener != null) {
                listener.onClientJoined(username, colorHex);
                listener.onLog("Welcome [ " + username + " ]");
            }
            out.println(readOnly ? "READONLY" : "WELCOME");
            broadcast(null, "[" + username + "] joined the chat.", false);
            handler.run();
        } catch (IOException e) {
            log("Client connection error: " + e.getMessage());
        } finally {
            if (username != null) {
                removeClient(username);
                if (listener != null) listener.onClientLeft(username);
                broadcast(null, "[" + username + "] left the chat.", false);
            }
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private void removeClient(String username) {
        clients.removeIf(h -> username.equals(h.getUsername()));
    }

    private void broadcast(String excludeUsername, String formattedMessage, boolean includeExcluded) {
        for (ClientHandler h : clients) {
            if (excludeUsername != null && excludeUsername.equals(h.getUsername()) && !includeExcluded)
                continue;
            h.send(formattedMessage);
        }
    }

    void onMessage(String username, String message) {
        if (message == null) return;
        String trimmed = message.trim();
        if (trimmed.isEmpty()) return;

        if (CMD_END.equalsIgnoreCase(trimmed) || CMD_BYE.equalsIgnoreCase(trimmed)) {
            removeClient(username);
            if (listener != null) listener.onClientLeft(username);
            broadcast(null, "[" + username + "] left the chat.", false);
            return;
        }
        if (CMD_ALL_USERS.equalsIgnoreCase(trimmed)) {
            sendUserList(username);
            return;
        }
        String time = LocalTime.now().format(TIME_FMT);
        String formatted = "[" + username + "] " + time + " " + trimmed;
        broadcast(username, formatted, false);
    }

    private void sendUserList(String toUsername) {
        List<String> names = new ArrayList<>();
        for (ClientHandler h : clients) {
            names.add(h.getUsername());
        }
        String payload = "USERS|" + String.join(",", names);
        for (ClientHandler h : clients) {
            if (toUsername.equals(h.getUsername())) {
                h.send(payload);
                break;
            }
        }
    }

    private static String randomColorHex() {
        int r = 180 + (int) (Math.random() * 76);
        int g = 200 + (int) (Math.random() * 56);
        int b = 220 + (int) (Math.random() * 36);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private void log(String msg) {
        if (listener != null) listener.onLog(msg);
    }

    public void stop() {
        running = false;
        for (ClientHandler h : new ArrayList<>(clients)) {
            h.close();
        }
        clients.clear();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
            serverSocket = null;
        }
        if (listener != null) listener.onServerStopped();
    }

    public List<String> getUsernames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler h : clients) {
            names.add(h.getUsername());
        }
        return names;
    }

    public List<String> getColors() {
        List<String> colors = new ArrayList<>();
        for (ClientHandler h : clients) {
            colors.add(h.getColorHex());
        }
        return colors;
    }

    private class ClientHandler {
        private final Socket socket;
        private final String username;
        private final boolean readOnly;
        private final String colorHex;
        private final BufferedReader in;
        private final PrintWriter out;
        private volatile boolean closed;

        ClientHandler(Socket socket, String username, boolean readOnly, String colorHex,
                     BufferedReader in, PrintWriter out) {
            this.socket = socket;
            this.username = username;
            this.readOnly = readOnly;
            this.colorHex = colorHex;
            this.in = in;
            this.out = out;
        }

        String getUsername() { return username; }
        String getColorHex() { return colorHex; }

        void send(String message) {
            if (closed) return;
            try {
                out.println(message);
            } catch (Exception ignored) {}
        }

        void close() {
            closed = true;
            try {
                socket.close();
            } catch (IOException ignored) {}
        }

        void run() {
            try {
                String line;
                while (!closed && (line = in.readLine()) != null) {
                    if (readOnly) continue;
                    onMessage(username, line);
                }
            } catch (IOException e) {
                if (!closed) log("Read error for " + username + ": " + e.getMessage());
            } finally {
                removeClient(username);
                if (listener != null) listener.onClientLeft(username);
                close();
            }
        }
    }
}
