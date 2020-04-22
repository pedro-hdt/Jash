package hack.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Permission;

public class SaferShellImpl {

    public static final String TEMP_DIR_PREFIX = "CS4218";
    private static String originalWorkingDir = null;
    private static File lastTouchedFile = null;

    private static class ExitTrappedException extends SecurityException {
    }

    private static void disallowExitCalls() {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }

            public void checkPermission(Permission perm, Object context) {
            }

            public void checkExit(int status) {
                super.checkExit(status);
                throw new ExitTrappedException();
            }
        };
        System.setSecurityManager(securityManager);
    }

    private static void allowExitCalls() {
        System.setSecurityManager(null);
    }

    public static void enterTempDir() throws IOException {
        originalWorkingDir = Environment.currentDirectory;
        Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        Environment.currentDirectory = tempDir.toString();
//        System.out.println(String.format("Current directory is %s", Environment.currentDirectory));
    }

    public static void leaveTempDir() throws IOException {
        Environment.currentDirectory = originalWorkingDir;
//        System.out.println(String.format("Current directory is %s", Environment.currentDirectory));
    }

    public static boolean isSpecialCommand(String commandString) {
        return commandString.startsWith("__mkdir ") ||
                commandString.startsWith("__append ") ||
                commandString.startsWith("__append_pwd") ||
                commandString.startsWith("__append_realpath ") ||
                commandString.startsWith("__touch ") ||
                commandString.startsWith("__pwd") ||
                commandString.startsWith("__realpath ") ||
                commandString.startsWith("#");
    }

    public static void runSpecialCommand(String commandString, PrintStream printStream) throws Exception {
        if (commandString.startsWith("#")) {
            // ignore
        } else if (commandString.startsWith("__mkdir ")) {
            String path = commandString.substring("__mkdir ".length());
            Paths.get(Environment.currentDirectory).resolve(path).toFile().mkdirs();
        } else if (commandString.startsWith("__touch ")) {
            String path = commandString.substring("__touch ".length());
            File file = Paths.get(Environment.currentDirectory).resolve(path).toFile();
            file.getParentFile().mkdirs();
            file.createNewFile();
            lastTouchedFile = file;
        } else if (commandString.startsWith("__append ")) {
            String line = commandString.substring("__append ".length()) + System.lineSeparator();
            Files.write(lastTouchedFile.toPath(), line.getBytes(), StandardOpenOption.APPEND);
        } else if (commandString.startsWith("__append_pwd")) {
            String line = Paths.get(Environment.currentDirectory).toAbsolutePath().toString() + System.lineSeparator();
            Files.write(lastTouchedFile.toPath(), line.getBytes(), StandardOpenOption.APPEND);
        } else if (commandString.startsWith("__append_realpath ")) {
            String arg = commandString.substring("__append_realpath ".length());
            String line = Paths.get(Environment.currentDirectory).resolve(arg).toAbsolutePath().toString() + System.lineSeparator();
            Files.write(lastTouchedFile.toPath(), line.getBytes(), StandardOpenOption.APPEND);
        } else if (commandString.startsWith("__pwd")) {
            String s = Paths.get(Environment.currentDirectory).toAbsolutePath().toString();
            printStream.println(s);
        } else if (commandString.startsWith("__realpath")) {
            String arg = commandString.substring("__realpath ".length());
            String s = Paths.get(Environment.currentDirectory).resolve(arg).toAbsolutePath().toString();
            printStream.println(s);
        } else {
            throw new Exception("Unknown special command: " + commandString);
        }
    }

    public static void run(InputStream inputStream, OutputStream outputStream, boolean allowSpecialCommand) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        PrintStream printStream = new PrintStream(outputStream);
        Shell shell = new ShellImpl();

        while (true) {
            try {
                String commandString = reader.readLine();
                if (commandString == null) {
                    break;
                } else if (StringUtils.isBlank(commandString)) {
                    continue;
                } else if (allowSpecialCommand && isSpecialCommand(commandString)) {
                    runSpecialCommand(commandString, printStream);
                } else {
                    shell.parseAndEvaluate(commandString, outputStream);
                }
            } catch (ExitException | ExitTrappedException ignored) {
                break;
            } catch (Exception e) {
                printStream.println("error: " + e.getMessage());
            }
        }
    }

    public static void runSafer(InputStream inputStream, OutputStream outputStream, boolean allowSpecialCommand) {
        disallowExitCalls();
        try {
            enterTempDir();
            try {
                run(inputStream, outputStream, allowSpecialCommand);
            } finally {
                leaveTempDir();
            }
        } catch (IOException e) {
            System.out.println("error: " + e.getMessage());
        } finally {
            allowExitCalls();
        }
    }

    public static void runSafer(InputStream inputStream, OutputStream outputStream) {
        runSafer(inputStream, outputStream, true);
    }

    public static void main(String... args) throws IOException {
        runSafer(System.in, System.out);
    }
}
