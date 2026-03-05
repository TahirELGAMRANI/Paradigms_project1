package com.chat.server;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * View: JavaFX UI for the server (ListView of clients, activity log).
 * Presentation only; no business logic.
 */
public class ServerView extends VBox {
    private final ListView<String> clientListView;
    private final TextArea logArea;
    private final Label statusLabel;
    private final Circle statusDot;

    public ServerView() {
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #f5f5f5;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        statusLabel = new Label("Status: Stopped");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        statusDot = new Circle(6);
        statusDot.setFill(Color.GRAY);
        HBox statusBox = new HBox(8, statusDot, statusLabel);
        statusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        grid.add(statusBox, 0, 0, 2, 1);

        Label clientListLabel = new Label("Connected clients:");
        clientListLabel.setStyle("-fx-font-weight: bold;");
        grid.add(clientListLabel, 0, 1);

        clientListView = new ListView<>();
        clientListView.setPrefHeight(200);
        clientListView.setPlaceholder(new Label("No clients connected"));
        clientListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    int idx = getIndex();
                    List<String> colors = getColorsForList();
                    if (idx >= 0 && idx < colors.size()) {
                        setStyle("-fx-background-color: " + colors.get(idx) + "; -fx-background-radius: 3;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        grid.add(clientListView, 0, 2);
        VBox.setVgrow(clientListView, Priority.SOMETIMES);

        Label logLabel = new Label("Activity log:");
        logLabel.setStyle("-fx-font-weight: bold;");
        grid.add(logLabel, 0, 3);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(12);
        logArea.setPrefHeight(220);
        grid.add(logArea, 0, 4);
        GridPane.setHgrow(logArea, Priority.ALWAYS);
        GridPane.setVgrow(logArea, Priority.ALWAYS);

        getChildren().add(grid);
    }

    private List<String> getColorsForList() {
        Object prop = clientListView.getProperties().get("userColors");
        if (prop instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> c = (List<String>) prop;
            return c;
        }
        return List.of();
    }

    public void setUserColors(List<String> colors) {
        clientListView.getProperties().put("userColors", colors);
        clientListView.refresh();
    }

    public void setClientList(List<String> usernames, List<String> colors) {
        Platform.runLater(() -> {
            clientListView.getItems().setAll(usernames);
            clientListView.getProperties().put("userColors", colors);
            clientListView.refresh();
        });
    }

    public void appendLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }

    public void setStatusOnline(boolean online) {
        Platform.runLater(() -> {
            statusLabel.setText(online ? "Status: Running" : "Status: Stopped");
            statusDot.setFill(online ? Color.GREEN : Color.GRAY);
        });
    }

    public ListView<String> getClientListView() {
        return clientListView;
    }
}
