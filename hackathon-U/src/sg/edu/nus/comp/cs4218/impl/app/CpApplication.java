package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.resolveFilePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CpApplication implements CpInterface {

    private static final String COMMAND_HEADER = "cp: ";
    private OutputStream outputStream;

    @Override
    public String cpSrcFileToDestFile(String srcFile, String destFile) throws Exception {

        File src = resolveFilePath(srcFile).toFile();

        File dest = resolveFilePath(destFile).toFile();

        // if destFile does not exist, it will be created
        if (!src.isFile()) {
            throw new CpException(ERR_FILE_NOT_FOUND);
        }

        Files.copy(src.toPath(), dest.toPath(), REPLACE_EXISTING);
        return null;
    }

    @Override
    public String cpFilesToFolder(String destFolder, String... fileName) throws Exception {
        Path destDirPath = resolveFilePath(destFolder);
        File destDir = new File(String.valueOf(destDirPath));

        if (!destDir.isDirectory()) {
            throw new CpException(destFolder
                    + ": "
                    + ERR_FILE_NOT_FOUND);
        }
        for (String name: fileName) {
            File file = resolveFilePath(name).toFile();
            File dest = destDirPath.resolve(file.getName()).toFile();

            if (!file.exists()) {
                outputStream.write((COMMAND_HEADER
                        + name
                        + ": "
                        + ERR_FILE_NOT_FOUND
                        + STRING_NEWLINE).getBytes());
                continue;
            }
            if (file.isFile()) {
                Files.copy(file.toPath(), dest.toPath(), REPLACE_EXISTING);
            } else {
                copyDirectory(file, dest);
            }
        }
        return null;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CpException {
        try {
            if (args == null) {
                throw new CpException(ERR_NULL_ARGS);
            }

            if (stdin == null || stdout == null) {
                throw new CpException(ERR_NULL_STREAMS);
            }

            if (args.length <= 1) {
                throw new CpException(ERR_MISSING_ARG);
            }

            setOutputStream(stdout);

            String lastArg = args[args.length - 1];

            if (args.length > 2 && !doesFileExist(lastArg)) {
                throw new CpException(lastArg
                        + ": "
                        + ERR_FILE_NOT_FOUND);
            }

            if (isFolder(lastArg)) {
                String[] fileNames = new String[args.length - 1];
                for (int i = 0; i < args.length - 1; i++) {
                    fileNames[i] = args[i];
                }
                cpFilesToFolder(lastArg, fileNames);
            } else {
                if (args.length > 2) {
                    throw new CpException(ERR_TOO_MANY_ARGS);
                }

                cpSrcFileToDestFile(args[0], args[1]);
            }
        } catch (CpException cpException) {
            throw cpException;
        } catch (Exception e) {
            throw new CpException(e.getMessage());
        }
    }

    private boolean isFolder(String fileName) {
        File file = resolveFilePath(fileName).toFile();
        return file.isDirectory();
    }

    private boolean doesFileExist(String fileName) {
        File file = resolveFilePath(fileName).toFile();
        return file.exists();
    }

    public void setOutputStream(OutputStream stdout) {
        outputStream = stdout;
    }

    public static void copyDirectory(File sourceDir, File targetDir)
            throws IOException {
        if (sourceDir.isDirectory()) {
            copyDirectoryRecursively(sourceDir, targetDir);
        } else {
            Files.copy(sourceDir.toPath(), targetDir.toPath());
        }
    }
    // recursive method to copy directory and sub-diretory in Java
    private static void copyDirectoryRecursively(File source, File target)
            throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }

        for (String child : source.list()) {
            copyDirectory(new File(source, child), new File(target, child));
        }
    }
}
