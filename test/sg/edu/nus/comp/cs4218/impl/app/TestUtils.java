package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {

    /**
     * Asserts that the given exception's message contains the specified text
     *
     * @param e    exception to be looked into
     * @param text text we are looking for
     */
    public static void assertMsgContains(Exception e, String text) {
        assertTrue(e.getMessage().contains(text));
    }

}
