package main.Client;

import java.util.HashSet;
import java.util.Set;

public class ClientModel {

    private Set<String> allUserNicknames = new HashSet<>();

    protected Set<String> getAllNickname() {
        return allUserNicknames;
    }

    protected void addUser(String nickname) {
        allUserNicknames.add(nickname);
    }

    protected void deleteUser(String nickname) {
        allUserNicknames.remove(nickname);
    }

    protected void setUsers(Set<String> users) {
        this.allUserNicknames = users;
    }
}