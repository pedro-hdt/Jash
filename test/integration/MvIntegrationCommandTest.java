package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
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

public class MvIntegrationCommandTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private OutputStream stdout;

    private ShellImpl shell = new ShellImpl();


    @BeforeAll
    static void setupAll() {

        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "MvIntegrationTestFolder");
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
                + StringUtils.fileSeparator() + "MvIntegrationTestFolder");

        stdout.flush();
    }


    @Test
    @DisplayName("EF2 with BF")
    public void testMvWithExit() {
        try {
            shell.parseAndEvaluate("exit | mv 'name.mv' mvDir", stdout);
            assertEquals("", stdout.toString());
            fail();

        } catch (Exception e) {
            assertTrue(e instanceof ExitException); //NOPMD
            assertMsgContains(e, "terminating execution");
        }
    }

    @Test
    @DisplayName("EF2 with BF")
    public void testMvWithSed() {
        try {
            Path dirPath = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), "mvDir"));
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "name.mv"));

            shell.parseAndEvaluate("mv 'name.mv' mvDir ; sed \"s|abc|def|\" mvDir/*.mv", stdout);
            assertEquals("", stdout.toString());

            assertFalse(Files.exists(filePath1));
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, "mvDir", "name.mv")));

            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "mvDir", "name.mv"));
            Files.deleteIfExists(dirPath);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("EF2 with EF1")
    public void testMvWithCdAndLs() {
        try {
            Path dirPath = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), "cdDir"));
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "name.cd")); //NOPMD

            shell.parseAndEvaluate("mv 'name.cd' cdDir ; cd cdDir ; ls", stdout);
            assertTrue(stdout.toString().contains("name.cd"));

            assertFalse(Files.exists(filePath1));
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, "name.cd")));

            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "name.cd"));
            Files.deleteIfExists(dirPath);
        } catch (Exception e) {
            fail();
        }
    }


    @Test
    @DisplayName("Mv with EF1")
    public void testMvWithWc() {
        try {
            Path dirPath = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), "wcDir"));
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "name.wc"));

            shell.parseAndEvaluate("mv 'name.wc' wcDir ; wc wcDir/name.wc", stdout);
            assertEquals("       0       0       0 wcDir/name.wc" + StringUtils.STRING_NEWLINE, stdout.toString());

            assertFalse(Files.exists(filePath1));
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory,"wcDir", "name.wc")));

            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "wcDir", "name.wc"));
            Files.deleteIfExists(dirPath);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Mv with BF")
    public void testMvWithPaste() {
        try {
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "name.paste"));
            FileOutputStream outputStream = new FileOutputStream(filePath1.toFile()); //NOPMD
            byte[] strToBytes = "first".getBytes();
            outputStream.write(strToBytes);
            outputStream.close();

            shell.parseAndEvaluate("mv 'name.paste' \"new.paste\" ; paste new.paste new.paste", stdout);
            assertEquals("first\tfirst" + StringUtils.STRING_NEWLINE, stdout.toString());

            assertFalse(Files.exists(filePath1));
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, "new.paste")));

            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "new.paste"));
        } catch (Exception e) {
            fail();
        }
    }

}
