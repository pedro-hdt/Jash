package hack.util;

import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SystemTestRunner {

    public static String getOutputText(String inputText) {
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        SaferShellImpl.runSafer(inputStream, outputStream);
        return outputStream.toString();
    }

    public static String readFileContent(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }

    public static String simplifiedLine(String line) {
        final String[] ERROR_PREFIX = {
                "shell:",
                "error:",
                "ls:",
                "find:",
                "wc:",
                "rm:",
                "echo:",
                "exit:",
                "grep:",
                "sort:",
                "paste:",
                "diff:",
                "cd:",
                "sed:",
                "cp:",
                "mv:",
                "cut:"
        };
        final String SIMPLIFIED_ERROR_MESSAGE = "<some error message>";
        for (String prefix : ERROR_PREFIX) {
            if (line.startsWith(prefix)) {
                return SIMPLIFIED_ERROR_MESSAGE;
            }
        }
        return line;
    }

    public static String simplifyErrors(String text) {
        Pattern p = Pattern.compile("\\r?\\n");
        Matcher m = p.matcher(text);
        StringBuilder result = new StringBuilder();
        int caret = 0;
        while (m.find()) {
            result.append(simplifiedLine(text.substring(caret, m.start())));
            result.append(text, m.start(), m.end());
            caret = m.end();
        }
        result.append(simplifiedLine(text.substring(caret)));
        return result.toString();
    }

    public static void run(String inputPath, String outputPath) throws IOException {
        Path root = Paths.get("rebuttal", "team19", "hack", "tests");
        String inputText = readFileContent(root.resolve(inputPath));
        String outputText = simplifyErrors(readFileContent(root.resolve(outputPath)));
        String actualText = simplifyErrors(SystemTestRunner.getOutputText(inputText));
        assertEquals(outputText, actualText);
    }

    public static void run(String testID) throws IOException {
        run(testID + ".in.txt", testID + ".ok.txt");
    }
}
