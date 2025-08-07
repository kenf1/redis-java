package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class InputHandler {
    public static final Map<String, String> db = new HashMap<>();

    public static void mainWrapper(int port, boolean listening) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);

            //todo: fix loop
            while (listening) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                new Thread(() -> processBuffer(clientSocket))
                        .start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            System.exit(-1);
        }
    }

    private static void processBuffer(Socket clientSocket) {
        try (BufferedReader clientInput = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter clientOutput = new BufferedWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            String content;

            while ((content = clientInput.readLine()) != null) {
                handleInput(content, clientOutput, clientInput);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //set to public to avoid reflection
    public static void handleInput(
            String content, BufferedWriter clientOutput, BufferedReader clientInput
    ) throws IOException {
        String[] contentParams = content.split(" ");

        switch (contentParams[0].toLowerCase()) {
            case "ping":
                clientOutput.write("+PONG\r\n");
                clientOutput.flush();
                break;
            case "echo":
                if (contentParams.length >= 2) {
                    String echoArgument = content.substring(5).trim();
                    clientOutput.write("$" + echoArgument.length() + "\r\n" + echoArgument + "\r\n");
                } else {
                    clientOutput.write("-ERR incorrect number of arguments for 'echo' command\r\n");
                }

                clientOutput.flush();
                break;
            case "set":
                if (contentParams.length >= 3) {
                    String key = contentParams[1];
                    String value = contentParams[2];
                    db.put(key, value);
                    clientOutput.write("+OK\r\n");
                } else {
                    clientOutput.write("-ERR syntax error\r\n");
                }

                clientOutput.flush();
                break;
            case "get":
                if (contentParams.length >= 2) {
                    String key = contentParams[1];
                    String value = db.get(key);
                    if (value != null) {
                        clientOutput.write("$" + value.length() + "\r\n" + value + "\r\n");
                    } else {
                        clientOutput.write("$-1\r\n"); // Null bulk string
                    }
                } else {
                    clientOutput.write("-ERR incorrect number of arguments for 'get' command\r\n");
                }

                clientOutput.flush();
                break;
            default:
                clientOutput.write("-ERR unknown command\r\n");
                clientOutput.flush();
                break;
        }
    }
}
