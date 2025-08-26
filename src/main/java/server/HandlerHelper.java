package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class HandlerHelper {
    public static String[] tryHandleBulkString(
            BufferedWriter clientOutput, BufferedReader clientInput, int numArgs
    ) throws IOException {
        String[] args = new String[numArgs];
        for (int i = 0; i < numArgs; i++) {
            String bulkLenLine = clientInput.readLine();
            if (bulkLenLine == null || !bulkLenLine.startsWith("$")) {
                clientOutput.write("-ERR Protocol error: expected '$' line\r\n");
                clientOutput.flush();
                return null;
            }

            int bulkLen;
            try {
                bulkLen = Integer.parseInt(bulkLenLine.substring(1));
            } catch (NumberFormatException e) {
                clientOutput.write("-ERR Protocol error: invalid bulk length\r\n");
                clientOutput.flush();
                return null;
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
                    return null;
                }
                read += r;
            }

            args[i] = new String(buf);

            // read ending \r\n after bulk string data
            String crlf = clientInput.readLine();
            if (crlf == null) {
                clientOutput.write("-ERR Protocol error: expected CRLF after bulk string\r\n");
                clientOutput.flush();
                return null;
            }
        }
        return args;
    }
}
