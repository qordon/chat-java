package main.Client;

import java.util.HashSet;
import java.util.Set;

public class ClientModel {

    private Set<String> usersOnline = new HashSet<>();
    private Set<String> allUsers = new HashSet<>();

    protected Set<String> getUsersOnline() {
        return usersOnline;
    }
    protected Set<String> getAllUsers(){
        return allUsers;
    }
    protected void addUserToOnline(String nickname) {
        usersOnline.add(nickname);
    }

    protected void removeUserFromOnline(String nickname) {
        usersOnline.remove(nickname);
    }

    protected void setUsersOnline(Set<String> users) {
        this.usersOnline = users;
    }
    protected void setAllUsers(Set<String> users){
        this.allUsers = users;
    }
}
