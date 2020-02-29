package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.SedException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * SedApplicationTest used to test sed command
 *
 * Test Cases:
 *
 * Negative:
 * -    No arguments given
 * -    No output stream
 * -    No replacement text provided
 * -    With Invalid Rule having less than 4 limiters
 * -    Invalid replacement rule NaN specified
 * -    Empty regex given
 * -    Null file input
 * -    File doesn't exist
 *
 *
 * Positive:
 * -    Replace first with stdin input
 * -    Replace with regex tests helper function `replaceSubstringInStdin`
 * -    Replace content in 1 file
 * -    Replace content in multiple files
 * -    Attempt to replace with no matches with regex
 * -    Replace second occurrence of regex
 * -    Complex regex replacement (2)
 * -    Replace with Different separators
 * -    Replace with empty string
 * -    Query empty file
 * -    Replace across multiple lines
 *
 *
 */
public class SedApplicationTest {
    public static final String REPLACING_FILE = "replacingSubstring.txt";
    public static final String FILE1_TXT = "file1.txt";
    public static final String FILE2_TXT = "file2.txt";
    public static final String MULTIPLE_LINES = "multipleLines.txt";
    private static SedApplication sed;
    private static InputStream stdin;
    private static OutputStream stdout;

    private static String ORIGINAL_DIR;


    @BeforeAll
    static void setupAll() {
        ORIGINAL_DIR = Environment.getCurrentDirectory();
        Environment.setCurrentDirectory(ORIGINAL_DIR + File.separator + "dummyTestFolder" + File.separator + "SedTestFolder");
    }

