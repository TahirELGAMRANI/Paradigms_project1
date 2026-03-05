package com.chat.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Model: socket communication and connection state.
 * Independent of the UI (Separation of Concerns).
 */
public class ClientModel {
    private static final String CMD_ALL_USERS = "allUsers";
    private static final String CMD_END = "end";
    private static final String CMD_BYE = "bye";
    private static final String PREFIX_USERS = "USERS|";

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile String username;
    private volatile boolean readOnly;
    private volatile boolean connected;
    private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();
    private ClientModelListener listener;

    public interface ClientModelListener {
        void onConnected(boolean readOnlyMode);
        void onMessage(String message);
        void onUserList(String userListMessage);
        void onDisconnected(String reason);
    }

    public void setListener(ClientModelListener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port, String username) throws IOException {
        this.username = username != null ? username.trim() : "";
        this.readOnly = this.username.isEmpty();
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println(this.username.isEmpty() ? "" : this.username);
        String response = in.readLine();
        connected = true;
        if (listener != null) {
            listener.onConnected(readOnly);
        }
        readerExecutor.submit(this::readLoop);
    }

    private void readLoop() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                if (line.startsWith(PREFIX_USERS)) {
                    if (listener != null) listener.onUserList(line.substring(PREFIX_USERS.length()));
                } else if (listener != null) {
                    listener.onMessage(line);
                }
            }
        } catch (IOException e) {
            if (connected && listener != null) {
                listener.onDisconnected(e.getMessage());
            }
        } finally {
            connected = false;
        }
    }

    public void sendMessage(String text) {
        if (!connected || out == null) return;
        if (readOnly) return;
        String trimmed = text != null ? text.trim() : "";
        if (trimmed.isEmpty()) return;
        out.println(trimmed);
        if (CMD_END.equalsIgnoreCase(trimmed) || CMD_BYE.equalsIgnoreCase(trimmed)) {
            disconnect();
        }
    }

    public void requestAllUsers() {
        if (connected && out != null && !readOnly) {
            out.println(CMD_ALL_USERS);
        }
    }

    public void disconnect() {
        connected = false;
        if (out != null) {
            out.println(CMD_BYE);
            out.flush();
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        readerExecutor.shutdownNow();
        if (listener != null) listener.onDisconnected("Disconnected");
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getUsername() {
        return username != null ? username : "";
    }
}
