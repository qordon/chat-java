package main.Client;

import main.Connection.Message;
import main.Connection.MessageType;
import main.Connection.Network;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ClientController {

    private Network connection;
    private ClientModel model;
    private ClientView view;

    private volatile boolean clientConnected;
    private String nickname;
    private boolean isDatabaseConnected;

    public void run() {
        model = new ClientModel();
        view = new ClientView(this);
        view.initComponents();
        while (true) {
            if (this.isClientConnected()) {
                this.userNameRegistration();
                this.receiveMessageFromServer();
                this.setClientConnected(false);
            }
        }
    }

    protected void userNameRegistration() {
        while (true) {
            try {
                Message message = connection.receive();
                if (message.getTypeMessage() == MessageType.REQUEST_NICKNAME) {
                    connection.send(new Message(MessageType.NICKNAME, nickname));
                }
                if (message.getTypeMessage() == MessageType.NICKNAME_USED) {
                    view.errorDialogWindow("A user with this name is already in the chat");
                    disableClient();
                    break;
                }
                if (message.getTypeMessage() == MessageType.NICKNAME_ACCEPTED) {
                    view.addMessage(String.format("Your name is accepted (%s)\n", nickname));
                    model.setUsersOnline(message.getListUsers());
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                view.errorDialogWindow("An error occurred while registering the name. Try reconnecting");
                try {
                    connection.close();
                    clientConnected = false;
                    break;
                } catch (IOException exception) {
                    view.errorDialogWindow("Error closing connection");
                }
            }
        }
    }

    protected void receiveMessageFromServer() {
        while (clientConnected) {
            try {
                Message message = connection.receive();
                if (message.getTypeMessage() == MessageType.PRIVATE_TEXT_MESSAGE) {
                    processingOfPrivateMessagesForReceiving(message);
                }
                else if(message.getTypeMessage() == MessageType.DIALOG_HISTORY){
                    System.out.println(message.getTextMessage());
                    processOfDialogHistory(message);
                }
                else if(message.getTypeMessage() == MessageType.ALL_USERS){
                    System.out.println(message.getTextMessage());
                    addAllUsers(message);
                }
                else if (message.getTypeMessage() == MessageType.USER_ADDED) {
                    addUserToOnline(message);
                }
                else if (message.getTypeMessage() == MessageType.REMOVED_USER) {
                    deleteUserFromOnline(message);
                }
            } catch (Exception e) {
                view.errorDialogWindow("An error occurred while receiving a message from the server.");
                setClientConnected(false);
                view.refreshListUsers(model.getUsersOnline());
                break;
            }
        }
    }

    private void processOfDialogHistory(Message message) {
        view.addHistory(message.getTextMessage());
    }

    public boolean isClientConnected() {
        return clientConnected;
    }

    public void setClientConnected(boolean clientConnected) {
        this.clientConnected = clientConnected;
    }

    protected void addAllUsers(Message message){
        String[] strings = message.getTextMessage().split("\n");
        Set<String> users = (new HashSet(Arrays.asList(strings)));
        users.remove(nickname);
        model.setAllUsers(users);
        view.refreshListUsers(model.getAllUsers());
    }

    protected void addUserToOnline(Message message) {
        model.addUserToOnline(message.getTextMessage());
//        view.refreshListUsers(model.getUsersOnline());
    }

    protected void deleteUserFromOnline(Message message) {
        model.removeUserFromOnline(message.getTextMessage());
//        view.refreshListUsers(model.getUsersOnline());
    }

    protected void processingOfPrivateMessagesForReceiving(Message message) {
        view.addMessage(String.format("%s: %s\n", message.getFrom(), message.getTextMessage()));
    }

    protected void getHistory(String to){
        try{
            if(!nickname.equals(to)){
                connection.send(new Message(MessageType.DIALOG_HISTORY, null, nickname, to));
            }
        }
        catch (Exception e) {
            view.errorDialogWindow("Error getting dialog history");
        }
    }

    protected void disableClient() {
        try {
            if (clientConnected) {
                connection.send(new Message(MessageType.DISABLE_USER));
                model.getUsersOnline().clear();
                clientConnected = false;
                view.refreshListUsers(model.getUsersOnline());
                view.addMessage("You have disconnected from the server.\n");
            } else {
                view.errorDialogWindow("You are already disconnected.");
            }
        } catch (Exception e) {
            view.errorDialogWindow("An error occurred while disconnecting.");
        }
    }

    protected void connectToServer() {
        if (!clientConnected) {
            while (true) {
                try {
                    connection = new Network(new Socket(view.getServerAddress(), view.getPort()));
                    clientConnected = true;
                    view.addMessage("You have connected to the server.\n");
                    break;
                } catch (Exception e) {
                    view.errorDialogWindow("An error has occurred! Perhaps you entered the wrong server address or port. try again");
                    break;
                }
            }
        } else {
            view.errorDialogWindow("You are already connected!");
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    protected void sendPrivateMessageOnServer(String userSelected, String text) {
        try {
            if (!nickname.equals(userSelected)) {
                view.addMessage(String.format(nickname + ": %s\n", text));
                connection.send(new Message(MessageType.PRIVATE_TEXT_MESSAGE, text, nickname,
                                            userSelected, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
            } else {
                view.errorDialogWindow("You cannot send a private message to yourself");
            }
        } catch (Exception e) {
            view.errorDialogWindow("Error sending message");
        }
    }

    public boolean isDatabaseConnected() {
        return isDatabaseConnected;
    }

    public void setDatabaseConnected(boolean databaseConnected) {
        isDatabaseConnected = databaseConnected;
    }
}
