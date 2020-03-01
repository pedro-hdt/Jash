package sg.edu.nus.comp.cs4218.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestUtils {

    private TestUtils() {
    }

    /**
     * Asserts that the given exception's message contains the specified text
     *
     * @param exception    exception to be looked into
     * @param text text we are looking for
     */
    public static void assertMsgContains(Exception exception, String text) {
        assertTrue(exception.getMessage().contains(text));
    }

}
