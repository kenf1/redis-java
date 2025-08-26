package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

public class InMemoryDB {
    public static void getValueFromKey(
            BufferedWriter clientOutput,
            String key,
            Map<String, String> db,
            Map<String, Long> expiryDB
    ) throws IOException {
        if (checkExpiry(key, db, expiryDB)) {
            clientOutput.write("$-1\r\n");
        } else {
            String val = db.get(key);
            checkEmptyInput(clientOutput, val);
        }
    }

    public static boolean checkExpiry(
            String key,
            Map<String, String> db,
            Map<String, Long> expiryDB
    ) {
        if (expiryDB.containsKey(key)) {
            long expiryTime = expiryDB.get(key);
            if (System.currentTimeMillis() > expiryTime) {
                expiryDB.remove(key);
                db.remove(key);
                return true;
            }
        }
        return false;
    }

    public static void setExpiry(
            Map<String, Long> expiryDB,
            String[] args
    ) {
        if (args.length >= 5 && "px".equalsIgnoreCase(args[3]) && args[4] != null) {
            try {
                long pxMillis = Long.parseLong(args[4]);
                expiryDB.put(args[1], System.currentTimeMillis() + pxMillis);
            } catch (NumberFormatException e) {
                System.out.println("Error: invalid PX argument");
            }
        }
    }

    private static void checkEmptyInput(
            BufferedWriter clientOutput, String val
    ) throws IOException {
        if (val != null) {
            clientOutput.write("$" + val.length() + "\r\n" + val + "\r\n");
        } else {
            clientOutput.write("$-1\r\n");
        }
    }
}
