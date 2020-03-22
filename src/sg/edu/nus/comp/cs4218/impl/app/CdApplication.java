package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class CdApplication implements CdInterface {



    @Override
    public void changeToDirectory(String path) throws CdException {
        Environment.currentDirectory = getNormalizedAbsolutePath(path);
    }

    /**
     * Runs the cd application with the specified arguments.
     * Assumption: calling cd with no args should have no effect and throw no exception
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws CdException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws CdException {
        if (args == null) {
            throw new CdException(ERR_NULL_ARGS);
        }
        if (stdin == null) {
            throw new CdException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new CdException(ERR_NO_OSTREAM);
        }

        if (args.length == 0) {
            Environment.currentDirectory = System.getProperty("user.home").trim();
            return;
        }

        if (args.length > 1) {
            throw new CdException(ERR_TOO_MANY_ARGS);
        }
        changeToDirectory(args[0]);
    }

    private String getNormalizedAbsolutePath(String pathStr) throws CdException {
        if (StringUtils.isBlank(pathStr)) {
            throw new CdException(ERR_NO_ARGS);
        }

        Path path = new File(pathStr).toPath();
        if (!path.isAbsolute()) {
            path = Paths.get(Environment.currentDirectory, pathStr);
        }

        if (!Files.exists(path)) {
            throw new CdException(String.format("%s: %s", pathStr, ERR_FILE_NOT_FOUND));
        }

        if (!Files.isDirectory(path)) {
            throw new CdException(String.format("%s: %s", pathStr, ERR_IS_NOT_DIR));
        }
        if (!Files.isExecutable(path)) {
            throw new CdException(String.format("%s: %s", pathStr, ERR_NO_PERM));
        }

        return path.normalize().toString();
    }
}
