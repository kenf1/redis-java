package server;

import java.io.BufferedReader;
import java.io.IOException;

public class RespParser {
    public String[] parse(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();
        if (firstLine == null || !firstLine.startsWith("*")) {
            throw new IOException("Protocol error: expected '*'");
        }

        int numArgs = Integer.parseInt(firstLine.substring(1));
        if (numArgs < 1) {
            throw new IOException("Invalid number of arguments");
        }

        String[] args = new String[numArgs];
        for (int i = 0; i < numArgs; i++) {
            String bulkLengthLine = reader.readLine();
            if (bulkLengthLine == null || !bulkLengthLine.startsWith("$")) {
                throw new IOException("Protocol error: expected '$'");
            }
            int bulkLen = Integer.parseInt(bulkLengthLine.substring(1));
            char[] buf = new char[bulkLen];
            int read = 0;
            while (read < bulkLen) {
                int r = reader.read(buf, read, bulkLen - read);
                if (r == -1) throw new IOException("Unexpected stream end");
                read += r;
            }
            args[i] = new String(buf);
            reader.readLine(); // CRLF
        }
        return args;
    }
}
