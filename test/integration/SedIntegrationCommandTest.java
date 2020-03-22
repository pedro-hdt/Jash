package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
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

public class SedIntegrationCommandTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private OutputStream stdout;

    private ShellImpl shell = new ShellImpl();


    @BeforeAll
    static void setupAll() {

        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "SedIntegrationTestFolder");
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
                + StringUtils.fileSeparator() + "SedIntegrationTestFolder");

        stdout.flush();
    }

    @Test
    public void testWithStdIn() {

        try {
            String stdin = "abc";
            System.setIn(new ByteArrayInputStream(stdin.getBytes()));

            shell.parseAndEvaluate("sed 's/abc/def/'", stdout);
            assertEquals("def" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    @DisplayName("Sed with EF2")
    public void testWithSort() {

        try {
            String stdin = "abc" + StringUtils.STRING_NEWLINE + "mmm";
            System.setIn(new ByteArrayInputStream(stdin.getBytes()));

            shell.parseAndEvaluate("sed 's/abc/zzz/' | sort", stdout);
            assertEquals("mmm" + StringUtils.STRING_NEWLINE +
                    "zzz" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Sed with EF1")
    public void testWithGrep() {

        try {
            String stdin = "abc" + StringUtils.STRING_NEWLINE + "mmm";
            System.setIn(new ByteArrayInputStream(stdin.getBytes()));

            shell.parseAndEvaluate("sed 's/abc/zzz/' | grep -c mmm", stdout);
            assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Sed with EF1")
    public void testWithWcAndIoredir() {

        try {
            String stdin = "abc" + StringUtils.STRING_NEWLINE + "mmm";
            System.setIn(new ByteArrayInputStream(stdin.getBytes()));

            shell.parseAndEvaluate("sed 's/abc/zzz/' > result.txt ; wc -cl result.txt", stdout);
            assertEquals("       2       8 result.txt" + StringUtils.STRING_NEWLINE, stdout.toString());

            Files.delete(Paths.get(Environment.currentDirectory, "result.txt"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Sed with EF2")
    public void testWithFind() {

        try {
            String stdin = "abc" + StringUtils.STRING_NEWLINE + "mmm";
            System.setIn(new ByteArrayInputStream(stdin.getBytes()));

            shell.parseAndEvaluate("sed 's/abc/zzz/' > find.txt | find ./ -name 'find.txt'", stdout);
            assertEquals("./find.txt" + StringUtils.STRING_NEWLINE, stdout.toString());

            Files.delete(Paths.get(Environment.currentDirectory, "find.txt"));

        } catch (Exception e) {
            fail();
        }
    }

}
