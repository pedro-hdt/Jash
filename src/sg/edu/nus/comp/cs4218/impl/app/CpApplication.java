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
        try {
            Path source = IOUtils.resolveFilePath(srcFile);
            Files.copy(source, source.resolveSibling(destFile), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw (CpException) new CpException(ERR_FILE_NOT_FOUND).initCause(e);
        }

        return null;
    }

    @Override
    public String cpFilesToFolder(String destFolder, String... fileName) throws Exception {

        List<String> invalidFiles = new ArrayList<>();

        for (String srcPath : fileName) {

            Path src = IOUtils.resolveFilePath(srcPath);

            if (!Files.exists(src)) {
                invalidFiles.add(srcPath); // signal this file does not exist to report it later then skip it
                continue;
            }

            if (shouldOverwrite) {
                // Can avoid this with assumption that target operand is always a directory
                if (!Files.isDirectory(IOUtils.resolveFilePath(destFolder))) {
                    FileOutputStream outputStream = new FileOutputStream(IOUtils.resolveFilePath(destFolder).toFile());//NOPMD
                    byte[] strToBytes = Files.readAllBytes(src);
                    outputStream.write(strToBytes);
                    outputStream.close();

                    Files.delete(src);
                    return null;
                }
                try {
                    // Assumption: Replacement doesn't work when a directory is being copied and target directory is non-empty and same name
                    Files.copy(src,
                            Paths.get(IOUtils.resolveFilePath(destFolder).toString(),
                                    src.getFileName().toString()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (DirectoryNotEmptyException dnee) {
                    throw (CpException) new CpException(ERR_CANNOT_OVERWRITE).initCause(dnee);
                }

            } else {
                // if overwriting is not allowed then only allow possibility of moving if its directory
                if (Files.isDirectory(IOUtils.resolveFilePath(destFolder))) {
                    Files.copy(src,
                            Paths.get(IOUtils.resolveFilePath(destFolder).toString(), srcPath));
                } else {
                    throw new CpException(ERR_NOT_MOVABLE);
                }
            }
        }

        if (!invalidFiles.isEmpty()) {
            StringBuilder sb = new StringBuilder();//NOPMD
            for (String f : invalidFiles) {
                sb.append(f);
                sb.append(" skipped: ");
                sb.append(ERR_FILE_NOT_FOUND);
                sb.append(STRING_NEWLINE);
            }
            throw new CpException(STRING_NEWLINE + sb.toString().trim());
        }

        return null;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CpException {

        if (args == null) {
            throw new CpException(ERR_NULL_ARGS);
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
            if (Files.isDirectory(IOUtils.resolveFilePath(targetOperand))) {
                cpFilesToFolder(targetOperand, sourceOperands.toArray(new String[0]));
            } else {
                String src = sourceOperands.get(0);
                if (!Files.exists(IOUtils.resolveFilePath(src))) {
                    throw new CpException(src + ": " + ERR_FILE_NOT_FOUND);
                }
                cpSrcFileToDestFile(sourceOperands.get(0), targetOperand);
            }
        } catch (Exception e) {
            throw (CpException) new CpException(e.getMessage()).initCause(e);
        }
    }
}