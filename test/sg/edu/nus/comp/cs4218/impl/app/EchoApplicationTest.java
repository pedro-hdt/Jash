package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;
import sg.edu.nus.comp.cs4218.exception.EchoException;

import java.io.ByteArrayOutputStream;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class EchoApplicationTest {

    private static EchoApplication echo = new EchoApplication();
    private static ByteArrayOutputStream out = new ByteArrayOutputStream();

    /**
     * Call echo without any arguments
     */
    @Test
    public void emptyArgs() throws EchoException {

        out.reset();
        echo.run(new String[0], System.in, out);

        // empty args should print a new line
        Assertions.assertEquals(STRING_NEWLINE, out.toString());

    }


    /**
     * Call echo with a single argument
     */
    @Test
    public void singleArg() throws EchoException {

        out.reset();
        echo.run(new String[]{"hello"}, System.in, out);

        // empty args should print a new line
        Assertions.assertEquals("hello" + STRING_NEWLINE, out.toString());

    }

    /**
     * Call echo with 2 arguments
     */
    @Test
    public void twoArgs() throws EchoException {

        out.reset();
        echo.run(new String[]{"hello", "world"}, System.in, out);

        // empty args should print a new line
        Assertions.assertEquals("hello world" + STRING_NEWLINE, out.toString());

    }

}
