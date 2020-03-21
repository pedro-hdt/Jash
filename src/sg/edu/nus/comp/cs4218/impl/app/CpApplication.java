package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CpApplication implements CpInterface {

    private Boolean shouldOverwrite = false;

    @Override
    public String cpSrcFileToDestFile(String srcFile, String destFile) throws Exception {
        Path source = IOUtils.resolveFilePath(srcFile);
        if (!Files.exists(source)) {
            throw new CpException("'" + srcFile + "': " + ERR_FILE_NOT_FOUND);
        } else if (source.equals(source.resolveSibling(destFile))) {
            throw new CpException(String.format("'%s' and '%s' are the same file"));
        }
        Files.copy(source, source.resolveSibling(destFile), StandardCopyOption.REPLACE_EXISTING);

        return null;
    }

    @Override
    public String cpFilesToFolder(String destFolder, String... fileName) throws Exception {

        List<String> invalidFiles = new ArrayList<>();
        boolean hasOtherErrorOccurred = false;

        for (String srcPath : fileName) {

            Path src = IOUtils.resolveFilePath(srcPath);

            if (!Files.exists(src) || src.getParent().equals(IOUtils.resolveFilePath(destFolder))) {
                invalidFiles.add(srcPath); // signal this file does not exist to report it later then skip it
                continue;
            }

            if (shouldOverwrite) {
                // Can avoid this with assumption that target operand is always a directory
                if (!Files.isDirectory(IOUtils.resolveFilePath(destFolder)) && fileName.length == 1) {
                    FileOutputStream outputStream = new FileOutputStream(IOUtils.resolveFilePath(destFolder).toFile());//NOPMD
                    byte[] strToBytes = Files.readAllBytes(IOUtils.resolveFilePath(srcPath));
                    outputStream.write(strToBytes);
                    outputStream.close();

                    return null;
                } else if (!Files.isDirectory(IOUtils.resolveFilePath(destFolder)) && fileName.length > 1) {
                    throw new Exception("'" + srcPath + "' is not a directory.");
                }
                try {
                    // Assumption: Replacement doesn't work when a directory is being copied and target directory is non-empty and same name
                    Files.copy(IOUtils.resolveFilePath(srcPath),
                            Paths.get(IOUtils.resolveFilePath(destFolder).toString(),
                                    IOUtils.resolveFilePath(srcPath).getFileName().toString()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (DirectoryNotEmptyException dnee) {
                    throw new Exception(ERR_CANNOT_OVERWRITE + " non-empty directory: " + destFolder);
                } catch (FileSystemException fse) {
                    hasOtherErrorOccurred = true;
                }

            } else {
                // if overwriting is not allowed then only allow possibility of copying if its directory
                cpFilesToFolderNoOverWrite(destFolder, srcPath);
            }
        }

        if (!invalidFiles.isEmpty()) {
            StringBuilder sb = new StringBuilder();//NOPMD
            for (String f : invalidFiles) {
                sb.append(f);
                sb.append(" skipped: ");
                if (!Files.exists(IOUtils.resolveFilePath(f))) {
                    sb.append(ERR_FILE_NOT_FOUND);
                } else {
                    sb.append(String.format("'%s' and '%s' are the same file", f, f));
                }
                sb.append(STRING_NEWLINE);
            }
            throw new Exception(STRING_NEWLINE + sb.toString().trim());
        }

        if (hasOtherErrorOccurred) {
            throw new Exception("error copying file");
        }

        return null;
    }

    private void cpFilesToFolderNoOverWrite(String destFolder, String srcPath) throws Exception {
        if (Files.isDirectory(IOUtils.resolveFilePath(destFolder))) {
            try {
                Files.copy(IOUtils.resolveFilePath(srcPath),
                        Paths.get(IOUtils.resolveFilePath(destFolder).toString(), srcPath));
            } catch (FileAlreadyExistsException faee) {
                throw new Exception(ERR_CANNOT_OVERWRITE + " file already exists: " + srcPath); //NOPMD
            }

        } else {
            throw new Exception("'" + srcPath + "' is a file and replacement is not allowed.");
        }
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CpException {

        if (args == null) {
            throw new CpException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new CpException(ERR_NO_OSTREAM);
        }

        // Assumption: Will assume multiple args passed when regex is used with only first arg passed
        if (args.length < 2) {
            throw new CpException(ERR_NO_ARGS);
        }

        CpArgsParser parser = new CpArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw (CpException) new CpException(e.getMessage()).initCause(e);
        }

        shouldOverwrite = parser.shouldOverwrite();
        List<String> sourceOperands = parser.getSourceOperands();
        String targetOperand = parser.getTargetOperand();

        if (sourceOperands.isEmpty() || targetOperand == null) {
            throw new CpException(ERR_NO_FILE_ARGS);
        }

        try {
            if (Files.exists(IOUtils.resolveFilePath(targetOperand))) {
                cpFilesToFolder(targetOperand, sourceOperands.toArray(new String[0]));
            } else {
                if (sourceOperands.size() > 1) {
                    throw new Exception("can't copy multiple files into a single one");
                }
                cpSrcFileToDestFile(sourceOperands.get(0), targetOperand);
            }
        } catch (Exception e) {
            throw (CpException) new CpException(e.getMessage()).initCause(e);
        }
    }
}