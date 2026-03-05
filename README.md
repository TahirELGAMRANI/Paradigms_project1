# Paradigms_project1 — Group Chat Application

A real-time **group chat** application built with **Java**, **TCP sockets**, and **JavaFX**. Multiple clients connect to a central server and exchange messages in a shared chat. This project implements the Mini Project 1 requirements (Server–Client architecture, authentication, read-only mode, and Model–View separation).

**Repository:** [https://github.com/TahirELGAMRANI/Paradigms_project1](https://github.com/TahirELGAMRANI/Paradigms_project1)

### Group

| Name |
|------|
| **Tahir Elgamrani** |
| Hiba Aouahchi |
| Yassmine Najib |
| Bouthayna Errouhi |

---

## Table of Contents

- [Overview](#overview)
- [Group](#group)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Requirements](#requirements)
- [Build & Run](#build--run)
- [Configuration](#configuration)
- [Usage & Commands](#usage--commands)
- [Multiple clients](#multiple-clients-see-messages-from-both)
- [Testing the Chat](#testing-the-chat)
- [Project Structure](#project-structure)
- [License](#license)

---

## Overview

The application has two parts:

- **Server (TCPServer):** Listens for client connections, receives messages from any client, and broadcasts them to all other connected clients. It shows a list of connected users and an activity log in a JavaFX window.
- **Client (TCPClient):** Connects to the server with a username, sends and receives messages in real time, and supports special commands (`allUsers`, `bye`/`end`). The client has a JavaFX UI: login screen, chat area, and message input with a SEND button.

Communication is **TCP** only; configuration (host/port) is loaded from a properties file on the server and passed as command-line arguments on the client.

---

## Features

### Client

- **Authentication:** User must enter a username before using the chat (or leave empty for read-only).
- **Read-only mode:** If the username is left empty, the user can only read messages; the input field and SEND button are disabled.
- **Real-time messaging:** Type in the text field and send with the **SEND** button or **Enter**.
- **Active users:** Type the command **`allUsers`** to receive a list of all currently connected clients.
- **Disconnect:** Type **`end`** or **`bye`** to disconnect; the connection closes and the server is notified.
- **UI:** “Online” status with a green indicator; sent messages appear in the chat area (including your own).

### Server

- **Multiple connections:** Accepts many clients at once (one thread per client).
- **Message distribution:** When a client sends a message, the server adds the sender’s username and time, then broadcasts it to all other connected clients.
- **Client list:** A live list of connected usernames in the server UI.
- **Visual distinction:** Each user in the list is shown with a random background color.
- **Activity log:** Messages such as “Server Started,” “Waiting for clients,” and “Welcome [User].”

### General

- **Model–View separation:** Business logic and network code (Model) are independent of the JavaFX UI (View).
- **Config-driven server:** Server host and port are read from `config.properties` (no recompile needed for environment changes).

---

## Architecture

- **Server–Client (TCP):** One central server, multiple clients. Server listens on a configurable host/port; clients connect and communicate over TCP sockets.
- **Server:** Thread-per-connection; each client is handled in its own thread. Messages are broadcast to all other clients.
- **Client:** One thread for reading server messages; UI updates run on the JavaFX thread.
- **Model–View:**  
  - **Server:** `ServerModel` (sockets, broadcast, client list) and `ServerView` (JavaFX).  
  - **Client:** `ClientModel` (sockets, send/receive) and `ClientView` (JavaFX).

---

## Technology Stack

| Component        | Technology                          |
|-----------------|-------------------------------------|
| Language        | Java 17                             |
| Network         | Java Sockets (TCP)                  |
| GUI             | JavaFX (GridPane layout, CSS)       |
| Build           | Maven (multi-module)                |
| Config          | `.properties` (server host/port)    |

---

## Requirements

- **JDK 17** (or 11+ with JavaFX; JavaFX is not bundled in JDK 11+).
- **Maven 3.6+** (optional if you use the included Maven wrapper `./mvnw`).

---

## Build & Run

### Build

From the project root:

```bash
./mvnw clean package
```

If you have Maven installed globally: `mvn clean package`.

This produces:

- `server/target/server-1.0-SNAPSHOT.jar` and `server/target/lib/`
- `client/target/client-1.0-SNAPSHOT.jar` and `client/target/lib/`

### Run server

```bash
java --module-path server/target/lib --add-modules javafx.controls -jar server/target/server-1.0-SNAPSHOT.jar
```

Or use the script (from project root):

```bash
./run-server.sh
```

### Run client

```bash
java --module-path client/target/lib --add-modules javafx.controls -jar client/target/client-1.0-SNAPSHOT.jar localhost 3000
```

Or:

```bash
./run-client.sh localhost 3000
```

Then enter a username (or leave empty for read-only) and click **Join Chat**.

### Multiple clients (see messages from both)

You can have **several clients** in the same chat; each client sees messages from all the others.

1. **Start the server once** (one terminal or IntelliJ: run **TCPServer**).
2. **Start the first client**: run **TCPClient** (or `./run-client.sh localhost 3000`). Enter a username (e.g. **Alice**) and click **Join Chat**.
3. **Start the second client**: run **TCPClient** again (another Run in IntelliJ, or open a new terminal and run the client again). Enter a different username (e.g. **Bob**) and click **Join Chat**.
4. In **Alice’s window**, type a message and press **SEND**. It appears in Alice’s chat and in **Bob’s window**.
5. In **Bob’s window**, type a message and send. It appears in Bob’s chat and in **Alice’s window**.

All connected clients share the same chat: every message (except commands like `allUsers`) is broadcast by the server to every other client, so everyone sees everyone’s messages. You can run more than two clients the same way (run the client again for each new user).

### IntelliJ IDEA

1. Open the project as a Maven project.
2. Set Project SDK to 17.
3. Run **Maven → Lifecycle → package** once (so `server/target/lib` and `client/target/lib` exist).
4. Use the run configurations **TCPServer** and **TCPClient** (they include the required VM options: `--module-path ... --add-modules javafx.controls`).
5. Run **TCPServer** first, then **TCPClient** (select TCPClient in the dropdown and run again). In the client window: username → **Join Chat** → type in the message field and use **SEND** or **Enter**.

---

## Configuration

Server network settings are in:

**`server/src/main/resources/config.properties`**

```properties
server.host=0.0.0.0
server.port=3000
```

Change `server.port` (and optionally `server.host`) as needed. Rebuild so the updated file is copied to `target/`. The client must use the same host and port (e.g. `localhost 3000`) when starting.

---

## Usage & Commands

| Action           | What to do                                      |
|------------------|-------------------------------------------------|
| Send a message   | Type in the message box → **SEND** or **Enter** |
| List all users   | Type **`allUsers`** and send                    |
| Disconnect       | Type **`bye`** or **`end`** and send            |

Sent messages appear in your own chat area and are broadcast to others. The server adds `[username] HH:mm` to each message.

---

## Testing the Chat

1. Start the **server** (see [Run server](#run-server)). A window titled **Group Chat - Server** appears with status and log.
2. Start the **client** (see [Run client](#run-client)). A window titled **Group Chat - Client** appears.
3. In the client: enter a username → **Join Chat**. You should see the chat screen with “Online” and the message input at the bottom.
4. Send a message: type in the box and press **SEND** or **Enter**. It appears in your chat area and (if another client is connected) in theirs.
5. Start a **second client** (run the client again). Both clients see join/leave messages; the server list shows both users with different colors.
6. In any client, type **`allUsers`** and send. The chat area shows the list of active users.
7. Type **`bye`** or **`end`** in one client to disconnect; the server list and the other client update.
8. **Read-only:** Start a client, leave the username empty, click **Join Chat**. The input is disabled; the user can only read messages.

---

## Project Structure

```
Paradigms_Project1/
├── pom.xml                    # Parent Maven POM (modules: server, client)
├── mvnw                       # Maven wrapper script
├── run-server.sh              # Run server JAR
├── run-client.sh              # Run client JAR (args: host port)
├── server/
│   ├── pom.xml
│   ├── src/main/java/com/chat/server/
│   │   ├── TCPServer.java     # Server main, JavaFX entry
│   │   ├── ServerModel.java   # Sockets, broadcast, client list
│   │   ├── ServerView.java    # Server JavaFX UI
│   │   └── ConfigLoader.java  # Load config.properties
│   └── src/main/resources/
│       ├── config.properties  # server.host, server.port
│       └── server.css
├── client/
│   ├── pom.xml
│   ├── src/main/java/com/chat/client/
│   │   ├── TCPClient.java     # Client main, JavaFX entry
│   │   ├── ClientModel.java   # Sockets, send/receive
│   │   └── ClientView.java    # Client JavaFX UI (login + chat)
│   └── src/main/resources/
│       └── client.css
└── .idea/runConfigurations/   # IntelliJ run configs (TCPServer, TCPClient)
```

---

## License

This project is for educational use (Mini Project 1 — Paradigms).
