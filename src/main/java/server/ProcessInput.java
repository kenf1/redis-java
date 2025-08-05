package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ProcessInput {
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

    private static void handleInput(
            String content, BufferedWriter clientOutput, BufferedReader clientInput
    ) throws IOException {
        if (content.equalsIgnoreCase("ping")) {
            clientOutput.write("+PONG\r\n");
            clientOutput.flush();
        } else if (content.equalsIgnoreCase("echo")) {
            String numBytes = clientInput.readLine();

            clientOutput.write(numBytes + "\r\n" + clientInput.readLine() + "\r\n");
            clientOutput.flush();
        } else {
            clientOutput.write("Invalid input" + "\r\n");
            clientOutput.flush();
        }
    }
}
