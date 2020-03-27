package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.resolveFilePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class TestUtils {
    public static final String TEMP_DIR_PREFIX = "CS4218";
    private static String originalWorkingDir = null;

    public static void enterTempDir() throws IOException {
        originalWorkingDir = Environment.currentDirectory;
        Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        Environment.currentDirectory = tempDir.toString();
        System.out.println(String.format("Current directory is %s", Environment.currentDirectory));
    }

    public static void leaveTempDir() throws IOException {
        Environment.currentDirectory = originalWorkingDir;
        System.out.println(String.format("Current directory is %s", Environment.currentDirectory));
    }

    public static String getRunOutputText(List<String> args, String inputText) throws ShellException, AbstractApplicationException {
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        ApplicationRunner appRunner = new ApplicationRunner();
        ArgumentResolver argumentResolver = new ArgumentResolver();
        Command command = new CallCommand(args, appRunner, argumentResolver);
        command.evaluate(inputStream, outputStream);
        return outputStream.toString();
    }

    public static String getRunOutputText(List<String> args, String inputText, String outputFileName) throws ShellException, AbstractApplicationException, IOException {
        getRunOutputText(args, inputText);
        return readFileContent(resolveFilePath(Environment.currentDirectory).resolve(outputFileName));
    }

    public static String getRunOutputText(String commandText, String inputText) throws ShellException, AbstractApplicationException {
        InputStream inputStream = new ByteArrayInputStream(inputText.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        Command command = CommandBuilder.parseCommand(commandText, new ApplicationRunner());
        command.evaluate(inputStream, outputStream);
        return outputStream.toString();
    }

    public static String readFileContent(Path path, Charset charset) throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, charset);
    }

    public static String readFileContent(String pathString, Charset charset) throws Exception {
        Path path = resolveFilePath(pathString);
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, charset);
    }

    public static String readFileContent(Path path) throws IOException {
        return readFileContent(path, StandardCharsets.UTF_8);
    }

    public static void writeFileContent(Path path, String content, Charset charset) throws IOException {
        Files.write(path, content.getBytes(charset));
    }

    public static void writeFileContent(Path path, String content) throws IOException {
        writeFileContent(path, content, StandardCharsets.UTF_8);
    }

    public static String getTempFileName() {
        return UUID.randomUUID().toString();
    }

    public static String createTempFileWithContent(String content) throws IOException {
        String fileName = getTempFileName();
        Path path = resolveFilePath(Environment.currentDirectory).resolve(fileName);
        writeFileContent(path, content);
        return path.toString();
    }

    public static File createFolder(String dir, String name) throws Exception {
        File file = resolveFilePath(dir).resolve(name).toFile();
        file.mkdir();
        return file;
    }

    public static void createFile(String fileName, String content) throws Exception {
        Files.write(resolveFilePath(fileName), content.getBytes());
    }

    public static File createFileWithContent(String dir, String name, String content) throws Exception {
        File file = resolveFilePath(dir).resolve(name).toFile();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(content);
        fileWriter.close();
        return file;
    }

    public static boolean compareFiles(File first, File second) throws Exception {
        return readFileContent(first.getPath(), Charset.forName("UTF-8")).equals(readFileContent(second.getPath(), Charset.forName("UTF-8")));
    }

    public static boolean compareFiles(String firstName, String secondName) throws Exception {
        return readFileContent(firstName, Charset.forName("UTF-8")).equals(readFileContent(secondName, Charset.forName("UTF-8")));
    }

    public static boolean exist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static void createDirectory(String directoryName) throws Exception {
        Files.createDirectories(resolveFilePath(directoryName));
    }

    public static String autoCRLF(String s) {
        return s.replace("\r\n", "\n").replace("\n", STRING_NEWLINE);
    }
}
