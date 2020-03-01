package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

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
 * <p>
 * Negative test cases:
 * - Empty args
 * - Null args
 */
public class IORedirectionHandlerTest {

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
        Path outFile = IOUtils.resolveFilePath("outfile.txt");

        // instantiate a redirection handler for the command
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(
                Arrays.asList("echo", "hello", ">", outFile.toString()),
                System.in,
                System.out,
                new ArgumentResolver()
        );

        // let the handler run
        ioRedirectionHandler.extractRedirOptions();

        // validate arguments not related to redirection were correctly identified
        List<String> expectedNoRedirArgs = Arrays.asList("echo", "hello");
        assertEquals(expectedNoRedirArgs, ioRedirectionHandler.getNoRedirArgsList());

        // validate the input stream is still stdin as it should
        assertEquals(System.in, ioRedirectionHandler.getInputStream());

        // validate the output stream is into a file
        assertTrue(ioRedirectionHandler.getOutputStream() instanceof FileOutputStream);

        // get the out stream and use it to write a byte to the file so we can validate it goes
        // where it should go
        FileOutputStream outputStream = (FileOutputStream) ioRedirectionHandler.getOutputStream();
        outputStream.write(65);
        outputStream.close();

        // read the file and verify the byte we wrote is in fact there
        byte[] outFileBytes = Files.readAllBytes(outFile);
        assertTrue(1 == outFileBytes.length);
        assertEquals(65, outFileBytes[0]);

        Files.delete(outFile); // cleanup

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
        Path inFile = IOUtils.resolveFilePath("infile.txt");
        Files.write(inFile, new byte[]{65}); // write an A (byte 65 in ASCII) into the file for validation

        // instantiate a redirection handler for the command
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(
                Arrays.asList("paste", "-", "<", inFile.toString()),
                System.in,
                System.out,
                new ArgumentResolver()
        );

        // let the handler run
        ioRedirectionHandler.extractRedirOptions();

        // validate arguments not related to redirection were correctly identified
        List<String> expectedNoRedirArgs = Arrays.asList("paste", "-");
        assertEquals(expectedNoRedirArgs, ioRedirectionHandler.getNoRedirArgsList());

        // validate the output stream is still stdout as it should
        assertEquals(System.out, ioRedirectionHandler.getOutputStream());

        // validate the input stream is from a file
        assertTrue(ioRedirectionHandler.getInputStream() instanceof FileInputStream);

        // get the in stream and use it to read from the file so we can validate it came from the right place
        FileInputStream inputStream = (FileInputStream) ioRedirectionHandler.getInputStream();

        // read the input stream and verify the byte we put in the file is there
        int b = inputStream.read();
        assertEquals(65, b);

        // verify there is nothing else there
        assertTrue(-1 == inputStream.read());

        // cleanup
        inputStream.close();
        Files.delete(inFile);

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
    public void testRedirInputAndOuputFromAndToFiles() throws AbstractApplicationException, ShellException, IOException {

        // file to be used as output redirection
        Path inFile = IOUtils.resolveFilePath("infile.txt");
        Files.write(inFile, new byte[]{65}); // write an A (byte 65 in ASCII) into the file for validation

        // file to be used as output redirection
        Path outFile = IOUtils.resolveFilePath("outfile.txt");

        // instantiate a redirection handler for the command
        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(
                Arrays.asList("paste", "-", "<", inFile.toString(), ">", outFile.toString()),
                System.in,
                System.out,
                new ArgumentResolver()
        );

        // let the handler run
        ioRedirectionHandler.extractRedirOptions();

        // validate arguments not related to redirection were correctly identified
        List<String> expectedNoRedirArgs = Arrays.asList("paste", "-");
        assertEquals(expectedNoRedirArgs, ioRedirectionHandler.getNoRedirArgsList());

        // validate the both streams are into or from files
        assertTrue(ioRedirectionHandler.getInputStream() instanceof FileInputStream);
        assertTrue(ioRedirectionHandler.getOutputStream() instanceof FileOutputStream);

        // get the in stream and use it to read from the file so we can validate it came from the right place
        FileInputStream inputStream = (FileInputStream) ioRedirectionHandler.getInputStream();

        // read the input stream and verify the byte we put in the file is there
        int b = inputStream.read();
        assertEquals(65, b);

        // verify there is nothing else there
        assertTrue(-1 == inputStream.read());

        // get the out stream and use it to write a byte to the file so we can validate it goes
        // where it should go
        FileOutputStream outputStream = (FileOutputStream) ioRedirectionHandler.getOutputStream();
        outputStream.write(65);
        outputStream.close();

        // read the file and verify the byte we wrote is in fact there
        byte[] outFileBytes = Files.readAllBytes(outFile);
        assertTrue(1 == outFileBytes.length);
        assertEquals(65, outFileBytes[0]);

        // cleanup
        inputStream.close();
        Files.delete(inFile);
        Files.delete(outFile);

    }


    /**
     * Attempts to extract redirection options from empty arguments list
     * An exception is expected
     */
    @Test
    public void testFailsEmptyArgs() {

        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(
                Arrays.asList(),
                System.in,
                System.out,
                new ArgumentResolver()
        );

        ShellException shellException =
                assertThrows(ShellException.class, () -> ioRedirectionHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_SYNTAX);

    }


    /**
     * Attempts to extract redirection options from null arguments list
     * An exception is expected
     */
    @Test
    public void testFailsNullArgs() {

        IORedirectionHandler ioRedirectionHandler = new IORedirectionHandler(
                null,
                System.in,
                System.out,
                new ArgumentResolver()
        );

        ShellException shellException =
                assertThrows(ShellException.class, () -> ioRedirectionHandler.extractRedirOptions());

        assertMsgContains(shellException, ERR_SYNTAX);

    }

}
