package tdd.ef2;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.app.CutApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD")
class CutApplicationTest {
    
    private static String folderName = "test/tdd/util/dummyTestFolder/CutTestFolder";
    private static String fileNameTest = "test.txt";
    private static String fileNameNames = "course.txt";
    private static String subDirName = "subDir";
    private static String fileNameNotExist = "notExist.txt";
    private static String fileNameEmpty1 = "empty1.txt";
    private static String cutPrefix = "cut: ";
    private final CutInterface app = new CutApplication();
    private OutputStream outputStream = null;

    @Test
    void testCutTwoCharactersFromFile() {
        String expectResult = "Ts";
        assertDoesNotThrow(() -> {
            String realResult = app.cutFromFiles(true, false, false, 1, 8, folderName + CHAR_FILE_SEP + fileNameTest);
            assertEquals(expectResult, realResult);
        });
    }

    @Test
    void testCutTwoCharactersInReverseOrderFromFile() {
        String expectResult = "Ts";
        assertDoesNotThrow(() -> {
            String realResult = app.cutFromFiles(true, false, false, 8, 1, folderName + CHAR_FILE_SEP + fileNameTest);
            assertEquals(expectResult, realResult);
        });
    }

    @Test
    void testCutRangeOfCharactersFromFile() {
        String expectResult = "Today is";
        assertDoesNotThrow(() -> {
            String realResult = app.cutFromFiles(true, false, true, 1, 8, folderName + CHAR_FILE_SEP + fileNameTest);
            assertEquals(expectResult, realResult);
        });
    }

    @Test
    void testCutSingleCharactersFromFile() {
        String expectResult = "s";
        assertDoesNotThrow(() -> {
            String realResult = app.cutFromFiles(true, false, false, 8, 0, folderName + CHAR_FILE_SEP + fileNameTest);
            assertEquals(expectResult, realResult);
        });
    }

    @Test
    void testCutSingleBytesFromStdin() {
        String original = "bad";
        InputStream stdin = new ByteArrayInputStream(original.getBytes());
        String expectResult = "a";
        assertDoesNotThrow(() -> {
            String realResult = app.cutFromStdin(false, true, false, 2, 0, stdin);
            assertEquals(expectResult, realResult);
        });
    }

    @Test
    void testCutTwoBytesFromStdin() {
        String original = "baz";
        InputStream stdin = new ByteArrayInputStream(original.getBytes());
        String expectResult = "az";
        assertDoesNotThrow(() -> {
            String realResult = app.cutFromStdin(false, true, false, 2, 3, stdin);
            assertEquals(expectResult, realResult);
        });
    }

    @Test
    void testCutRangeOfBytesFromStdin() {
        String original = "bazzzz";
        InputStream stdin = new ByteArrayInputStream(original.getBytes());
        String expectResult = "azzzz";
        assertDoesNotThrow(() -> {
            String realResult = app.cutFromStdin(false, true, true, 2, 6, stdin);
            assertEquals(expectResult, realResult);
        });
    }

