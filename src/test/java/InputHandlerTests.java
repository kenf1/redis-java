import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.InputHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputHandlerTests {
    //hashmap in memory
    @BeforeEach
    void clearDb() {
        InputHandler.db.clear();
    }

    @Test
    void testPingResponse() throws Exception {
        String respInput = "*1\r\n$4\r\nPING\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        assertEquals("+PONG\r\n", output.toString());
    }

    @Test
    void testEchoResponse() throws Exception {
        String respInput = "*2\r\n$4\r\nECHO\r\n$5\r\nhello\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        assertEquals("$5\r\nhello\r\n", output.toString());
    }

    @Test
    void testInvalidInput() throws Exception {
        String respInput = "*1\r\n$6\r\nfoobar\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        assertEquals("-ERR unknown command\r\n", output.toString());
    }

    @Test
    void testSetResponse() throws Exception {
        String respInput = "*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        assertEquals("+OK\r\n", output.toString());
        assertEquals("bar", InputHandler.db.get("foo"));
    }

    @Test
    void testGetExistingKey() throws Exception {
        InputHandler.db.put("fookey", "fooval");

        String respInput = "*2\r\n$3\r\nGET\r\n$6\r\nfookey\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        assertEquals("$6\r\nfooval\r\n", output.toString());
    }

    @Test
    void testGetMissingKey() throws Exception {
        String respInput = "*2\r\n$3\r\nGET\r\n$7\r\nmissing\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        assertEquals("$-1\r\n", output.toString());
    }

    @Test
    void testSetIncorrectNumberArguments() throws Exception {
        String respInput = "*2\r\n$3\r\nSET\r\n$3\r\nfoo\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        assertEquals("-ERR wrong number of arguments for 'set' command\r\n", output.toString());
    }

    @Test
    void testGetIncorrectNumberArguments() throws Exception {
        String respInput = "*1\r\n$3\r\nGET\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(respInput));

        StringWriter output = new StringWriter();
        BufferedWriter writer = new BufferedWriter(output);

        InputHandler.handleInput(writer, reader);
        writer.flush();

        assertEquals("-ERR wrong number of arguments for 'get' command\r\n", output.toString());
    }
}
