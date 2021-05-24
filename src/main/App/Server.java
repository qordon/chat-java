package main.App;

import main.Server.ServerController;

public class Server {
    public static void main(String[] args) {
        ServerController serverController = new ServerController();
        serverController.run();
    }
}
