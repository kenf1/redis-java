import org.junit.jupiter.api.Test;
import server.InputHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputHandlerTests {
    @Test
    void testPingResponse() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("ping", writer, null);

        writer.flush();
        assertEquals("+PONG\r\n", output.toString());
    }

    @Test
    void testEchoResponse() throws Exception {
        StringReader input = new StringReader("5\nhello\n");

        BufferedReader reader = new BufferedReader(input);

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("echo", writer, reader);

        writer.flush();
        assertEquals("5\r\nhello\r\n", output.toString());
    }

    @Test
    void testInvalidInputResponse() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("foobar", writer, null);

        writer.flush();
        assertEquals("Invalid input\r\n", output.toString());
    }
}

