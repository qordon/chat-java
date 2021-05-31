package main.Server;

import main.Connection.Message;
import main.Connection.MessageType;
import main.Connection.Network;
import main.Database.SQLService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerController {
    private ServerView view;
    private ServerModel model;
    private ServerSocket serverSocket;
    private volatile boolean isServerStart;

    public void run() {
        view = new ServerView(this);
        model = new ServerModel();
        view.initComponents();
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
            view.refreshDialogWindowServer("Server started.\n");
        } catch (Exception e) {
            view.refreshDialogWindowServer("Server failed to start.\n");
        }
    }

    protected void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Network> user : model.getUsersOnline().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getUsersOnline().clear();
                isServerStart = false;
                view.refreshDialogWindowServer("Server stopped.\n");
            } else {
                view.refreshDialogWindowServer("The server is not running - there is nothing to stop!\n");
            }
        } catch (Exception e) {
            view.refreshDialogWindowServer("Server could not be stopped.\n");
        }
    }

    protected void acceptServer() {
        while (true) {
            try {
                new ServerThread(serverSocket.accept()).start();
            } catch (Exception e) {
                view.refreshDialogWindowServer("Server connection lost.\n");
                break;
            }
        }
    }

    protected void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Network> user : model.getUsersOnline().entrySet()) {
            try {
                user.getValue().send(message);
            } catch (Exception e) {
                view.refreshDialogWindowServer("Error sending message to all users!\n");
            }
        }
    }

    protected void sendPrivateMessage(Message message) {
        System.out.println(message.getTextMessage());
        for (Map.Entry<String, Network> user : model.getUsersOnline().entrySet()) {
            try {
                if (user.getKey().equals(message.getTo())) {
                    user.getValue().send(message);
                }
            } catch (Exception e) {
                view.refreshDialogWindowServer("Error sending message to user!\n");
            }
        }
    }

    protected void sendDialogHistory(String history, String from){
        for (Map.Entry<String, Network> user : model.getUsersOnline().entrySet()) {
            try {
                if (user.getKey().equals(from)) {
                    user.getValue().send(new Message(MessageType.DIALOG_HISTORY, history, from, null));
                }
            } catch (Exception e) {
                view.refreshDialogWindowServer("Error sending message to user!\n");
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
                    if (responseMessage.getTypeMessage() == MessageType.NICKNAME && nickname != null && !nickname.isEmpty() && !model.getUsersOnline().containsKey(nickname)) {
                        model.addUserToOnline(nickname, connection);
                        Set<String> listUsers = new HashSet<>();
                        for (Map.Entry<String, Network> users : model.getUsersOnline().entrySet()) {
                            listUsers.add(users.getKey());
                        }
                        connection.send(new Message(MessageType.NICKNAME_ACCEPTED, listUsers));
                        sendMessageAllUsers(new Message(MessageType.USER_ADDED, nickname));
                        connection.send(new Message(MessageType.ALL_USERS, SQLService.getAllUsers()));
                        return nickname;
                    } else {
                        connection.send(new Message(MessageType.NICKNAME_USED));
                    }
                } catch (Exception e) {
                    view.refreshDialogWindowServer("There was an error requesting and adding a new user\n");
                    return null;
                }
            }
        }

        private void messagingBetweenUsers(Network network, String nickname) {
            while (true) {
                try {
                    Message message = network.receive();
                    if (message.getTypeMessage() == MessageType.PRIVATE_TEXT_MESSAGE) {
                        SQLService.savingUserMessages(message);
                        sendPrivateMessage(message);
                    }
                    else if(message.getTypeMessage() == MessageType.DIALOG_HISTORY){
                        ArrayList<Message> messages = SQLService.getDialogHistory(message);
                        StringBuilder output = new StringBuilder();

                        for(Message mess : messages){
                            output.append("[").append(mess.getTime()).append("] ").append(mess.getFrom()).append(": ").append(mess.getTextMessage()).append("\n");
                        }

                        sendDialogHistory(output.toString(), nickname);
                    }
                } catch (Exception e) {
                    view.refreshDialogWindowServer(String.format("An error occurred while sending a message from the user %s, either disconnected!\n", nickname));
                    model.removeUserFromOnline(nickname);
                    break;
                }
            }
        }


        private void disableUser(String nickname, Network network) throws IOException {
            sendMessageAllUsers(new Message(MessageType.REMOVED_USER, nickname));
            model.removeUserFromOnline(nickname);
            network.close();
            view.refreshDialogWindowServer(String.format("Remote access user %s disconnected.\n", socket.getRemoteSocketAddress()));
        }

        @Override
        public void run() {

            view.refreshDialogWindowServer(String.format("A new user connected with a remote socket - %s.\n", socket.getRemoteSocketAddress()));
            try {
                Network connection = new Network(socket);
                messagingBetweenUsers(connection, requestAndAddingUser(connection));
            } catch (Exception e) {
                view.refreshDialogWindowServer("An error occurred while sending a message from the user!\n");
            }
        }

    }

}
