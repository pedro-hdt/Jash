package hackathon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplicationTest {
    private static PasteApplication pasteApp = new PasteApplication();
    private static final String TEST_FOLDER =   "rebuttal" + CHAR_FILE_SEP +
                                                "team6" + CHAR_FILE_SEP +
            "hackathon/files" + CHAR_FILE_SEP +
                                                "paste" + CHAR_FILE_SEP;
    private static final String FILE_NEW_PATH = TEST_FOLDER + "fileNewLine.txt";
    private static final String FILE_EMPTY_1 = TEST_FOLDER + "empty1.txt";
    private static final String FILE_EMPTY_2 = TEST_FOLDER + "empty2.txt";

    /**
     * This bug is due to wrongly handling std_in with all NEW_LINEs
     *
     * `paste /path/to/file` should get exactly the same result with `paste - < /path/to/file
     * But the outputs of upper two command are different.
     * @throws Exception Unhandled IOException
     */
    @Test
    @DisplayName("Should display 3 NEW_LINEs while paste 5 NEW_LINEs")
    public void testMergeStdin_multipleLine() throws Exception {
        // Newline
        try (InputStream inStream = new FileInputStream(FILE_NEW_PATH)) {
            String expected = "";
            assertEquals(expected, pasteApp.mergeStdin(inStream));
        }
    }

    /**
     * This bug is due to invalid handling paste two empty files.
     * Ref. spec page 14, Section 9.3, No assumption mentioned.
     *
     * The assumption says "When pasting only files, the command produces the same output as GNU paste.", but while testing
     * `paste empty1.txt empty2.txt`, the MacOSX terminal gives "" rather than an Exception.
     */
    @Test
    @DisplayName("Should display nothing while paste 2 empty files")
    void testMergeTwoEmptyFiles() { //TODO: Unhandle empty files
        String expectResult = "\t";
        assertDoesNotThrow(() -> { // Should not throw exception, but StringIndexOutOfBoundsException was thrown
            String realResult = pasteApp.mergeFile(FILE_EMPTY_1, FILE_EMPTY_2);
            assertEquals(expectResult, realResult);
        });
    }
}
