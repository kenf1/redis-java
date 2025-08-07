import org.junit.jupiter.api.Test;
import server.InputHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InputHandlerTests {
    @Test
    void testPingResponse() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("ping", writer, mock(BufferedReader.class));

        writer.flush();
        assertEquals("+PONG\r\n", output.toString());
    }

    @Test
    void testEchoResponse() throws Exception {
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine()).thenReturn("5", "hello");

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("echo", writer, mockReader);

        writer.flush();
        assertEquals("5\r\nhello\r\n", output.toString());
    }

    @Test
    void testInvalidInput() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("foobar", writer, mock(BufferedReader.class));

        writer.flush();
        assertEquals("Invalid input\r\n", output.toString());
    }
}
