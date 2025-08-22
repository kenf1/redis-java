package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class InputHandler {
    //dependency injection
    public final Map<String, String> db;
    private final int port;
    private final boolean listening;

    public InputHandler(Map<String, String> db, int port, boolean listening) {
        this.db = db;
        this.port = port;
        this.listening = listening;
    }

    public void run() {
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

    //set to public to avoid reflection
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

        String[] args = new String[numArgs];
        for (int i = 0; i < numArgs; i++) {
            String bulkLenLine = clientInput.readLine();
            if (bulkLenLine == null || !bulkLenLine.startsWith("$")) {
                clientOutput.write("-ERR Protocol error: expected '$' line\r\n");
                clientOutput.flush();
                return false;
            }

            int bulkLen;
            try {
                bulkLen = Integer.parseInt(bulkLenLine.substring(1));
            } catch (NumberFormatException e) {
                clientOutput.write("-ERR Protocol error: invalid bulk length\r\n");
                clientOutput.flush();
                return false;
            }

            if (bulkLen < 0) {
                args[i] = null;
                clientInput.readLine();
                continue;
            }

            char[] buf = new char[bulkLen];
            int read = 0;
            while (read < bulkLen) {
                int r = clientInput.read(buf, read, bulkLen - read);
                if (r == -1) {
                    clientOutput.write("-ERR Protocol error: unexpected stream end\r\n");
                    clientOutput.flush();
                    return false;
                }
                read += r;
            }

            args[i] = new String(buf);

            // read ending \r\n after bulk string data
            String crlf = clientInput.readLine();
            if (crlf == null) {
                clientOutput.write("-ERR Protocol error: expected CRLF after bulk string\r\n");
                clientOutput.flush();
                return false;
            }
        }

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
                    clientOutput.write("+OK\r\n");
                } else {
                    clientOutput.write("-ERR wrong number of arguments for 'set' command\r\n");
                }
                clientOutput.flush();
                break;
            case "get":
                if (args.length >= 2 && args[1] != null) {
                    String val = db.get(args[1]);
                    if (val != null) {
                        clientOutput.write("$" + val.length() + "\r\n" + val + "\r\n");
                    } else {
                        clientOutput.write("$-1\r\n");
                    }
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