    @Test
    void testRunWithOneFile() {
        String expectResult = "Ts" + STRING_NEWLINE;
        String[] args = {"-c", "1,8", folderName + CHAR_FILE_SEP + fileNameTest};
        outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            app.run(args, System.in, outputStream);
            assertEquals(expectResult, outputStream.toString());
        });
    }

    @Test
    void testRunWithTwoFile() {
        String expectResult = "Today is" + STRING_NEWLINE + "Cristina" + STRING_NEWLINE;
        String[] args = {"-c", "1-8", folderName + CHAR_FILE_SEP + fileNameTest, folderName + CHAR_FILE_SEP + fileNameNames};
        outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            app.run(args, System.in, outputStream);
            assertEquals(expectResult, outputStream.toString());
        });
    }

    @Test
    void testRunWithFilesAndStdin() {
        String original = "bazzz";
        InputStream stdin = new ByteArrayInputStream(original.getBytes());
        String expectResult = "od" + STRING_NEWLINE + "az" + STRING_NEWLINE;
        String[] args = {"-c", "2-3", folderName + CHAR_FILE_SEP + fileNameTest, "-"};
        outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            app.run(args, stdin, outputStream);
            assertEquals(expectResult, outputStream.toString());
        });
    }

    @Test
    void testRunWithoutSpecifiedFile() {
        String original = "baz";
        InputStream stdin = new ByteArrayInputStream(original.getBytes());
        String expectResult = "az" + STRING_NEWLINE;
        String[] args = {"-c", "2-3"};
        outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            app.run(args, stdin, outputStream);
            assertEquals(expectResult, outputStream.toString());
        });
    }

    @Test
    void testRunWithSingleIndex() {
        String expectResult = "o" + STRING_NEWLINE;
        String[] args = {"-c", "2", folderName + CHAR_FILE_SEP + fileNameTest};
        outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            app.run(args, System.in, outputStream);
            assertEquals(expectResult, outputStream.toString());
        });
    }

    @Test
    @Disabled("There is always the chance that an IOException is encountered, so one cannot avoid the exception handling code." +
      " Adding this test is likely to complicate the code.")
    void testRunWithClosedOutputStream() {
        String[] args = {"-b", "1-8", folderName + CHAR_FILE_SEP + fileNameTest};
        Throwable thrown = assertThrows(CutException.class, () -> {
            outputStream = new FileOutputStream(new File(folderName + CHAR_FILE_SEP + fileNameEmpty1));
            IOUtils.closeOutputStream(outputStream);
            app.run(args, System.in, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_WRITE_STREAM);
    }

    @Test
    @Disabled("GNU's implementation of cut doesn't care about the excess range provided and cuts the text in full.")
    void testRunCharacterIndexOutOfRange() {
        String original = "baz";
        InputStream stdin = new ByteArrayInputStream(original.getBytes());
        String[] args = {"-c", "1-8", "-"};
        outputStream = new ByteArrayOutputStream();
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.run(args, stdin, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix); // + ERR_OUT_RANGE);
    }

    @Test
    @Disabled("GNU's implementation of cut doesn't care about the excess range provided and cuts the text in full.")
    void testRunByteIndexOutOfRange() {
        String[] args = {"-b", "1-20", folderName + CHAR_FILE_SEP + fileNameTest};
        outputStream = new ByteArrayOutputStream();
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.run(args, System.in, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix); // + ERR_OUT_RANGE);
    }

    @Test
    void testCutWithNullIStream() {
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.cutFromStdin(false, false, false, 1, 8, null);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_NULL_STREAMS);
    }

    @Test
    void testCutWithNotExistFileName() {
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.cutFromFiles(false, false, false, 1, 8, folderName + CHAR_FILE_SEP + fileNameNotExist);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_FILE_NOT_FOUND);
    }

    @Test
    void testCutWithNullFileName() {
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.cutFromFiles(false, false, false, 1, 8, null);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_NULL_ARGS);
    }

    @Test
    void testCutWithDirectoryName() {
        Throwable thrown = assertThrows(Exception.class, () -> {
            app.cutFromFiles(false, false, false, 1, 8, folderName + CHAR_FILE_SEP + subDirName);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_IS_DIR);
    }

    @Test
    void testRunWithNullArg() {
        outputStream = new ByteArrayOutputStream();
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.run(null, System.in, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_NULL_ARGS);
    }

    @Test
    void testRunWithNullOStream() {
        String[] args = {folderName + CHAR_FILE_SEP + fileNameTest};
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.run(args, System.in, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_NULL_STREAMS);
    }

    @Test
    void testRunWithLessThanTwoArgs() {
        String[] args = {"-b"};
        outputStream = new ByteArrayOutputStream();
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.run(args, System.in, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_NO_ARGS);
    }

    @Test
    void testRunWithInvalidFlag() {
        String[] args = {"-p", "8-2"};
        outputStream = new ByteArrayOutputStream();
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.run(args, System.in, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_INVALID_FLAG);
    }

    @Test
    void testRunWithInvalidRange() {
        String[] args = {"-c", "8-2"};
        outputStream = new ByteArrayOutputStream();
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.run(args, System.in, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_INVALID_RANGE);
    }

    @Test
    void testRunWithInvalidNumber() {
        String[] args = {"-c", "0", folderName + CHAR_FILE_SEP + fileNameTest};
        outputStream = new ByteArrayOutputStream();
        Throwable thrown = assertThrows(CutException.class, () -> {
            app.run(args, System.in, outputStream);
        });
        assertEquals(thrown.getMessage(), cutPrefix + ERR_OUT_OF_RANGE);
    }
}