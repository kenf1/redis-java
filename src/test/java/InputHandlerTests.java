import org.junit.jupiter.api.Test;
import server.InputHandler;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputHandlerTests {
    @Test
    void testPingResponse() throws Exception {
        String respInput = "*1\r\n$4\r\nPING\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(), new HashMap<>(),6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("+PONG\r\n", output);
    }

    @Test
    void testEchoResponse() throws Exception {
        String respInput = "*2\r\n$4\r\nECHO\r\n$5\r\nhello\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("$5\r\nhello\r\n", output);
    }

    @Test
    void testInvalidInput() throws Exception {
        String respInput = "*1\r\n$6\r\nfoobar\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("-ERR unknown command\r\n", output);
    }

    @Test
    void testSetResponse() throws Exception {
        String respInput = "*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("+OK\r\n", output);
        assertEquals("bar", handler.db().get("foo"));
    }

    @Test
    void testGetExistingKey() throws Exception {
        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        handler.db().put("fookey", "fooval");

        String respInput = "*2\r\n$3\r\nGET\r\n$6\r\nfookey\r\n";

        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("$6\r\nfooval\r\n", output);
    }

    @Test
    void testGetMissingKey() throws Exception {
        String respInput = "*2\r\n$3\r\nGET\r\n$7\r\nmissing\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("$-1\r\n", output);
    }

    @Test
    void testSetIncorrectNumberArguments() throws Exception {
        String respInput = "*2\r\n$3\r\nSET\r\n$3\r\nfoo\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("-ERR wrong number of arguments for 'set' command\r\n", output);
    }

    @Test
    void testGetIncorrectNumberArguments() throws Exception {
        String respInput = "*1\r\n$3\r\nGET\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("-ERR wrong number of arguments for 'get' command\r\n", output);
    }

    @Test
    void testSetWithPXBeforeExpiry() throws Exception {
        String respInput = "*5\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$5\r\nhello\r\n$2\r\nPX\r\n$4\r\n1000\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("+OK\r\n", output);

        //get before expires
        String getCmd = "*2\r\n$3\r\nGET\r\n$5\r\nmykey\r\n";

        InputHandler getHandler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String getOutput = TestsHelper.runInputHandler(getCmd,getHandler);

        assertEquals("$5\r\nhello\r\n", getOutput);
    }

    @Test
    void testSetWithPXAfterExpiry() throws Exception {
        String respInput =
                "*5\r\n$3\r\nSET\r\n$4\r\ntemp\r\n$3\r\nval\r\n$2\r\nPX\r\n$2\r\n50\r\n";

        InputHandler handler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String output = TestsHelper.runInputHandler(respInput,handler);

        assertEquals("+OK\r\n", output);


        //force expire
        Thread.sleep(120);
        String getCmd = "*2\r\n$3\r\nGET\r\n$4\r\ntemp\r\n";

        InputHandler getHandler = new InputHandler(new HashMap<>(),new HashMap<>(), 6379);
        String getOutput = TestsHelper.runInputHandler(respInput,getHandler);

        assertEquals("$-1\r\n", output);
        //assertNull(InputHandler.db.get("temp"));
    }

}
