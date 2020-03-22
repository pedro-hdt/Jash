package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class LsIntegrationCommandTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private OutputStream stdout;

    private ShellImpl shell = new ShellImpl();


    @BeforeAll
    static void setupAll() {

        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "LsIntegrationTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        shell = new ShellImpl();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "LsIntegrationTestFolder");

        stdout.flush();
    }

    @Test
    @DisplayName("invalid case")
    public void testLsInvalidDir() {
        try {
            shell.parseAndEvaluate("ls \"*\"", stdout);
            assertEquals("ls: cannot access '*': No such file or directory" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Ls with BF (1)")
    public void testLsWithRm() {
        try {
            Path pathf1 = Files.createFile(Paths.get(Environment.currentDirectory, "temp.txt"));
            shell.parseAndEvaluate("rm `ls 'temp.txt'`", stdout);

            assertFalse(Files.exists(pathf1));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Ls with BF (2)")
    public void testLsWithEcho() {
        try {
            Path pathf1 = Files.createDirectory(Paths.get(Environment.currentDirectory, "dire"));
            Path pathf2 = Files.createFile(Paths.get(Environment.currentDirectory, "dire", "temp3.txt"));

            shell.parseAndEvaluate("ls -Rd | echo 'temp3.txt'", stdout);
            assertEquals("temp3.txt" + StringUtils.STRING_NEWLINE, stdout.toString());

            Files.deleteIfExists(pathf2);
            Files.deleteIfExists(pathf1);
        } catch (Exception e) {
            fail();
        }
    }


    @Test
    @DisplayName("Ls with BF (3)")
    public void testLsWithEcho2() {
        try {
            Path pathf1 = Files.createDirectory(Paths.get(Environment.currentDirectory, "dir"));
            Path pathf2 = Files.createFile(Paths.get(Environment.currentDirectory, "dir", "temp2.txt"));

            shell.parseAndEvaluate("echo 'temp2.txt' | ls -Rd ", stdout);
            assertEquals("./:" + StringUtils.STRING_NEWLINE +
                     "dir" + StringUtils.STRING_NEWLINE +
                    "" + StringUtils.STRING_NEWLINE +
                    "dir:" + StringUtils.STRING_NEWLINE, stdout.toString());

            Files.deleteIfExists(pathf2);
            Files.deleteIfExists(pathf1);
        } catch (Exception e) {
            fail();
        }
    }


    @Test
    @DisplayName("Ls with EF1 (1)")
    public void testLsWithWc() {
        try {
            shell.parseAndEvaluate("ls wcFile* | wc -c", stdout);
            assertEquals("      11" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Ls with EF1 (2)")
    public void testLsWithDiff() {
        try {
            shell.parseAndEvaluate("diff `ls diff*.txt`", stdout);
            assertEquals("< azz" + StringUtils.STRING_NEWLINE +
                    "< ccc" + StringUtils.STRING_NEWLINE +
                    "> zza" + StringUtils.STRING_NEWLINE +
                    "> ccd" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }


}
