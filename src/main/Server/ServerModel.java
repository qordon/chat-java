package main.Server;

import main.Connection.Network;
import java.util.HashMap;
import java.util.Map;

public class ServerModel {
    private final Map<String, Network> usersOnline = new HashMap<>();

    protected Map<String, Network> getUsersOnline() {
        return usersOnline;
    }

    protected String getName(String nickname) {
        return usersOnline.get(nickname).toString();
    }

    protected Network getConnection(String nickname) {
        return usersOnline.get(nickname);
    }

    protected void addUserToOnline(String nickname, Network connection) {
        usersOnline.put(nickname, connection);
    }

    protected void removeUserFromOnline(String nickname) {
        usersOnline.remove(nickname);
    }
}
