import server.InputHandler;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        InputHandler handler = new InputHandler(new HashMap<>(), 6379);
        handler.startServer();
    }
}
