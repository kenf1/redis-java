package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ExecuteCommand executor;
    private final RespParser parser = new RespParser();

    public ClientHandler(Socket socket, ExecuteCommand executor) {
        this.socket = socket;
        this.executor = executor;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()))
        ) {
            while (!socket.isClosed()) {
                try {
                    String[] args = parser.parse(in);
                    executor.execute(args, out);
                } catch (IOException e) {
                    out.write("-ERR " + e.getMessage() + "\r\n");
                    out.flush();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e);
        }
    }
}
