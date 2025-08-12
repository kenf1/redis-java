package server;

import java.util.HashMap;
import java.util.Map;

public class AllDB {
    private final Map<String, String> db = new HashMap<>();
    private final Map<String, Long> expiryDb = new HashMap<>();

    public synchronized void set(String key, String value, Long pxMillis) {
        db.put(key, value);
        if (pxMillis != null) {
            expiryDb.put(key, System.currentTimeMillis() + pxMillis);
        } else {
            expiryDb.remove(key);
        }
    }

    public synchronized String get(String key) {
        if (expiryDb.containsKey(key)) {
            long expiry = expiryDb.get(key);
            if (System.currentTimeMillis() > expiry) {
                db.remove(key);
                expiryDb.remove(key);
                return null;
            }
        }
        return db.get(key);
    }
}
