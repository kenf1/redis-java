import server.InputHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class TestsHelper {
    public static String runInputHandler(String respInput) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        return output.toString();
    }
}
