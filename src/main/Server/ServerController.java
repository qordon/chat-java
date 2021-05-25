package main.Server;

import main.Connection.Message;
import main.Connection.MessageType;
import main.Connection.Network;
import main.Database.SQLService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerController {
    private ServerView gui;
    private ServerModel model;
    private ServerSocket serverSocket;
    private volatile boolean isServerStart;

    public void run() {
        gui = new ServerView(this);
        model = new ServerModel();
        gui.initComponents();
        while (true) {
            if (isServerStart) {
                this.acceptServer();
                isServerStart = false;
            }
        }
    }

    protected void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isServerStart = true;
            gui.refreshDialogWindowServer("Server started.\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Server failed to start.\n");
        }
    }

    protected void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Network> user : model.getAllUsersChat().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getAllUsersChat().clear();
                isServerStart = false;
                gui.refreshDialogWindowServer("Server stopped.\n");
            } else {
                gui.refreshDialogWindowServer("The server is not running - there is nothing to stop!\n");
            }
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Server could not be stopped.\n");
        }
    }

    protected void acceptServer() {
        while (true) {
            try {
                new ServerThread(serverSocket.accept()).start();
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Server connection lost.\n");
                break;
            }
        }
    }

    protected void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Network> user : model.getAllUsersChat().entrySet()) {
            try {
                user.getValue().send(message);
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Error sending message to all users!\n");
            }
        }
    }

    protected void sendPrivateMessage(Message message) {
        System.out.println(message.getTextMessage());
        for (Map.Entry<String, Network> user : model.getAllUsersChat().entrySet()) {
            try {
                if (user.getKey().equals(message.getFrom())) {
                    user.getValue().send(message);
                }
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Error sending message to all users!\n");
            }
        }
    }

    public boolean isServerStart() {
        return isServerStart;
    }

    private class ServerThread extends Thread {

        private final Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        private String requestAndAddingUser(Network connection) {
            while (true) {
                try {
                    connection.send(new Message(MessageType.REQUEST_NICKNAME));
                    Message responseMessage = connection.receive();
                    String nickname = responseMessage.getTextMessage();
                    if (responseMessage.getTypeMessage() == MessageType.NICKNAME && nickname != null && !nickname.isEmpty() && !model.getAllUsersChat().containsKey(nickname)) {
                        model.addUser(nickname, connection);
                        Set<String> listUsers = new HashSet<>();
                        for (Map.Entry<String, Network> users : model.getAllUsersChat().entrySet()) {
                            listUsers.add(users.getKey());
                        }
                        connection.send(new Message(MessageType.NICKNAME_ACCEPTED, listUsers));
                        sendMessageAllUsers(new Message(MessageType.USER_ADDED, nickname));
                        return nickname;
                    } else {
                        connection.send(new Message(MessageType.NICKNAME_USED));
                    }
                } catch (Exception e) {
                    gui.refreshDialogWindowServer("There was an error requesting and adding a new user\n");
                    return null;
                }
            }
        }

        private void messagingBetweenUsers(Network network, String nickname) {
            while (true) {
                try {
                    Message message = network.receive();
//                    if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
//                        sendMessage(nickname, message);
//                        SQLService.savingUserMessages(String.format("%s: %s\n", nickname, message.getTextMessage()));
//                    }
                    if (message.getTypeMessage() == MessageType.PRIVATE_TEXT_MESSAGE) {
//                        sendPrivateMessage(new Message(MessageType.PRIVATE_TEXT_MESSAGE, message.getTextMessage() + " " + nickname));
//                        sendPrivateMessage(new Message(MessageType.PRIVATE_TEXT_MESSAGE, message.getTextMessage(),
//                                                        message.getFrom(), message.getTo()));
                        SQLService.savingUserMessages(message);
                        sendPrivateMessage(message);
//                        SQLService.savingUserMessages("*" + message.getTextMessage() + " - (" + nickname + ")");

                    }
//                    if (message.getTypeMessage() == MessageType.DISABLE_USER) {
//                        disableUser(nickname, network);
//                        SQLService.savingUserMessages((nickname + ": disconnected"));
//                        break;
//                    }
                } catch (Exception e) {
                    gui.refreshDialogWindowServer(String.format("An error occurred while sending a message from the user %s, either disconnected!\n", nickname));
                    model.removeUser(nickname);
                    break;
                }
            }
        }


        private void disableUser(String nickname, Network network) throws IOException {
            sendMessageAllUsers(new Message(MessageType.REMOVED_USER, nickname));
            model.removeUser(nickname);
            network.close();
            gui.refreshDialogWindowServer(String.format("Remote access user %s disconnected.\n", socket.getRemoteSocketAddress()));
        }

        @Override
        public void run() {

            gui.refreshDialogWindowServer(String.format("A new user connected with a remote socket - %s.\n", socket.getRemoteSocketAddress()));
            try {
                Network connection = new Network(socket);
                messagingBetweenUsers(connection, requestAndAddingUser(connection));
            } catch (Exception e) {
                gui.refreshDialogWindowServer("An error occurred while sending a message from the user!\n");
            }
        }

    }

}
