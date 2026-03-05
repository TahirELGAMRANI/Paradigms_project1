package com.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Client entry point. Expects: java TCPClient <ServerIPAddress> <PortNumber>
 * Wires Model and View (Separation of Concerns).
 */
public class TCPClient extends Application {
    private ClientModel model;
    private ClientView view;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        view = new ClientView();
        view.setViewListener(new ClientView.ClientViewListener() {
            @Override
            public void onLogin(String username) {
                connect(username);
            }

            @Override
            public void onSendMessage(String text) {
                if (model != null) {
                    String trimmed = text != null ? text.trim() : "";
                    if ("allUsers".equalsIgnoreCase(trimmed)) {
                        model.requestAllUsers();
                    } else if ("end".equalsIgnoreCase(trimmed) || "bye".equalsIgnoreCase(trimmed)) {
                        model.sendMessage(text);
                        // Don't echo; client will disconnect
                    } else {
                        model.sendMessage(text);
                        // Echo our own message so it appears in our chat (server only broadcasts to others)
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                        view.appendMessage("[" + model.getUsername() + "] " + time + " " + trimmed);
                    }
                }
            }

            @Override
            public void onDisconnect() {
                if (model != null) model.disconnect();
            }
        });

        Scene scene = new Scene(view, 500, 480);
        try {
            scene.getStylesheets().add(getClass().getResource("/client.css").toExternalForm());
        } catch (Exception ignored) {}
        stage.setTitle("Group Chat - Client");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            if (model != null) model.disconnect();
            Platform.exit();
        });
        stage.show();

        Parameters params = getParameters();
        java.util.List<String> args = params.getRaw();
        if (args.size() >= 2) {
            String host = args.get(0);
            int port;
            try {
                port = Integer.parseInt(args.get(1));
            } catch (NumberFormatException ex) {
                view.showError("Invalid port: " + args.get(1));
                return;
            }
            storeHostPort(host, port);
        } else {
            view.showError("Usage: java TCPClient <ServerIPAddress> <PortNumber>\nExample: java TCPClient localhost 3000");
        }
    }

    private String savedHost;
    private int savedPort;

    private void storeHostPort(String host, int port) {
        savedHost = host;
        savedPort = port;
    }

    private void connect(String username) {
        if (savedHost == null || savedPort <= 0) {
            view.showError("Server address and port were not provided. Run: java TCPClient <ServerIP> <Port>");
            return;
        }
        model = new ClientModel();
        model.setListener(new ClientModel.ClientModelListener() {
            @Override
            public void onConnected(boolean readOnlyMode) {
                view.showChat(readOnlyMode);
            }

            @Override
            public void onMessage(String message) {
                view.appendMessage(message);
            }

            @Override
            public void onUserList(String userListMessage) {
                view.showUserList(userListMessage);
            }

            @Override
            public void onDisconnected(String reason) {
                view.setOffline();
                view.appendMessage("[Disconnected: " + reason + "]");
                view.disconnectAndShowLogin();
            }
        });
        try {
            model.connect(savedHost, savedPort, username);
        } catch (Exception e) {
            view.showError("Could not connect: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Application.launch(TCPClient.class, args);
    }
}
