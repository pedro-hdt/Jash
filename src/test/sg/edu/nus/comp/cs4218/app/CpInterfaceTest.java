package sg.edu.nus.comp.cs4218.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CdException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CpInterfaceTest {

    public static CpInterface cp;

    /**
     * Creates a file and schedules it to be deleted on exit
     *
     * @return Path to file created
     * @throws IOException
     */
    public static Path mkfile() throws IOException {
        Path file = Files.createTempFile("cpTest", "");
        file.toFile().deleteOnExit();
        return file;
    }

    /**
     * Creates a directory and schedules it to be deleted on exit
     *
     * @return Path to directory created
     * @throws IOException
     */
    public static Path mkdir() throws IOException {
        Path dir = Files.createTempDirectory("cpTest");
        dir.toFile().deleteOnExit();
        return dir;
    }

    /**
     * Hashes two files using SHA-256 and return whether the hashes match
     *
     * @return Path to directory created
     * @throws IOException
     */
    public static boolean hashesMatch(Path file1, Path file2) throws NoSuchAlgorithmException, IOException {

        // TODO if all test with empy files this is not needed as they will all hash to the same

        // Create message digest, input streams and buffer
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(Files.readAllBytes(file1));
        byte[] hash1 = md.digest();

        md.reset();

        md.update(Files.readAllBytes(file2));
        byte[] hash2 = md.digest();

        return Arrays.equals(hash1, hash2);
    }

    @BeforeEach
    public void setCp() {
        // TODO we need to instantiate cp to test, but there is no implementation of the interface...
        // cp = new CpApplication();
    }

    /**
     * Attempts to copy a file to the same directory it is in
     * Assumption: should fail and throw an exception with the text "same file"
     */
    @Test
    public void cpSingleFileToSameDir() throws IOException {

        Path file = mkfile();

        String[] args = {file.toString(), "."};
        CdException e = assertThrows(CdException.class, () -> cp.run(args, System.in, System.out));

        assertTrue(e.getMessage().contains("same file"));

    }

    /**
     * Copy file to another directory
     */
    @Test
    public void cpSingleFileToOtherDir() throws IOException, AbstractApplicationException {

        Path file = mkfile();
        Path dir = mkdir();

        String[] args = {file.toString(), dir.toString()};
        cp.run(args, System.in, System.out);

        Path copy = Paths.get(dir.toString() + file.getFileName());
        assertTrue(Files.exists(copy));

    }

    /**
     * Copy file to another new file
     */
    @Test
    public void cpSingleFileToNewFile() throws IOException, AbstractApplicationException, NoSuchAlgorithmException {

        Path src = mkfile();
        Path dest = mkfile();
        Files.delete(dest); // TODO is there a better way to do this?

        String[] args = {src.toString(), dest.toString()};
        cp.run(args, System.in, System.out);

        assertTrue(Files.exists(dest));
        assertTrue(hashesMatch(src, dest));

    }

    /**
     * Copy file to another existent file
     */
    @Test
    public void cpSingleFileToExistentFile() throws IOException, AbstractApplicationException, NoSuchAlgorithmException {

        Path src = mkfile();
        Path dest = mkfile();

        String[] args = {src.toString(), dest.toString()};
        cp.run(args, System.in, System.out);

        assertTrue(Files.exists(dest));
        assertTrue(hashesMatch(src, dest));

    }

}