package com.chat.client;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * View: JavaFX UI for the client (login, chat area, Online indicator, Send).
 * Presentation only; no business logic.
 */
public class ClientView extends VBox {
    private final GridPane loginPane;
    private final TextField usernameField;
    private final Button loginButton;
    private final GridPane chatPane;
    private final TextArea chatArea;
    private final TextField inputField;
    private final Button sendButton;
    private final Label statusLabel;
    private final Circle statusDot;
    private final Label readOnlyLabel;
    private ClientViewListener listener;

    public interface ClientViewListener {
        void onLogin(String username);
        void onSendMessage(String text);
        void onDisconnect();
    }

    public ClientView() {
        setSpacing(0);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #fafafa;");

        // --- Login screen ---
        loginPane = new GridPane();
        loginPane.setHgap(10);
        loginPane.setVgap(10);
        loginPane.setAlignment(Pos.CENTER);
        Label userLabel = new Label("Username (empty = read-only):");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username or leave empty");
        usernameField.setPrefWidth(220);
        usernameField.setOnAction(e -> tryLogin());
        loginButton = new Button("Join Chat");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> tryLogin());
        HBox loginBox = new HBox(10, userLabel, usernameField, loginButton);
        loginBox.setAlignment(Pos.CENTER);
        loginPane.add(loginBox, 0, 0);
        getChildren().add(loginPane);

        // --- Chat screen (hidden until logged in) ---
        chatPane = new GridPane();
        chatPane.setHgap(10);
        chatPane.setVgap(8);
        chatPane.setVisible(false);
        chatPane.setManaged(false);

        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusDot = new Circle(6);
        statusDot.setFill(Color.GRAY);
        statusLabel = new Label("Offline");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        readOnlyLabel = new Label("");
        readOnlyLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
        statusBox.getChildren().addAll(statusDot, statusLabel, readOnlyLabel);
        chatPane.add(statusBox, 0, 0, 2, 1);

        Label chatLabel = new Label("Chat:");
        chatLabel.setStyle("-fx-font-weight: bold;");
        chatPane.add(chatLabel, 0, 1, 2, 1);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefRowCount(15);
        chatArea.setPrefHeight(320);
        chatPane.add(chatArea, 0, 2, 2, 1);
        GridPane.setHgrow(chatArea, Priority.ALWAYS);
        GridPane.setVgrow(chatArea, Priority.ALWAYS);

        Label messageLabel = new Label("Message (type here, then SEND or Enter):");
        messageLabel.setStyle("-fx-font-weight: bold;");
        chatPane.add(messageLabel, 0, 3, 2, 1);
        inputField = new TextField();
        inputField.setPromptText("Type your message here... (or: allUsers / bye / end)");
        inputField.setDisable(true);
        inputField.setOnAction(e -> sendFromInput());
        sendButton = new Button("SEND");
        sendButton.setDisable(true);
        sendButton.setOnAction(e -> sendFromInput());
        HBox inputBox = new HBox(10, inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        chatPane.add(inputBox, 0, 4, 2, 1);
        getChildren().add(chatPane);
    }

    public void setViewListener(ClientViewListener listener) {
        this.listener = listener;
    }

    private void tryLogin() {
        if (listener != null) {
            listener.onLogin(usernameField.getText());
        }
    }

    private void sendFromInput() {
        String text = inputField.getText();
        if (text != null && !text.trim().isEmpty() && listener != null) {
            listener.onSendMessage(text);
            inputField.clear();
        }
    }

    public void showChat(boolean readOnly) {
        Platform.runLater(() -> {
            loginPane.setVisible(false);
            loginPane.setManaged(false);
            chatPane.setVisible(true);
            chatPane.setManaged(true);
            statusDot.setFill(Color.GREEN);
            statusLabel.setText("Online");
            if (readOnly) {
                readOnlyLabel.setText("(Read-only mode)");
                inputField.setDisable(true);
                sendButton.setDisable(true);
            } else {
                readOnlyLabel.setText("");
                inputField.setDisable(false);
                sendButton.setDisable(false);
            }
        });
    }

    public void appendMessage(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }

    public void showUserList(String userListMessage) {
        Platform.runLater(() -> {
            String display = "Active users: " + userListMessage.replace(",", ", ");
            chatArea.appendText(display + "\n");
        });
    }

    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void setOffline() {
        Platform.runLater(() -> {
            statusDot.setFill(Color.GRAY);
            statusLabel.setText("Offline");
        });
    }

    public void disconnectAndShowLogin() {
        Platform.runLater(() -> {
            chatPane.setVisible(false);
            chatPane.setManaged(false);
            loginPane.setVisible(true);
            loginPane.setManaged(true);
            usernameField.clear();
            chatArea.clear();
            inputField.clear();
            setOffline();
        });
    }

    public String getUsernameInput() {
        return usernameField.getText();
    }
}
