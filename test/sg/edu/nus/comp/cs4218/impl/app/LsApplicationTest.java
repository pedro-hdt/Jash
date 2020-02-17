package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.LsException;

class LsApplicationTest {

    private static LsApplication lsApp;
    private static OutputStream stdout;

    static final String originalDir = Environment.getCurrentDirectory();


    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(originalDir + File.separator + "dummyTestFolder" + File.separator + "LsTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(originalDir);
    }


    @BeforeEach
    void setUp() {
        lsApp = new LsApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    @Test
    public void testFailsWithNullArgsOrStream() {
        Exception expectedException = assertThrows(LsException.class, () -> lsApp.run(null, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_ARGS));

        expectedException = assertThrows(LsException.class, () -> lsApp.run(new String[0], null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NO_OSTREAM));

    }

    @Test
    public void testNonExistentDirectory() throws LsException {
        lsApp.run(new String[]{"no-folder-named-like-this" }, System.in, stdout);

        assertTrue(stdout.toString().contains("ls: cannot access 'no-folder-named-like-this': No such file or directory"));

    }


    /**
     * LS command with no argument showing non-null result
     * @throws LsException
     */
    @Test
    void testLsWithNoArgs() {
        try {
            lsApp.run(new String[0], System.in, stdout);
            assertTrue(stdout.toString().contains("textfile.txt"));
        } catch (LsException e) {
            fail("should not fail:" + e.getMessage());
        }
    }

    /**
     * LS command with -d argument showing a directory
     * @throws LsException
     */
    @Test
    void testLsOnlyFolders() throws LsException, IOException {

        Path directoryFolder = Files.createTempDirectory("directoryFolder");

        lsApp.run(new String[]{directoryFolder.toString(), "-d"}, System.in, stdout);

        assertTrue(stdout.toString().contains(directoryFolder.toString()));
    }

    /**
     * LS command with -R argument showing all recursive directory and files
     * @throws LsException
     */
    @Test
    void testLsRecursiveDirectory() throws LsException, IOException {
        Path directoryFolder = Files.createTempDirectory("directoryFolder");
        File tempFile = File.createTempFile("directoryFolder", "tempFile", directoryFolder.toFile());

        lsApp.run(new String[]{directoryFolder.toString(), "-R"}, System.in, stdout);

        assertTrue(stdout.toString().contains(directoryFolder.toString()));
        assertTrue(stdout.toString().contains(tempFile.getName()));
    }

}