package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * Tests for IO redirection
 * <p>
 * Assumption: redirecting both input and output to/from same file is "undefined behavior",
 * and should never be used. As of now this causes the file to be empty, just like in bash.
 * <p>
 * Positive test cases:
 * - Redirecting output to a file
 * - Redirecting input from file
 * - Redirecting both input and output to/from different files
 * - redirecting output to a file, called from the shell (integration)
 * <p>
 * Negative test cases:
 * - Empty args
 * - Null args
 * - Output redirection without destination (eg "echo hello >")
 * - Output redirection to multiple different files (eg "echo hello > f1 > f2")
 * - Input redirection from inexistent file throws an exception
 */
public class IORedirectionHandlerTest {

    private static final String INFILE = "infile.txt";
    private static final String OUTFILE = "outfile.txt";

    /**
     * Tests the IO redirection of the output of the command "echo hello > outfile.txt"
     * <ul>
     *     <li>Checks that the correct tokens ("echo", "hello") were identified as not redirection tokens</li>
     *     <li>Checks that the input stream is still stdin</li>
     *     <li>Checks that the output stream is into a file</li>
     *     <li>Checks that writing a byte into the output stream writes a byte to the given file</li>
     * </ul>
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     * @throws IOException
     */
    @Test
    public void testRedirOutputToFile() throws AbstractApplicationException, ShellException, IOException {

        // file to be used as output redirection
        Path outFile = IOUtils.resolveFilePath(OUTFILE);
        outFile.toFile().deleteOnExit();

        // instantiate a redirection handler for the command
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("echo", "hello", ">", outFile.toString()),//NOPMD
          System.in,
          System.out,
          new ArgumentResolver()
        );

        // let the handler run
        ioRedirHandler.extractRedirOptions();

        // validate arguments not related to redirection were correctly identified
        List<String> expectNoRedirArgs = Arrays.asList("echo", "hello");
        assertEquals(expectNoRedirArgs, ioRedirHandler.getNoRedirArgsList());

        // validate the input stream is still stdin as it should
        assertEquals(System.in, ioRedirHandler.getInputStream());

        // validate the output stream is into a file
        assertTrue(ioRedirHandler.getOutputStream() instanceof FileOutputStream);

        // get the out stream and use it to write a byte to the file so we can validate it goes
        // where it should go
        FileOutputStream outputStream = (FileOutputStream) ioRedirHandler.getOutputStream(); //NOPMD
        outputStream.write(65);
        outputStream.close();

