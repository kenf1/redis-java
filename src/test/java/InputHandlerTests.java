import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.InputHandler;

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

        String output = TestsHelper.runInputHandler(respInput);

        assertEquals("+PONG\r\n", output);
    }

    @Test
    void testEchoResponse() throws Exception {
        String respInput = "*2\r\n$4\r\nECHO\r\n$5\r\nhello\r\n";

        String output = TestsHelper.runInputHandler(respInput);

        assertEquals("$5\r\nhello\r\n", output);
    }

    @Test
    void testInvalidInput() throws Exception {
        String respInput = "*1\r\n$6\r\nfoobar\r\n";

        String output = TestsHelper.runInputHandler(respInput);

        assertEquals("-ERR unknown command\r\n", output);
    }

    @Test
    void testSetResponse() throws Exception {
        String respInput = "*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n";

        String output = TestsHelper.runInputHandler(respInput);

        assertEquals("+OK\r\n", output);
        assertEquals("bar", InputHandler.db.get("foo"));
    }

    @Test
    void testGetExistingKey() throws Exception {
        InputHandler.db.put("fookey", "fooval");

        String respInput = "*2\r\n$3\r\nGET\r\n$6\r\nfookey\r\n";

        String output = TestsHelper.runInputHandler(respInput);

        assertEquals("$6\r\nfooval\r\n", output);
    }

    @Test
    void testGetMissingKey() throws Exception {
        String respInput = "*2\r\n$3\r\nGET\r\n$7\r\nmissing\r\n";

        String output = TestsHelper.runInputHandler(respInput);

        assertEquals("$-1\r\n", output);
    }

    @Test
    void testSetIncorrectNumberArguments() throws Exception {
        String respInput = "*2\r\n$3\r\nSET\r\n$3\r\nfoo\r\n";

        String output = TestsHelper.runInputHandler(respInput);

        assertEquals("-ERR wrong number of arguments for 'set' command\r\n", output);
    }

    @Test
    void testGetIncorrectNumberArguments() throws Exception {
        String respInput = "*1\r\n$3\r\nGET\r\n";

        String output = TestsHelper.runInputHandler(respInput);

        assertEquals("-ERR wrong number of arguments for 'get' command\r\n", output);
    }
}