    @AfterAll
    static void reset() throws IOException {

        // Reset file content after replaced
        FileOutputStream outputStream = new FileOutputStream(IOUtils.resolveFilePath(REPLACING_FILE).toFile());
        byte[] strToBytes = "before".getBytes();
        outputStream.write(strToBytes);
        outputStream.close();


        // Reset after updating content of multiple files
        outputStream = new FileOutputStream(IOUtils.resolveFilePath(FILE1_TXT).toFile());
        strToBytes = "1file content".getBytes();
        outputStream.write(strToBytes);
        outputStream.close();

        outputStream = new FileOutputStream(IOUtils.resolveFilePath(FILE2_TXT).toFile());
        strToBytes = "2file content".getBytes();
        outputStream.write(strToBytes);
        outputStream.close();


        // Reset for multipleLines.txt
        outputStream = new FileOutputStream(IOUtils.resolveFilePath(MULTIPLE_LINES).toFile());
        strToBytes = "hello boy\ngirl hello hello".getBytes();
        outputStream.write(strToBytes);
        outputStream.close();



        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    public void setUp() {
        sed = new SedApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    public void tearDown() throws IOException {
        stdout.flush();
    }

    @Test
    public void testFailsWithNullArgsOrStream() {
        Exception expectedException = assertThrows(SedException.class, () -> sed.run(null, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_ARGS));

        expectedException = assertThrows(SedException.class, () -> sed.run(new String[1], null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));

    }

    @Test
    public void testFailsWithLessThanOneArgs() {
        Exception exception = assertThrows(SedException.class, () -> sed.run(new String[0], null, stdout));
        assertTrue(exception.getMessage().contains(ERR_NO_REP_RULE));

    }

    @Test
    public void testInvalidRuleLessLimiters() {

        String[] args = new String[] {"s|he|ye"};
        stdin = new ByteArrayInputStream("random".getBytes());

        Exception exception = assertThrows(SedException.class, () -> sed.run(args, stdin, stdout));
        assertTrue(exception.getMessage().contains(ERR_INVALID_REP_RULE));
    }

    @Test
    public void testInvalidRuleNotANumber() {
        String stdInString = "hello";

        String[] args = new String[] {"s|he|ye|x"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        Exception exception = assertThrows(SedException.class, () -> sed.run(args, stdin, stdout));
        assertTrue(exception.getMessage().contains(ERR_INVALID_REP_X));
    }

    @Test
    public void testInvalidRuleEmptyRegex() {

        String[] args = new String[] {"s||ye|"};
        stdin = new ByteArrayInputStream("invalid".getBytes());

        Exception exception = assertThrows(SedException.class, () -> sed.run(args, stdin, stdout));
        assertTrue(exception.getMessage().contains(ERR_EMPTY_REGEX));
    }

    @Test
    public void testInvalidNullFile() {

        Exception exception = assertThrows(Exception.class, ()
                -> sed.replaceSubstringInFile("reg", "no", 1, null));
        assertTrue(exception.getMessage().contains(ERR_NULL_ARGS));
    }

    @Test
    public void testInvalidFileDoesntExist() {

        Exception exception = assertThrows(Exception.class, ()
                -> sed.replaceSubstringInFile("reg", "no", 1, "no-such-file.txt"));
        assertTrue(exception.getMessage().contains(ERR_FILE_NOT_FOUND));
    }

    @Test
    public void testReplaceSubStringFromStdin() throws SedException {
        String stdInString = "hello toBeReplaced";

        String[] args = new String[] {"s/toBeReplaced/replacedWord/"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        sed.run(args, stdin, stdout);
        assertTrue(stdout.toString().contains("replacedWord"));
    }

    @Test
    public void testReplaceSubstringInStdinWithRandomRegex() {
        String stdInString = "man 1";
        String regex = "^ma";
        String replacement = "woma";

        stdin = new ByteArrayInputStream(stdInString.getBytes());

        try {
            String result = sed.replaceSubstringInStdin(regex, replacement,1, stdin);
            assertTrue(result.contains("woman 1"));
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());//NOPMD - Suppressed as its fine to have similar fail with different exception msg
        }
    }

    @Test
    public void testSuccessfulReplaceInFileContent() throws IOException {
        String[] args = new String[] {"s/before/after/", REPLACING_FILE};

        try {
            String str = new String(Files.readAllBytes(IOUtils.resolveFilePath(REPLACING_FILE)));
            assertTrue(str.contains("before"));

            sed.run(args, stdin, stdout);
            str = new String(Files.readAllBytes(IOUtils.resolveFilePath(REPLACING_FILE)));
            assertTrue(str.contains("after"));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceSubstringNoMatches() {
        String stdInString = "hello";

        String[] args = new String[] {"s/notFound/random/"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        try {
            sed.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains(stdInString));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceSubstringSecondMatch() {
        String stdInString = "hello hello";

        String[] args = new String[] {"s/hello/boy/2"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        try {
            sed.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains("hello boy"));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceSubstringComplexRegex1() {
        String stdInString = "bullet";

        String[] args = new String[] {"s/^/> /"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        try {
            sed.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains("> bullet"));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceSubstringComplexRegex2(){
        String stdInString = "$16.32";

        String[] args = new String[] {"s|\\p{Sc}*(\\s?\\d+[.,]?\\d*)\\p{Sc}*|4|"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        try {
            sed.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains("4"));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceSubstringWithDiffSeparators() {
        String stdInString = "cat";

        String[] args = new String[] {"s|c|m|"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        try {
            sed.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains("mat"));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceSubstringClear() {
        String stdInString = "clear";

        String[] args = new String[] {"s/clear//"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        try {
            sed.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains(""));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceNoContentInInput() {
        String[] args = new String[] {"s/hello//", "emptyFile.txt"};

        try {
            sed.run(args, null, stdout);
            assertTrue(stdout.toString().contains(""));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceAcrossMultipleLines() {
        String[] args = new String[] {"s/hello/hell/", MULTIPLE_LINES};

        try {
            sed.run(args, null, stdout);
            assertTrue(stdout.toString().contains("hell boy\ngirl hell hello"));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testReplaceSubstringMultipleFiles() throws IOException {
        String[] args = new String[] {"s/file/updatedFile/", FILE1_TXT, FILE2_TXT};

        try {
            String str = new String(Files.readAllBytes(IOUtils.resolveFilePath(FILE1_TXT)));
            assertTrue(str.contains("1file content"));

            str = new String(Files.readAllBytes(IOUtils.resolveFilePath(FILE2_TXT)));
            assertTrue(str.contains("2file content"));

            sed.run(args, stdin, stdout);
            str = new String(Files.readAllBytes(IOUtils.resolveFilePath(FILE1_TXT)));
            assertTrue(str.contains("1updatedFile content"));

            str = new String(Files.readAllBytes(IOUtils.resolveFilePath(FILE2_TXT)));
            assertTrue(str.contains("2updatedFile content"));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }




}
