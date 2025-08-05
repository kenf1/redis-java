import server.ProcessInput;

public class Main {
    public static void main(String[] args) {
        int port = 6379;
        boolean listening = true;

        ProcessInput.mainWrapper(port, listening);
    }
}
