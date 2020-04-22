package hackathon;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.impl.app.CutApplication;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CutApplicationTest {
    private static String folderName = "hackathon" + CHAR_FILE_SEP +
                                        "team6" + CHAR_FILE_SEP +
            "hackathon/files" + CHAR_FILE_SEP +
                                        "cut";
    private static String TEST_FILE = "test.txt";
    private static String fileNameNames = "course.txt";

    private final CutInterface app = new CutApplication();
    private OutputStream outputStream = null;




    /**
     * The bug is due to invalid handling of "Cuts out selected portions of each line (as specified by list) from each
     * file and writes them to the standard output"
     * Ref. spec page 18, Section 9.11
     *
     * The implementation fails to cut file with multiple lines
     */
    @Test
    void testRunWithTwoFile() {
        String expectResult = "Today is" + STRING_NEWLINE + "Cristina" + STRING_NEWLINE + "Software" + STRING_NEWLINE;
        String[] args = {"-c", "1-8", folderName + CHAR_FILE_SEP + TEST_FILE, folderName + CHAR_FILE_SEP + fileNameNames};
        outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            app.run(args, System.in, outputStream);
            assertEquals(expectResult, outputStream.toString());
        });
    }
}
