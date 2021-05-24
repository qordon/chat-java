package main.Server;

import main.Connection.Network;
import java.util.HashMap;
import java.util.Map;

public class ServerModel {
    private final Map<String, Network> allUsers = new HashMap<>();

    protected Map<String, Network> getAllUsersChat() {
        return allUsers;
    }

    protected String getName(String nickname) {
        return allUsers.get(nickname).toString();
    }

    protected Network getConnection(String nickname) {
        return allUsers.get(nickname);
    }

    protected void addUser(String nickname, Network connection) {
        allUsers.put(nickname, connection);
    }

    protected void removeUser(String nickname) {
        allUsers.remove(nickname);
    }
}
