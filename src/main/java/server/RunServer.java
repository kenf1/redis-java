package server;

import java.io.IOException;
import java.net.ServerSocket;

public class RunServer {
    public static void main(String[] args) {
        int port = 6379;
        AllDB db = new AllDB();
        ExecuteCommand executor = new ExecuteCommand(db);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Server listening on " + port);
            while (true) {
                new Thread(new ClientHandler(serverSocket.accept(), executor)).start();
            }
        } catch (IOException e) {
            System.out.println("Error occurred");
        }
    }
}
