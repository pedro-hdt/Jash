package hackathon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.DiffException;
import sg.edu.nus.comp.cs4218.impl.app.DiffApplication;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class DiffApplicationTest {
    private static final String TEST_FOLDER =   "test" + CHAR_FILE_SEP +
                                                "hackathon" + CHAR_FILE_SEP +
            "hackathon/files" + CHAR_FILE_SEP +
                                                "diff" + CHAR_FILE_SEP;
    private static final String TEST_FILE = TEST_FOLDER + "test.txt";

    private static final String DIFFDIR1 = TEST_FOLDER + "diffDir1";
    private static final String DIFFDIR1_DIFF = TEST_FOLDER + "diffDir1-diff";

    private final DiffApplication diffApplication = new DiffApplication();


    /**
     * The bug is due to invalid handling `diff` a file with InputStream that contains spaces using `-B` flag.
     * Ref. spec page 15, Section 9.6, No assumption mentioned.
     *
     * When diff between two files using `-B` flag like `diff -B FILE1 FILE2`, it works correctly.
     */
    @Test
    @DisplayName("using -B flag should ignore the new line and space characters so nothing should output")
    void testRunWithStdinAndFlag() {
        String[] args = {"-B", TEST_FILE, "-"};
        InputStream inputStream = new ByteArrayInputStream(("test" + STRING_NEWLINE + "     ").getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> diffApplication.run(args, inputStream, outputStream));
        assertEquals("", outputStream.toString());
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The bug is just a miss in output display requirement when diff two directories.
     * Ref. spec page 15, Section 9.6, No assumption mentioned.
     *
     * Unlike missing NEW_LINE or spaces, this miss is considered as a incomplete implementation after our discussion.
     */
    @Test
    @DisplayName("diff dir1/file dir2/file should display for every file when diff two directories")
    public void testDiffDirContainFilesWithDifferentContent() {
        try {
            String result = diffApplication.diffTwoDir(DIFFDIR1, DIFFDIR1_DIFF, false, false, false);
            assertEquals(
                    "diff diffDir1" + StringUtils.CHAR_FILE_SEP + "diff1-identical.txt diffDir1-diff" +
                            StringUtils.CHAR_FILE_SEP + "diff1-identical.txt" + StringUtils.STRING_NEWLINE +
                            "> test D" + StringUtils.STRING_NEWLINE +
                            "> test E" + StringUtils.STRING_NEWLINE +
                            "diff diffDir1" + StringUtils.CHAR_FILE_SEP + "diff1.txt diffDir1-diff" + StringUtils.CHAR_FILE_SEP +
                            "diff1.txt" + StringUtils.STRING_NEWLINE +
                            "< test B" + StringUtils.STRING_NEWLINE +
                            "> test D" + StringUtils.STRING_NEWLINE +
                            "> test E" + StringUtils.STRING_NEWLINE +
                            "> test F", result);
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

}
