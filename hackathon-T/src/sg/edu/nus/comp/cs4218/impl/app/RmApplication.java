package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class RmApplication implements RmInterface {

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws RmException {
        if (fileName == null) {
            throw new RmException(ERR_NULL_ARGS);
        }
        if (fileName.length == 0) {
            throw new RmException(ERR_MISSING_ARG);
        }

        // "rm -r -d" & "rm -r"
        if (isRecursive) {
            removeRecursive(fileName);
        }
        // "rm -d"
        if (!isRecursive && isEmptyFolder) {
            removeDirectory(fileName);
        }
        // "rm"
        if (!isRecursive && !isEmptyFolder) {
            removeFile(fileName);
        }
    }

    private void removeRecursive(String... fileName) {
        // First round is to ensure that all files specified exist
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                System.err.println(ERR_FILE_NOT_FOUND + ": " + file);
                continue;
            }
            if (!node.getParentFile().canWrite() || !node.getParentFile().canExecute()) {
                System.err.println(ERR_NO_PERM + ": " + file + "\n");
                continue;
            }
            if (node.isDirectory() && node.list().length == 0) {
                System.err.println(ERR_NO_PERM + ": " + file + "\n");
                continue;
            }

            if (node.isDirectory()) {
                for (String fileToDelete : node.list()) {
                    removeRecursive(file + "/" + fileToDelete);
                }
            }

            node.delete();
        }
        return;
    }

    private void removeDirectory(String... fileName) {
        // First round is to ensure that all files specified exists and is an empty directory.
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                System.err.println(ERR_FILE_NOT_FOUND + ": " + file + "\n");
                continue;
            }
            if (!node.isDirectory()) {
                System.err.println(ERR_IS_NOT_DIR + ": " + file + "\n");
                continue;
            }
            if (node.list().length != 0) {
                System.err.println(ERR_DIR_NOT_EMPTY + ": " + file + "\n");
                continue;
            }
            if (!node.getParentFile().canWrite() || !node.getParentFile().canExecute()) {
                System.err.println(ERR_NO_PERM + ": " + file + "\n");
                continue;
            }
            node.delete();
        }
        return;
    }

    private void removeFile(String... fileName) {
        // First round is to ensure that all files specified exists and is not a directory.
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                System.err.println(ERR_FILE_NOT_FOUND + ": " + file + "\n");
                continue;
            }
            if (node.isDirectory()) {
                System.err.println(ERR_IS_DIR + ": " + file + "\n");
                continue;
            }
            if (!node.getParentFile().canWrite() || !node.getParentFile().canExecute()) {
                System.err.println(ERR_NO_PERM + ": " + file + "\n");
                continue;
            }
            node.delete();
        }
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws RmException {
        if (args == null) {
            throw new RmException(ERR_NULL_ARGS);
        }
        RmArgsParser parser = new RmArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new RmException(e.getMessage());
        }
        Boolean directoriesOnly = parser.isDirectoriesOnly();
        Boolean recursiveOnly = parser.isRecursive();
        String[] thingsToDelete = parser.getFilesOrDirectoriesToDelete();
        try {
            remove(directoriesOnly, recursiveOnly, thingsToDelete);
        } catch (RmException re) {
            throw re;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}