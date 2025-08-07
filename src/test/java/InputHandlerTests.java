import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.InputHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

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
        String content = "echo hello";

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(content, writer, mockReader);
        writer.flush();

        assertEquals("$5\r\nhello\r\n", output.toString());
    }

    @Test
    void testInvalidInput() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("foobar", writer, mock(BufferedReader.class));
        writer.flush();

        assertEquals("-ERR unknown command\r\n", output.toString());
    }

    @BeforeEach
    void clearDb() {
        InputHandler.db.clear();
    }

    @Test
    void testSetResponse() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("set foo bar", writer, mock(BufferedReader.class));
        writer.flush();

        assertEquals("+OK\r\n", output.toString());
        assertEquals("bar", InputHandler.db.get("foo"));
    }

    @Test
    void testGetExistingKey() throws Exception {
        InputHandler.db.put("fookey", "fooval");

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("get fookey", writer, mock(BufferedReader.class));
        writer.flush();

        assertEquals("$6\r\nfooval\r\n", output.toString());
    }

    @Test
    void testGetMissingKey() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("get missing", writer, mock(BufferedReader.class));
        writer.flush();

        assertEquals("$-1\r\n", output.toString());
    }

    @Test
    void testSetIncorrectNumberArguments() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("set foo", writer, mock(BufferedReader.class));
        writer.flush();

        assertEquals("-ERR syntax error\r\n", output.toString());
    }

    @Test
    void testGetIncorrectNumberArguments() throws Exception {
        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput("get", writer, mock(BufferedReader.class));
        writer.flush();

        assertEquals("-ERR incorrect number of arguments for 'get' command\r\n", output.toString());
    }
}
