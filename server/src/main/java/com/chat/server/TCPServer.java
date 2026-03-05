package com.chat.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Server entry point. Loads config, wires Model and View (Separation of Concerns).
 * Run: java com.chat.server.TCPServer  (config from config.properties)
 */
public class TCPServer extends Application {
    private ServerModel model;
    private ServerView view;
    private Timer listUpdateTimer;

    @Override
    public void start(Stage stage) {
        view = new ServerView();
        Scene scene = new Scene(view, 450, 520);
        scene.getStylesheets().add(getClass().getResource("/server.css").toExternalForm());
        stage.setTitle("Group Chat - Server");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();

        model = new ServerModel();
        model.setListener(new ServerModel.ServerModelListener() {
            @Override
            public void onLog(String message) {
                view.appendLog(message);
            }

            @Override
            public void onClientJoined(String username, String colorHex) {
                view.appendLog("Welcome [ " + username + " ]");
                refreshClientList();
            }

            @Override
            public void onClientLeft(String username) {
                refreshClientList();
            }

            @Override
            public void onServerStarted(int port) {
                view.appendLog("Server Started on port " + port);
                view.setStatusOnline(true);
            }

            @Override
            public void onServerStopped() {
                view.setStatusOnline(false);
                view.appendLog("Server stopped.");
            }
        });

        try {
            ConfigLoader config = new ConfigLoader();
            view.appendLog("Waiting for clients...");
            model.start(config.getHost(), config.getPort());
            listUpdateTimer = new Timer(true);
            listUpdateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    refreshClientList();
                }
            }, 500, 500);
        } catch (IOException ex) {
            view.appendLog("Error: " + ex.getMessage());
        }
    }

    private void refreshClientList() {
        if (model != null) {
            Platform.runLater(() -> {
                view.setClientList(model.getUsernames(), model.getColors());
            });
        }
    }

    private void shutdown() {
        if (listUpdateTimer != null) {
            listUpdateTimer.cancel();
        }
        if (model != null) {
            model.stop();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
