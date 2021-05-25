package main.Client;

import main.Connection.Message;
import main.Connection.MessageType;
import main.Connection.Network;
import main.Database.SQLService;
import main.Config.Config;
import main.Validator.Validator;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

/**
 * @author Zurbaevi Nika
 */
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
                    nickname = SQLService.getNickname(nickname);
                    connection.send(new Message(MessageType.NICKNAME, nickname));
                }
                if (message.getTypeMessage() == MessageType.NICKNAME_USED) {
                    view.errorDialogWindow("A user with this name is already in the chat");
                    disableClient();
                    break;
                }
                if (message.getTypeMessage() == MessageType.NICKNAME_ACCEPTED) {
                    view.addMessage(String.format("Your name is accepted (%s)\n", nickname));
                    model.setUsers(message.getListUsers());
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                view.errorDialogWindow("An error occurred while registering the name. Try reconnecting");
                try {
                    connection.close();
                    clientConnected = false;
                    break;
                } catch (IOException ex) {
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
                    processingOfPrivateMessagesForSending(message);
                }
                if (message.getTypeMessage() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message);
                }
                if (message.getTypeMessage() == MessageType.REMOVED_USER) {
                    informAboutDeletingNewUser(message);
                }
            } catch (Exception e) {
                view.errorDialogWindow("An error occurred while receiving a message from the server.");
                setClientConnected(false);
                view.refreshListUsers(model.getAllNickname());
                break;
            }
        }
    }

    public boolean isClientConnected() {
        return clientConnected;
    }

    public void setClientConnected(boolean clientConnected) {
        this.clientConnected = clientConnected;
    }

    protected void informAboutAddingNewUser(Message message) {
        model.addUser(message.getTextMessage());
        view.refreshListUsers(model.getAllNickname());
        view.addMessage(String.format("(%s) has joined the chat.\n", message.getTextMessage()));
    }

    protected void informAboutDeletingNewUser(Message message) {
        model.deleteUser(message.getTextMessage());
        view.refreshListUsers(model.getAllNickname());
        view.addMessage(String.format("(%s) has left the chat.\n", message.getTextMessage()));
    }

    protected void processingOfPrivateMessagesForSending(Message message) {
        String[] data = message.getTextMessage().split(" ");
        StringBuilder mess = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++)
        {
            mess.append(data[i]).append(" ");
        }

//        StringBuilder formattingForSendingPrivateMessage = new StringBuilder();
//        for (int i = 1; i < data.length - 1; i++) {
//            formattingForSendingPrivateMessage.append(data[i]).append(" ");
//        }
//        view.addMessage(String.format("Private message from (%s): %s\n", data[data.length - 1], formattingForSendingPrivateMessage.toString()));
        view.addMessage(String.format("Private message from (%s): %s\n", data[data.length-1], mess));
    }

    protected void notifyNicknameChanged(Message message) {
        String[] data = message.getTextMessage().split(" ");
        view.addMessage(message.getTextMessage() + "\n");
        model.deleteUser(data[0]);
        model.addUser(data[data.length - 1]);
        view.refreshListUsers(model.getAllNickname());
    }

    protected void processIncomingMessage(Message message) {
        view.addMessage(message.getTextMessage());
    }

    protected void disableClient() {
        try {
            if (clientConnected) {
                connection.send(new Message(MessageType.DISABLE_USER));
                model.getAllNickname().clear();
                clientConnected = false;
                view.refreshListUsers(model.getAllNickname());
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
                view.addMessage(String.format("Private message sent to user (%s)\n", userSelected));
//                connection.send(new Message(MessageType.PRIVATE_TEXT_MESSAGE, text));
                connection.send(new Message(MessageType.PRIVATE_TEXT_MESSAGE, text + " " +userSelected));
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
