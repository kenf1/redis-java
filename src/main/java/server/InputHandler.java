package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class InputHandler {
    public final Map<String, String> db;
    public final Map<String,Long> expiryDB;
    private final int port;
    private volatile boolean listening;

    public InputHandler(Map<String, String> db,Map<String,Long> expiryDB, int port) {
        this.db = db;
        this.expiryDB = expiryDB;
        this.port = port;
        this.listening = true;
    }

    private static void checkEmptyInput(BufferedWriter clientOutput, String val) throws IOException {
        if (val != null) {
            clientOutput.write("$" + val.length() + "\r\n" + val + "\r\n");
        } else {
            clientOutput.write("$-1\r\n");
        }
    }

    public void setExpiry(BufferedWriter clientOutput, String[] args) {
        if (args.length >= 5 && "px".equalsIgnoreCase(args[3]) && args[4] != null) {
            try {
                long pxMillis = Long.parseLong(args[4]);
                expiryDB.put(args[1], System.currentTimeMillis() + pxMillis);
            } catch (NumberFormatException e) {
                System.out.println("Error: " + e);
            }
        }
    }

    public Map<String, String> db() {
        return db;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);

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

    public void stopServer() {
        listening = false;
    }

    //set public to avoid reflection
    public boolean handleInput(
            BufferedWriter clientOutput, BufferedReader clientInput
    ) throws IOException {
        String line = clientInput.readLine(); //determine number of arguments *<num>\r\n
        if (line == null || !line.startsWith("*")) {
            clientOutput.write("-ERR Protocol error: expected '*'\r\n");
            clientOutput.flush();
            return false;
        }

        int numArgs;
        try {
            numArgs = Integer.parseInt(line.substring(1));
        } catch (NumberFormatException e) {
            clientOutput.write("-ERR Protocol error: invalid multibulk length\r\n");
            clientOutput.flush();
            return false;
        }

        if (numArgs < 1) {
            clientOutput.write("-ERR Protocol error: invalid number of arguments\r\n");
            clientOutput.flush();
            return false;
        }

        String[] args = HandlerHelper.tryHandleBulkString(clientOutput, clientInput, numArgs);
        if (args == null) return false;

        String command = args[0];
        if (command == null) {
            clientOutput.write("-ERR Protocol error: Command is null\r\n");
            clientOutput.flush();
            return false;
        }

        switch (command.toLowerCase()) {
            case "ping":
                clientOutput.write("+PONG\r\n");
                clientOutput.flush();
                break;
            case "echo":
                if (args.length >= 2 && args[1] != null) {
                    String echoArg = args[1];
                    clientOutput.write("$" + echoArg.length() + "\r\n" + echoArg + "\r\n");
                } else {
                    clientOutput.write("-ERR wrong number of arguments for 'echo' command\r\n");
                }
                clientOutput.flush();
                break;
            case "set":
                if (args.length >= 3 && args[1] != null && args[2] != null) {
                    db.put(args[1], args[2]);
                    setExpiry(clientOutput, args);

                    clientOutput.write("+OK\r\n");
                } else {
                    clientOutput.write("-ERR wrong number of arguments for 'set' command\r\n");
                }

                clientOutput.flush();
                break;
            case "get":
                if (args.length >= 2 && args[1] != null) {
                    String key = args[1];
                    checkExpiry(clientOutput, key);

                    String val = db.get(args[1]);
                    checkEmptyInput(clientOutput, val);
                } else {
                    clientOutput.write("-ERR wrong number of arguments for 'get' command\r\n");
                }
                clientOutput.flush();
                break;
            default:
                clientOutput.write("-ERR unknown command\r\n");
                clientOutput.flush();
                break;
        }
        return false;
    }

    private void checkExpiry(BufferedWriter clientOutput, String key) throws IOException {
        if(expiryDB.containsKey(key)){
            long expiryTime = expiryDB.get(key);

            if(System.currentTimeMillis() > expiryTime){
                expiryDB.remove(key);
                db.remove(key);

                clientOutput.write("$-1\r\n");
                clientOutput.flush();
            }
        }
    }

    private void processBuffer(Socket clientSocket) {
        try (BufferedReader clientInput = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter clientOutput = new BufferedWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            while (handleInput(clientOutput, clientInput)) {
                System.out.println("Handled input...");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
