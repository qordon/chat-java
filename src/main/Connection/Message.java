package main.Connection;

import java.io.Serializable;
import java.util.Set;

public class Message implements Serializable{
    private final MessageType typeMessage;
    private String from;
    private String to;
    private final String textMessage;
    private final Set<String> listUsers;

    public Message(MessageType typeMessage, String textMessage) {
        this.textMessage = textMessage;
        this.typeMessage = typeMessage;
        this.listUsers = null;
    }

    public Message(MessageType typeMessage, String textMessage, String from, String to) {
        this.textMessage = textMessage;
        this.typeMessage = typeMessage;
        this.from = from;
        this.to = to;
        this.listUsers = null;
    }

    public Message(MessageType typeMessage, Set<String> listUsers) {
        this.typeMessage = typeMessage;
        this.textMessage = null;
        this.listUsers = listUsers;
    }

    public Message(MessageType typeMessage) {
        this.typeMessage = typeMessage;
        this.textMessage = null;
        this.listUsers = null;
    }

    public Message(MessageType typeMessage, String[] text) {
        this.typeMessage = typeMessage;
        this.textMessage = text[0] + " " + text[1];
        this.listUsers = null;
    }

    public MessageType getTypeMessage() {
        return typeMessage;
    }

    public Set<String> getListUsers() {
        return listUsers;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getTextMessage() {
        return textMessage;
    }
}
