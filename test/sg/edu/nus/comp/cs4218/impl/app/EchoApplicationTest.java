package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.TestUtils;
import sg.edu.nus.comp.cs4218.exception.EchoException;

/**
 * Provides unit tests for the EchoApplication class
 * Output is received through a ByteArrayOutputStream
 * <p>
 * Positive test cases:
 * - no arguments
 * - single argument
 * - two arguments
 * - multiple args
 *
 * <p>
 * Negative test cases:
 * - null stream or args
 * - stdout throws exception
 *
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

        assertMsgContains(exception, ERR_NULL_ARGS);

    }

    /**
     * Call echo with null stream
     */
    @Test
    public void testNullOutputStream() {

        Exception exception = assertThrows(EchoException.class, () -> echo.run(new String[]{"test"}, System.in, null));

        assertMsgContains(exception, ERR_NO_OSTREAM);

    }

    /**
     *     Test echo app whether output exception is thrown when there is an IOException
     */
    @Test
    void testWritingResultToOutputStreamException() {
        try {
            OutputStream baos = TestUtils.getMockExceptionThrowingOutputStream(); //NOPMD

            echo.run(new String[]{"heya"}, System.in, baos);
            fail("Exception expected");
        } catch (EchoException e) {
            assertEquals("echo: " + ERR_IO_EXCEPTION, e.getMessage());
        }
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

        echo.run(new String[]{"single"}, System.in, out);

        assertEquals("single" + STRING_NEWLINE, out.toString());

    }

    /**
     * Call echo with a single argument as keyword
     */
    @Test
    public void singleArgAsKeyword() throws EchoException {

        echo.run(new String[]{"echo"}, System.in, out);

        assertEquals("echo" + STRING_NEWLINE, out.toString());

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

    /**
     * Call echo with multiple arguments
     */
    @Test
    public void multipleArgs() throws EchoException {

        echo.run(new String[]{"hi", "boy", "how", "are", "you man"}, System.in, out);

        assertEquals("hi boy how are you man" + STRING_NEWLINE, out.toString());

    }

}
