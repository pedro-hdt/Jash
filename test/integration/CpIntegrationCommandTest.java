package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;

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
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class CpIntegrationCommandTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private OutputStream stdout;

    private ShellImpl shell = new ShellImpl();


    @BeforeAll
    static void setupAll() {

        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "CpIntegrationTestFolder");
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
                + StringUtils.fileSeparator() + "CpIntegrationTestFolder");

        stdout.flush();
    }

    @Test
    @DisplayName("EF1 with BF invalid")
    public void testMvWithExit() {
        try {
            shell.parseAndEvaluate("exit | cp 'name.cp' cpDir", stdout);
            assertEquals("", stdout.toString());
            fail();

        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, "terminating execution");
        }
    }

    @Test
    @DisplayName("EF1 with BF")
    public void testCpAndRm() {
        try {
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "name.rm"));

            shell.parseAndEvaluate("cp 'name.rm' newname.rm ; rm *.rm", stdout);

            assertFalse(Files.exists(filePath1));
            assertFalse(Files.exists(Paths.get(Environment.currentDirectory, "newname.rm")));

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("EF1 with BF")
    public void testMvWithSed() {
        try {
            Path dirPath = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), "cpDir"));
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "name.cp"));

            shell.parseAndEvaluate("cp 'name.cp' cpDir ; sed \"s|abc|def|\" cpDir/*.cp", stdout);
            assertEquals("", stdout.toString());

            assertTrue(Files.exists(filePath1));
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, "cpDir", "name.cp")));

            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "cpDir", "name.cp"));
            Files.deleteIfExists(filePath1);
            Files.deleteIfExists(dirPath);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("EF1 with EF2")
    public void testCpAndLs() {
        try {
            Path dirPath = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), "lsDir"));
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "name.ls"));

            shell.parseAndEvaluate("cp 'name.ls' lsDir ; ls -R lsDir", stdout);
            assertEquals("lsDir:" + StringUtils.STRING_NEWLINE +
                    "name.ls" + StringUtils.STRING_NEWLINE, stdout.toString());

            assertTrue(Files.exists(filePath1));
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, "lsDir", "name.ls")));

            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "lsDir", "name.ls"));
            Files.deleteIfExists(filePath1);
            Files.deleteIfExists(dirPath);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("EF1 with EF2")
    public void testCpAndFind() {
        try {
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "name.find"));

            shell.parseAndEvaluate("cp 'name.find' newname.find  ; find ./ -name newname.find", stdout);
            assertEquals("./newname.find" + StringUtils.STRING_NEWLINE, stdout.toString());

            assertTrue(Files.exists(filePath1));
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, "newname.find")));

            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "newname.find"));
            Files.deleteIfExists(filePath1);
        } catch (Exception e) {
            fail();
        }
    }


}
