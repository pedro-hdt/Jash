
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;

class LsApplicationTest {

    private static LsApplication ls;
    private static ByteArrayOutputStream out;


    @BeforeEach
    void setUp() {
        ls = new LsApplication();
        out = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() {
        out.reset();
    }

    /**
     * LS command with no argument showing non-null result
     * @throws LsException
     */
    @Test
    void testLsWithNoArgs() throws LsException {
        ls.run(new String[0], System.in, out);
        assertNotNull(out.toString());
    }

    /**
     * LS command with -d argument showing a directory
     * @throws LsException
     */
    @Test
    void testLsOnlyFolders() throws LsException, IOException {

        Path directoryFolder = Files.createTempDirectory("directoryFolder");

        ls.run(new String[]{directoryFolder.toString(), "-d"}, System.in, out);

        assertTrue(out.toString().contains(directoryFolder.toString()));
    }

    /**
     * LS command with -r argument showing all recursive directory and files
     * @throws LsException
     */
    @Test
    void testLsRecursiveDirectory() throws LsException, IOException {
        Path directoryFolder = Files.createTempDirectory("directoryFolder");

        File tempFile = File.createTempFile("directoryFolder", "tempFile", directoryFolder.toFile());


        ls.run(new String[]{directoryFolder.toString(), "-R"}, System.in, out);

        assertTrue(out.toString().contains(directoryFolder.toString()));
        assertTrue(out.toString().contains(tempFile.getName()));
    }


}