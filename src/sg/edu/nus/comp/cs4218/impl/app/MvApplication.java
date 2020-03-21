package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_CANNOT_OVERWRITE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

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
import java.util.List;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings("PMD.PreserveStackTrace")
public class MvApplication implements MvInterface {

    private Boolean shouldOverwrite = false;

    @Override
    public String mvSrcFileToDestFile(String srcFile, String destFile) throws Exception {
        Path source = IOUtils.resolveFilePath(srcFile);
        Files.move(source, source.resolveSibling(destFile));

        return null;
    }

    @Override
    public String mvFilesToFolder(String destFolder, String... fileName) throws Exception {

        boolean hasErrorOccurred = false;

        for (String srcPath: fileName){
            if (shouldOverwrite) {
                // Can avoid this with assumption that target operand is always a directory
                if (!Files.isDirectory(IOUtils.resolveFilePath(destFolder)) && fileName.length == 1) {
                    FileOutputStream outputStream = new FileOutputStream(IOUtils.resolveFilePath(destFolder).toFile());//NOPMD
                    byte[] strToBytes = Files.readAllBytes(IOUtils.resolveFilePath(srcPath));
                    outputStream.write(strToBytes);
                    outputStream.close();

                    if (!IOUtils.resolveFilePath(srcPath).equals(IOUtils.resolveFilePath(destFolder))) {
                        Files.delete(IOUtils.resolveFilePath(srcPath));
                    }
                    return null;
                } else if (!Files.isDirectory(IOUtils.resolveFilePath(destFolder)) && fileName.length > 1) {
                    throw new Exception("'" + srcPath + "' is not a directory.");
                }
                try {
                    // Assumption: Replacement doesn't work when a directory is being moved and target directory is non-empty and same name
                    Files.move(IOUtils.resolveFilePath(srcPath),
                            Paths.get(IOUtils.resolveFilePath(destFolder).toString(),
                                    IOUtils.resolveFilePath(srcPath).getFileName().toString()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (DirectoryNotEmptyException dnee) {
                    throw new Exception(ERR_CANNOT_OVERWRITE + " non-empty directory: " + destFolder);
                } catch (FileSystemException fse) {
                    hasErrorOccurred = true;
                }

            } else {
                // if overwriting is not allowed then only allow possibility of moving if its directory
                mvFilesToFolderNoOverWrite(destFolder, srcPath);
            }
        }

        if (hasErrorOccurred) {
            throw new Exception("error moving file");
        }
        return null;
    }

    private void mvFilesToFolderNoOverWrite(String destFolder, String srcPath) throws Exception {
        if (Files.isDirectory(IOUtils.resolveFilePath(destFolder))) {
            try {
                Files.move(IOUtils.resolveFilePath(srcPath),
                        Paths.get(IOUtils.resolveFilePath(destFolder).toString(), srcPath));
            } catch (FileAlreadyExistsException faee) {
                throw new Exception(ERR_CANNOT_OVERWRITE + " file already exists: " + srcPath); //NOPMD
            }

        } else {
            throw new Exception("'" + srcPath + "' is a file and replacement not allowed.");
        }
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        if (args == null) {
            throw new MvException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new MvException(ERR_NO_OSTREAM);
        }

        // Assumption: Will assume multiple args passed when regex is used with only first arg passed
        if (args.length < 2) {
            throw new MvException(ERR_NO_FILE_ARGS);
        }

        MvArgsParser parser = new MvArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MvException(e.getMessage());
        }

        shouldOverwrite = parser.shouldOverwrite();
        List<String> sourceOperands = parser.getSourceOperands();
        String targetOperand = parser.getTargetOperand();

        for (String sourceOperand : sourceOperands) {
            if (!Files.exists(IOUtils.resolveFilePath(sourceOperand))) {
                throw new MvException("Cannot find '" + sourceOperand + "'. " +  ERR_FILE_NOT_FOUND + ".");
            }
        }

        try {
            if (Files.exists(IOUtils.resolveFilePath(targetOperand))) {
                mvFilesToFolder(targetOperand, sourceOperands.toArray(new String[0]));
            } else {
                if (sourceOperands.size() > 1) {
                    throw new Exception("can't rename multiple files");
                }
                mvSrcFileToDestFile(sourceOperands.get(0), targetOperand);
            }
        }  catch (Exception e) {
            throw new MvException(e.getMessage());
        }
    }
}
