package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for IOUtils, focusing only on the openOutputStream method as it was the only one
 * where bugs and build errors had been found.
 */
class IOUtilsTest {

    /**
     * Requests an output stream for an existent file and checks if content is overwritten when it is used
     *
     * @throws IOException
     * @throws ShellException
     */
    @Test
    void testOpenOutputStreamExistentFile() throws IOException, ShellException {

        // create temp file write a "B" (ASCII 66) to it and schedule to delete it
        Path outFile = Files.createTempFile("outfile", "");
        Files.write(outFile, new byte[]{66});
        outFile.toFile().deleteOnExit();

        // obtain stream
        OutputStream outputStream = IOUtils.openOutputStream(outFile.toString());

        // validate stream overwrites file contents
        outputStream.write(65);
        outputStream.close();
        byte[] bytesRead = Files.readAllBytes(outFile);
        assertTrue(1 == bytesRead.length);
        assertEquals(65, bytesRead[0]);

    }

    /**
     * Requests an output stream for a new file and checks if content is written correctly
     *
     * @throws IOException
     * @throws ShellException
     */
    @Test
    void testOpenOutputStreamNewFile() throws IOException, ShellException {

        // path to new file
        Path outFile = Paths.get("outfile.txt");

        // obtain stream
        OutputStream outputStream = IOUtils.openOutputStream(outFile.toString());

        // validate stream writes to the file as expected
        outputStream.write(65);
        outputStream.close();
        byte[] bytesRead = Files.readAllBytes(outFile);
        assertTrue(1 == bytesRead.length);
        assertEquals(65, bytesRead[0]);

    }

    /**
     * Requests an output stream for a new file in a directory 2 levels down that does not exist
     * and checks if content is written correctly
     *
     * @throws IOException
     * @throws ShellException
     */
    @Test
    void testOpenOutputStreamNewFileInNonExistentDir() throws IOException, ShellException {

        // path to new file
        Path outFile = Paths.get(Environment.getCurrentDirectory()
                + StringUtils.fileSeparator() + "dir1"
                + StringUtils.fileSeparator() + "dir2"
                + StringUtils.fileSeparator() + "outfile.txt");
        outFile.getParent().getParent().toFile().deleteOnExit();
        outFile.getParent().toFile().deleteOnExit();
        outFile.toFile().deleteOnExit();

        // obtain stream
        OutputStream outputStream = IOUtils.openOutputStream(outFile.toString());

        // validate stream writes to the file as expected
        outputStream.write(65);
        outputStream.close();
        byte[] bytesRead = Files.readAllBytes(outFile);
        assertTrue(1 == bytesRead.length);
        assertEquals(65, bytesRead[0]);

    }
}