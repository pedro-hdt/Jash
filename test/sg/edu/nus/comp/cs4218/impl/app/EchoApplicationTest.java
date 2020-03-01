package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.EchoException;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * Provides unit tests for the EchoApplication class
 * Output is received through a ByteArrayOutputStream
 * <p>
 * Positive test cases:
 * - no arguments
 * - single argument
 * - two arguments
 */
public class EchoApplicationTest {

    private static EchoApplication echo;
    private static ByteArrayOutputStream out;

    @BeforeAll
    public static void setOut() {
        out = new ByteArrayOutputStream();
    }

    @BeforeEach
    public void setEcho() {
        echo = new EchoApplication();
    }

    @AfterEach
    public void resetOut() {
        out.reset();
    }


    /**
     * Call echo with null arg for constructResult()
     */
    @Test
    public void testNullArgs() {
        Exception exception = assertThrows(EchoException.class, () -> echo.constructResult(null));

        TestUtils.assertMsgContains(exception, ERR_NULL_ARGS);

    }

    /**
     * Call echo with null stream
     */
    @Test
    public void testNullOutputStream() {

        Exception exception = assertThrows(EchoException.class, () -> echo.run(new String[]{"test"}, System.in, null));

        TestUtils.assertMsgContains(exception, ERR_NO_OSTREAM);

    }

    /**
     * Call echo without any arguments
     */
    @Test
    public void emptyArgs() throws EchoException {

        echo.run(new String[0], System.in, out);

        // empty args should print a new line
        assertEquals(STRING_NEWLINE, out.toString());

    }


    /**
     * Call echo with a single argument
     */
    @Test
    public void singleArg() throws EchoException {

        echo.run(new String[]{"hello"}, System.in, out);

        assertEquals("hello" + STRING_NEWLINE, out.toString());

    }

    /**
     * Call echo with a single argument as space
     */
    @Test
    public void singleArgAsSpace() throws EchoException {

        echo.run(new String[]{" "}, System.in, out);

        assertEquals(" " + STRING_NEWLINE, out.toString());

    }

    /**
     * Call echo with 2 arguments
     */
    @Test
    public void twoArgs() throws EchoException {

        echo.run(new String[]{"hello", "world"}, System.in, out);

        assertEquals("hello world" + STRING_NEWLINE, out.toString());

    }

}
