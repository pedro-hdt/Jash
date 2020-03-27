package sg.edu.nus.comp.cs4218.impl.util;

public final class TestUtils {
    private TestUtils() {
    }

    public static String getTestResultPath(String testName, String testDirectory) {
        return String.format("%s/result/%s.txt", testDirectory, testName);
    }

    public static String formatExceptionErrorMessage(String errorReason, Exception exception) {
        return String.format("%s: %s", errorReason, exception.getMessage());
    }
}
