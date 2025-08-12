package server;

import java.io.BufferedWriter;
import java.io.IOException;

public class ExecuteCommand {
    private final AllDB db;

    public ExecuteCommand(AllDB db) {
        this.db = db;
    }

    public void execute(String[] args, BufferedWriter out) throws IOException {
        String cmd = args[0].toLowerCase();

        switch (cmd) {
            case "ping":
                out.write("+PONG\r\n");
                break;
            case "echo":
                if (args.length >= 2) {
                    String msg = args[1];
                    out.write("$" + msg.length() + "\r\n" + msg + "\r\n");
                } else {
                    out.write("-ERR wrong number of arguments\r\n");
                }
                break;
            case "set":
                handleSet(args, out);
                break;
            case "get":
                handleGet(args, out);
                break;
            default:
                out.write("-ERR unknown command\r\n");
        }
        out.flush();
    }

    private void handleSet(String[] args, BufferedWriter out) throws IOException {
        if (args.length < 3) {
            out.write("-ERR wrong number of arguments for 'set'\r\n");
            return;
        }
        Long pxMillis = null;
        if (args.length >= 5 && "px".equalsIgnoreCase(args[3])) {
            try {
                pxMillis = Long.parseLong(args[4]);
            } catch (NumberFormatException e) {
                out.write("-ERR PX value is not a valid integer\r\n");
                return;
            }
        }
        db.set(args[1], args[2], pxMillis);
        out.write("+OK\r\n");
    }

    private void handleGet(String[] args, BufferedWriter out) throws IOException {
        if (args.length < 2) {
            out.write("-ERR wrong number of arguments for 'get'\r\n");
            return;
        }
        String value = db.get(args[1]);
        if (value != null) {
            out.write("$" + value.length() + "\r\n" + value + "\r\n");
        } else {
            out.write("$-1\r\n");
        }
    }
}
