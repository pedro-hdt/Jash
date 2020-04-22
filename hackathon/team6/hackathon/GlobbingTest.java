package hackathon;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class GlobbingTest {
    private static final String RELATIVE_PATH = "hackathon" + CHAR_FILE_SEP +
                                                "team6" + CHAR_FILE_SEP +
            "hackathon/files" + CHAR_FILE_SEP +
                                                "globbing";

    Shell shell = new ShellImpl();
    ByteArrayOutputStream outputStream;

    @BeforeEach
    void setup(){
        outputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void reset(){
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The bug is due to invalid handling of: Project Description 8.6 Globbing
     *
     * For each argument ARG in a shell command that contains unquoted * (asterisk) do the following:
     * 3. If there are such paths, replace ARG with a list of these path separated by spaces.
     */
    @Test
    void testGlobbing(){
        String commandString = "ls " + RELATIVE_PATH + CHAR_FILE_SEP + "test*";
        String expectResult = "hackathon/team6/hackathon/files/globbing/" + "test1.txt" + STRING_NEWLINE + "hackathon/team6/hackathon/files/globbing/" + "test2.txt" + STRING_NEWLINE;
        assertDoesNotThrow(()->{
            shell.parseAndEvaluate(commandString, outputStream);
            assertEquals(expectResult, outputStream.toString());
        });
    }
}
