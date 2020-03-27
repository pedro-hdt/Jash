package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.resolveFilePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class RmApplication implements RmInterface {

    private static final String COMMAND_HEADER = "rm: ";
    private String prefixPath;
    private OutputStream outputStream;
    // delete folder if it is empty with isEmptyFolder = true
    // delete file in folder if isRecursive = true
    // if isEmptyFolder and isRecursive = false, only delete file
    // if fileName doesn't exist => error message
    // if delete non empty folder without recursive => ignore
    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws Exception {
        if (isEmptyFolder == null || isRecursive == null || fileName == null) {
            throw new RmException(ERR_NULL_ARGS);
        }

        if (fileName.length == 0) {
            throw new RmException(ERR_MISSING_ARG);
        }

        //setCurrentPrefixPath();
        //setOutputStream(System.out);

        for (String name: fileName) {
            String filePath = String.valueOf(resolveFilePath(prefixPath).resolve(name));
            File file = new File(filePath);
            if (file.isFile()) {
                removeFile(file, outputStream);
            } else if (file.isDirectory()) {
                String[] subFiles = file.list();
                if (subFiles == null || subFiles.length == 0) {
                    if (isEmptyFolder) {
                        removeFile(file, outputStream);
                    } else {
                        outputStream.write((COMMAND_HEADER
                                + filePath
                                + ": "
                                + ERR_IS_DIR
                                + STRING_NEWLINE).getBytes());
                    }
                } else {
                    if (isRecursive) {
                        String prevPath = prefixPath;
                        prefixPath = filePath;
                        remove(isEmptyFolder, isRecursive, subFiles);
                        prefixPath = prevPath;
                        File fileAfter = new File(filePath);
                        if (fileAfter.getCanonicalPath().equals(Environment.currentDirectory)) {
                            outputStream.write((COMMAND_HEADER
                                    + fileAfter.getCanonicalPath()
                                    + ": "
                                    + ERR_CURR_DIR
                                    + STRING_NEWLINE).getBytes());
                        } else if (fileAfter.list().length == 0 && isEmptyFolder) {
                            removeFile(fileAfter, outputStream);
                        } else {
                            outputStream.write((COMMAND_HEADER
                                    + filePath
                                    + ": "
                                    + ERR_IS_DIR
                                    + STRING_NEWLINE).getBytes());
                        }
                    } else {
                        outputStream.write((COMMAND_HEADER
                                + filePath
                                + ": "
                                + ERR_IS_NON_EMPTY_DIR
                                + STRING_NEWLINE).getBytes());
                    }
                }
            } else {
                outputStream.write((COMMAND_HEADER
                        + filePath
                        + ": "
                        + ERR_FILE_NOT_FOUND
                        + STRING_NEWLINE).getBytes());
            }
        }
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws RmException {
        try {
            if (args == null) {
                throw new RmException(ERR_NULL_ARGS);
            }
            if (args.length == 0) {
                throw new RmException(ERR_NO_ARGS);
            }
            if (stdin == null || stdout == null) {
                throw new RmException(ERR_NULL_STREAMS);
            }
            setOutputStream(stdout);
            RmArgsParser parser = new RmArgsParser();
            try {
                parser.parse(args);
            } catch (InvalidArgsException e) {
                throw new RmException(e.getMessage());
            }

            setCurrentPrefixPath();
            Boolean recursive = parser.isRecursive();
            Boolean isFolders = parser.isFoldersOnly();
            String[] files = parser.getFiles()
                    .toArray(new String[parser.getFiles().size()]);
            remove(isFolders, recursive, files);
        } catch (RmException rmException) {
            throw rmException;
        } catch (Exception e) {
            throw new RmException(e.getMessage());
        }
    }

    private void removeFile(File file, OutputStream outputStream) throws Exception {
        if (!Files.isWritable(file.toPath())) {
            outputStream.write((COMMAND_HEADER
                    + file.getPath()
                    + ": "
                    + ERR_NO_PERM
                    + STRING_NEWLINE).getBytes());
            return;
        }
        boolean isDeleted =  file.delete();
        if (!isDeleted) {
            outputStream.write((COMMAND_HEADER
                    + file.getPath()
                    + ": "
                    + ERR_NO_PERM
                    + STRING_NEWLINE).getBytes());
        }
    }

    // for testing purpose
    public void setCurrentPrefixPath() {
        prefixPath = String.valueOf(Paths.get(Environment.currentDirectory));
    }

    public void setOutputStream(OutputStream stdout) {
        outputStream = stdout;
    }
}
