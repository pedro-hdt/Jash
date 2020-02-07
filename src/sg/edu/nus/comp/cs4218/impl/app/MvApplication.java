package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
        System.out.println(IOUtils.resolveFilePath(destFolder));
        for (String srcPath: fileName){
            if (shouldOverwrite) {
                Files.move(IOUtils.resolveFilePath(srcPath),
                        IOUtils.resolveFilePath(destFolder + File.separator + srcPath),
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(IOUtils.resolveFilePath(srcPath),
                        IOUtils.resolveFilePath(destFolder + File.separator + srcPath));
            }
        }
        return null;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        if (args == null) {
            throw new MvException(ERR_NULL_ARGS);
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


        if (sourceOperands.size() == 0 || targetOperand == null) {
            throw new MvException(ERR_NO_FILE_ARGS);
        }

        if (Files.exists(IOUtils.resolveFilePath(targetOperand))) {
            try {
                mvFilesToFolder(targetOperand, sourceOperands.toArray(new String[sourceOperands.size()]));
            } catch (Exception e) {
                throw (MvException) new MvException(ERR_FILE_NOT_FOUND).initCause(e);
            }
        } else {
            try {
                mvSrcFileToDestFile(sourceOperands.get(0), targetOperand);
            } catch (Exception e) {
                throw (MvException) new MvException(ERR_FILE_NOT_FOUND).initCause(e);
            }
        }


    }
}
