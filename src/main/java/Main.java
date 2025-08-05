import server.InputHandler;

public class Main {
    public static void main(String[] args) {
        int port = 6379;

        InputHandler.mainWrapper(port, true);
    }
}