        // read the file and verify the byte we wrote is in fact there
        byte[] outFileBytes = Files.readAllBytes(outFile);
        assertEquals(1, outFileBytes.length);
        assertEquals(65, outFileBytes[0]);

    }


    /**
     * Tests the IO redirection of the input of the command "paste - < infile.txt"
     * <ul>
     *     <li>Checks that the correct tokens ("paste", "-") were identified as not redirection tokens</li>
     *     <li>Checks that the output stream is still stdout</li>
     *     <li>Checks that the input stream is from a file</li>
     *     <li>Checks that reading the first byte from the input stream, matches the byte we "planted" in the file</li>
     *     <li>Checks that there is no other data in the input stream to be read</li>
     * </ul>
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     * @throws IOException
     */
    @Test
    public void testRedirInputFromFile() throws AbstractApplicationException, ShellException, IOException {

        // file to be used as output redirection
        Path inFile = IOUtils.resolveFilePath(INFILE);
        inFile.toFile().deleteOnExit();
        Files.write(inFile, new byte[]{65}); // write an A (byte 65 in ASCII) into the file for validation

        // instantiate a redirection handler for the command
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("paste", "-", "<", inFile.toString()),//NOPMD
          System.in,
          System.out,
          new ArgumentResolver()
        );

        // let the handler run
        ioRedirHandler.extractRedirOptions();

        // validate arguments not related to redirection were correctly identified
        List<String> expectNoRedirArgs = Arrays.asList("paste", "-");
        assertEquals(expectNoRedirArgs, ioRedirHandler.getNoRedirArgsList());

        // validate the output stream is still stdout as it should
        assertEquals(System.out, ioRedirHandler.getOutputStream());

        // validate the input stream is from a file
        assertTrue(ioRedirHandler.getInputStream() instanceof FileInputStream);

        // get the in stream and use it to read from the file so we can validate it came from the right place
        FileInputStream inputStream = (FileInputStream) ioRedirHandler.getInputStream(); //NOPMD

        // read the input stream and verify the byte we put in the file is there
        int byteRead = inputStream.read();
        assertEquals(65, byteRead);

        // verify there is nothing else there
        assertEquals(-1, inputStream.read());

        inputStream.close();

    }


    /**
     * Test the IO redirection of both input and output for the command "paste - < infile.txt > outfile.txt"
     * Performs each of the checks from the above two test cases.
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     * @throws IOException
     */
    @Test
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    // this test case just requires a lot of command decompsotition but makes no sense to break it, as it is 1 command
    public void testRedirInputAndOuputFromAndToFiles() throws AbstractApplicationException, ShellException, IOException {

        // file to be used as output redirection
        Path inFile = IOUtils.resolveFilePath(INFILE);
        inFile.toFile().deleteOnExit();
        Files.write(inFile, new byte[]{65}); // write an A (byte 65 in ASCII) into the file for validation

        // file to be used as output redirection
        Path outFile = IOUtils.resolveFilePath(OUTFILE);
        outFile.toFile().deleteOnExit();

        // instantiate a redirection handler for the command
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("paste", "-", "<", inFile.toString(), ">", outFile.toString()),
          System.in,
          System.out,
          new ArgumentResolver()
        );

        // let the handler run
        ioRedirHandler.extractRedirOptions();

        // validate arguments not related to redirection were correctly identified
        List<String> expectNoRedirArgs = Arrays.asList("paste", "-");
        assertEquals(expectNoRedirArgs, ioRedirHandler.getNoRedirArgsList());

        // validate the both streams are into or from files
        assertTrue(ioRedirHandler.getInputStream() instanceof FileInputStream);
        assertTrue(ioRedirHandler.getOutputStream() instanceof FileOutputStream);

        // get the in stream and use it to read from the file so we can validate it came from the right place
        FileInputStream inputStream = (FileInputStream) ioRedirHandler.getInputStream(); //NOPMD

        // read the input stream and verify the byte we put in the file is there
        int byteRead = inputStream.read();
        assertEquals(65, byteRead);

        // verify there is nothing else there
        assertEquals(-1, inputStream.read());

        // get the out stream and use it to write a byte to the file so we can validate it goes
        // where it should go
        FileOutputStream outputStream = (FileOutputStream) ioRedirHandler.getOutputStream(); //NOPMD
        outputStream.write(65);
        outputStream.close();

        // read the file and verify the byte we wrote is in fact there
        byte[] outFileBytes = Files.readAllBytes(outFile);
        assertEquals(1, outFileBytes.length);
        assertEquals(65, outFileBytes[0]);

        inputStream.close();
    }


    /**
     * Test integration of redirecting output with command "echo hello > outfile.txt"
     * The file is read to check that it contains "hello" followed by a (platform indepentent) newline
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     * @throws IOException
     */
    @Test
    public void testIntegrationOuputToFile() throws AbstractApplicationException, ShellException, IOException {

        // file to be used as output redirection
        Path outFile = IOUtils.resolveFilePath(OUTFILE);
        outFile.toFile().deleteOnExit();

        ShellImpl shellImpl = new ShellImpl();
        shellImpl.parseAndEvaluate("echo hello > " + outFile.toString(), System.out);

        // read the file and verify the bytes were written there
        byte[] outFileBytes = Files.readAllBytes(IOUtils.resolveFilePath(OUTFILE));
        assertTrue(5 + STRING_NEWLINE.length() == outFileBytes.length); // 5 chars in hello + newline
        assertArrayEquals(("hello" + STRING_NEWLINE).getBytes(), outFileBytes);

    }


    /**
     * Attempts to extract redirection options from empty arguments list
     * An exception is expected
     */
    @Test
    public void testFailsEmptyArgs() {

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList(),
          System.in,
          System.out,
          new ArgumentResolver()
        );
    
        ShellException shellException =
          assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_SYNTAX);

    }


    /**
     * Attempts to extract redirection options from null arguments list
     * An exception is expected
     */
    @Test
    public void testFailsNullArgs() {

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          null,
          System.in,
          System.out,
          new ArgumentResolver()
        );
    
        ShellException shellException =
          assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_SYNTAX);

    }

    /**
     * Attempts to call "echo hello >" without a destination for the output redirection
     * This should cause an exception of syntax error
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testFailsRedirOutputNoDest() {

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("echo", "hello", ">"),
          System.in,
          System.out,
          new ArgumentResolver()
        );
    
    
        ShellException shellException =
          assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_SYNTAX);
    }


    /**
     * Attempts to call "echo > /" redirecting output to a directory
     * This should cause an exception
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testFailsRedirOutputToDir() {

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
                Arrays.asList("echo", ">", "/"),
                System.in,
                System.out,
                new ArgumentResolver()
        );


        ShellException shellException =
                assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_IS_DIR);
    }


    /**
     * Attempts to call "echo hello > outfile1.txt > outfile2.txt" (with multiple outputs)
     * This should cause an exception of multiple streams
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testFailsRedirOutputMultStreams() {

        Path outFile1 = IOUtils.resolveFilePath("outfile1.txt");
        Path outFile2 = IOUtils.resolveFilePath("outfile2.txt");
        outFile1.toFile().deleteOnExit();

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("echo", "hello", ">", outFile1.toString(), ">", outFile2.toString()),
          System.in,
          System.out,
          new ArgumentResolver()
        );
    
    
        ShellException shellException =
          assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_MULTIPLE_STREAMS);

    }

    /**
     * Attempts to call redirect inexistent file into stdin
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testFailsRedirNonExistentInput() {

        // creates path but does not create the file
        Path inFile = IOUtils.resolveFilePath("ghost-infile.txt");

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("paste", "-", "<", inFile.toString()),
          System.in,
          System.out,
          new ArgumentResolver()
        );
    
    
        ShellException shellException =
          assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_FILE_NOT_FOUND);

    }

    /**
     * Attempts to call redirect with invalid syntax
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testFailsWhenInvalidRedir() {

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("echo", "<", "<"),
          System.in,
          System.out,
          new ArgumentResolver()
        );
    
        ShellException shellException =
          assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_SYNTAX);

    }

    /**
     * Attempts to call redirect when sending to multiple files
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testFailsWhenRedirToMultipleFiles() {

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("echo", "hi", ">", "*"),
          System.in,
          System.out,
          new ArgumentResolver()
        );
    
        ShellException shellException =
          assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_SYNTAX);

    }

}
