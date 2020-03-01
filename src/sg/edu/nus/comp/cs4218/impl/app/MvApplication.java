package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

public class MvApplication implements MvInterface {

    private Boolean shouldOverwrite = false;

    @Override
    public String mvSrcFileToDestFile(String srcFile, String destFile) throws Exception {
        Path source = IOUtils.resolveFilePath(srcFile);
        if (!Files.exists(source)) {
            throw new MvException(ERR_NO_FILE_ARGS);
        }
        Files.move(source, source.resolveSibling(destFile));
        return null;
    }

    @Override
    public String mvFilesToFolder(String destFolder, String... fileName) throws Exception {
        for (String srcPath: fileName){
            if (shouldOverwrite) {
                // Can avoid this with assumption that target operand is always a directory
                if (!Files.isDirectory(IOUtils.resolveFilePath(destFolder))) {
                    FileOutputStream outputStream = new FileOutputStream(IOUtils.resolveFilePath(destFolder).toFile());
                    byte[] strToBytes = Files.readAllBytes(IOUtils.resolveFilePath(srcPath));
                    outputStream.write(strToBytes);
                    outputStream.close();

                    Files.delete(IOUtils.resolveFilePath(srcPath));
                    return null;
                }
                // Assumption: Replacement doesn't work when a directory is being moved and target directory is non-empty and same name
                Files.move(IOUtils.resolveFilePath(srcPath),
                        Paths.get(IOUtils.resolveFilePath(destFolder).toString(),
                                IOUtils.resolveFilePath(srcPath).getFileName().toString()),
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                // if overwriting is not allowed then only allow possibility of moving if its directory
                if (Files.isDirectory(IOUtils.resolveFilePath(destFolder))) {
                    Files.move(IOUtils.resolveFilePath(srcPath),
                            Paths.get(IOUtils.resolveFilePath(destFolder).toString(), srcPath));

                }
            }
        }
        return null;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        if (args == null) {
            throw new MvException(ERR_NULL_ARGS);
        }

        // Assumption: Will assume multiple args passed when regex is used with only first arg passed
        if (args.length < 2) {
            throw new MvException(ERR_NO_FILE_ARGS);
        }

        MvArgsParser parser = new MvArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw (MvException) new MvException(e.getMessage()).initCause(e);
        }

        shouldOverwrite = parser.shouldOverwrite();
        List<String> sourceOperands = parser.getSourceOperands();
        String targetOperand = parser.getTargetOperand();


        if (sourceOperands.isEmpty() || targetOperand == null) {
            throw new MvException(ERR_NO_FILE_ARGS);
        }

        try {
            if (Files.exists(IOUtils.resolveFilePath(targetOperand))) {
                mvFilesToFolder(targetOperand, sourceOperands.toArray(new String[0]));
            } else {
                mvSrcFileToDestFile(sourceOperands.get(0), targetOperand);
            }
        }  catch (Exception e) {
            throw (MvException) new MvException(ERR_FILE_NOT_FOUND).initCause(e);
        }

    }
}
