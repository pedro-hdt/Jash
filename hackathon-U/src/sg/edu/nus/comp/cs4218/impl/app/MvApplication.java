package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.app.args.MvArguments;
import sg.edu.nus.comp.cs4218.impl.app.args.SortArguments;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.resolveFilePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class MvApplication implements MvInterface {

    private boolean doesNotOverwrite;
    private static final String COMMAND_HEADER = "mv: ";
    private OutputStream outputStream;

    @Override
    public String mvSrcFileToDestFile(String srcFileName, String destFileName) throws MvException {
        File srcFile = new File(getPathStringFromFileName(srcFileName));
        File destFile = new File(getPathStringFromFileName(destFileName));
        if (!srcFile.exists()) {
            throw new MvException(srcFileName
                    + ": "
                    + ERR_FILE_NOT_FOUND);
        }

        try {
            if (srcFile.isFile()) {
                    if (destFile.isFile()) {
                        if (srcFile.equals(destFile)) {
                            throw new MvException(String.format(ERR_SAME_FILE, srcFile.getName(), destFile.getName()));
                        }
                        if (!doesNotOverwrite) {
                            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            srcFile.delete();
                        } else {
                            throw new MvException(String.format(ERR_EXISTING_DES_CANNOT_REPLACE, destFile.getName()));
                        }
                    }
                    if (!destFile.exists()) {
                        Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        srcFile.delete();
                    }
            }
            if (srcFile.isDirectory()) {
                if (!destFile.exists()) {
                    Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    srcFile.delete();
                }
            }
        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            throw new MvException(e.getMessage());
        }
        return null;
    }

    @Override
    public String mvFilesToFolder(String destFolderName, String... fileNames) throws MvException {
        File destFolder = new File(getPathStringFromFileName(destFolderName));
        if (!destFolder.exists()) {
            throw new MvException(destFolderName
                    + ": "
                    + ERR_FILE_NOT_FOUND);
        }
        try {
            for (String fileName: fileNames) {
                File srcFile = new File(getPathStringFromFileName(fileName));
                if (!srcFile.exists()) {
                    outputStream.write((COMMAND_HEADER
                            + fileName
                            + ": "
                            + ERR_FILE_NOT_FOUND
                            + STRING_NEWLINE).getBytes());
                    continue;
                }

                if (destFolder.equals(srcFile)) {
                    outputStream.write((COMMAND_HEADER
                            + String.format(ERR_SAME_DIR, fileName, destFolderName) + STRING_NEWLINE).getBytes());
                    continue;
                }

                String newFilePath = String.valueOf(resolveFilePath(destFolderName).resolve(srcFile.getName()));
                File destFile = new File(newFilePath);
                if (destFile.exists() && doesNotOverwrite) {
                    outputStream.write((COMMAND_HEADER
                            + String.format(ERR_EXISTING_DES_IN_DIR_CANNOT_REPLACE, fileName, destFolderName) + STRING_NEWLINE).getBytes());
                    continue;
                }
                Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                srcFile.delete();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new MvException(ERR_WRITE_STREAM);
        } catch (Exception e) {
            throw new MvException(e.getMessage());
        }
        return null;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        // Format: mv [-n] SOURCE TARGET
        //         mv [-n] [SOURCE] ... DIRECTORY
        try {
            if (args == null) {
                throw new MvException(ERR_NULL_ARGS);
            }

            if (stdin == null || stdout == null) {
                throw new MvException(ERR_NULL_STREAMS);
            }
            setOutputStream(stdout);
            MvArguments mvArgumentsArgs = new MvArguments();
            mvArgumentsArgs.parse(args);
            setDoesNotOverwrite(mvArgumentsArgs.doesNotOverWrite());

            List<String> files = mvArgumentsArgs.getFiles();
            if (files == null || files.size() < 2) {
                throw new MvException(ERR_MISSING_ARG);
            }
            String lastFileName = files.get(files.size() - 1);
            File lastFile = new File(getPathStringFromFileName(lastFileName));

            String[] fileNames = new String[files.size() - 1];
            for (int i = 0; i < files.size() - 1; i++) {
                fileNames[i] = files.get(i);
            }
            if (files.size() == 2) {
                String srcFileName = getPathStringFromFileName(files.get(0));
                String destFileName = getPathStringFromFileName(files.get(1));
                File srcFile = new File(srcFileName);
                File destFile = new File(destFileName);
                if (destFile.isFile()) {
                    if (srcFile.isFile()) {
                        mvSrcFileToDestFile(files.get(0), files.get(1));
                    } else if (srcFile.isDirectory()) {
                        throw new MvException(String.format(ERR_SRC_DIR_DES_FILE, files.get(0), files.get(1)));
                    } else {
                        throw new MvException(files.get(0)
                                + ": "
                                + ERR_FILE_NOT_FOUND);
                    }
                }

                if (destFile.isDirectory()) {
                    mvFilesToFolder(destFileName, fileNames);
                }

                if (!destFile.exists()) {
                    mvSrcFileToDestFile(srcFileName, destFileName);
                }
            } else {
                if (!lastFile.isDirectory()) {
                    if (lastFile.isFile()) {
                        throw new MvException(lastFileName
                                + ": "
                                + ERR_IS_NOT_DIR);
                    } else {
                        throw new MvException(lastFileName
                                + ": "
                                + ERR_FILE_NOT_FOUND);
                    }
                }
                mvFilesToFolder(lastFileName, fileNames);
            }
        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            throw new MvException(e.getMessage());
        }
    }

    public void setDoesNotOverwrite(boolean doesNotOverwrite) {
        this.doesNotOverwrite = doesNotOverwrite;
    }

    public String getPathStringFromFileName(String fileName) {
        return String.valueOf(resolveFilePath(fileName));
    }

    public void setOutputStream(OutputStream stdout) {
        outputStream = stdout;
    }
}
