package sg.edu.nus.comp.cs4218;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;

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

    public static OutputStream getMockExceptionThrowingOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int bytes) throws IOException {
                throw new IOException();
            }

            @Override
            public void close() throws IOException {
                throw new IOException();
            }
        };
    }
}
