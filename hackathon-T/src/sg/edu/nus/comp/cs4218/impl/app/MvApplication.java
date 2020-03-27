package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.StringJoiner;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.PathUtils.convertToAbsolutePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class MvApplication implements MvInterface {
    public static final String NOT_DIRECTORY = "Not a directory";
    public static final String INVALID_PATH = "No such file or directory";
    private boolean canOverwrite = true; //NOPMD

    @Override
    //TODO: abstract functionality into submethods for shorter method length (ms3)
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        try {
            if (args == null) {
                throw new MvException(ERR_NULL_ARGS);
            }

            if (stdout == null) {
                throw new MvException(ERR_NO_OSTREAM);
            }

            MvArgsParser parser = new MvArgsParser();
            String[] srcPaths = null;
            String destPath = null;
            try {
                parser.parse(args);
                srcPaths = parser.getSrcPaths();
                destPath = parser.getDestPath();
            } catch (Exception e) {
                throw new MvException(e);
            }

            this.canOverwrite = parser.canOverwrite();

            File dest = new File(convertToAbsolutePath(destPath));
            String results = "";
            if (dest.exists() && dest.isDirectory()) {
                // If destination exists as a directory, shift as many files/folders in
                results = mvFilesToFolder(destPath, srcPaths);
            } else {
                // Else renaming one file
                if (srcPaths.length == 1) {
                    // If one source file, rename
                    results = mvSrcFileToDestFile(srcPaths[0], destPath);
                } else {
                    // Unable to rename more than one file
                    throw new Exception(String.format("%s: %s", destPath, ERR_IS_NOT_DIR));
                }
            }

            if (results == null) {
                // No write to output if no results
                return;
            }

            String trimmedResults = results.trim();
            if (!trimmedResults.isEmpty()) {
                results += STRING_NEWLINE;
            }

            try {
                stdout.write(results.getBytes());
            } catch (IOException e) {
                throw new Exception(ERR_WRITE_STREAM); //NOPMD
            }
        } catch (MvException mve) {
            throw mve;
        } catch (Exception e) {
            throw new MvException(e);
        }
    }

    /**
     * Move multiple files into destination folder.
     *
     * @param destFolder of path to destination folder
     * @param fileName   Array of String of file names
     * @return
     * @throws Exception
     */
    @Override
    public String mvFilesToFolder(String destFolder, String... fileName) throws Exception {
        boolean requiresOutput = false;
        StringJoiner stringJoiner = new StringJoiner(STRING_NEWLINE);

        File dest = new File(convertToAbsolutePath(destFolder));
        if (!dest.exists() || !dest.isDirectory()) {
            throw new Exception(NOT_DIRECTORY);
        }

        for (String src : fileName) {
            try {
                String result = executeMoveOperation(src, destFolder, false);
                if (result != null) {
                    stringJoiner.add(result);
                    requiresOutput = true;
                }
            } catch (Exception e) {
                stringJoiner.add(e.getMessage());
                requiresOutput = true;
            }
        }

        return requiresOutput ? stringJoiner.toString() : null;
    }


    /**
     * Renames the file named by the source operand to the destination path named by the target operand
     *
     * @param srcFile  of path to source file
     * @param destFile of path to destination file
     * @return
     * @throws Exception
     */
    @Override
    public String mvSrcFileToDestFile(String srcFile, String destFile) throws Exception {
        try {
            return executeMoveOperation(srcFile, destFile, true);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Executes the move operation required
     *
     * @param src         path of file to move
     * @param dest        path of file to move to
     * @param isSingleSrc whether dealing with single source or multiple sources
     * @return
     * @throws Exception
     */
    private String executeMoveOperation(String src, String dest, boolean isSingleSrc) throws Exception {
        String absSrc = convertToAbsolutePath(src);
        File srcFile = new File(absSrc);
        String absDest = convertToAbsolutePath(dest);
        File destFile = new File(absDest);

        if (!srcFile.exists()) {
            String errorDestPath = destFile.exists() && destFile.isDirectory() ?
                    dest + StringUtils.fileSeparator() + src : dest;
            throw new MvException(src, errorDestPath, INVALID_PATH);
        }

        boolean isInvalidMove = destFile.exists() && !destFile.isDirectory() && srcFile.isDirectory();
        if (isInvalidMove) {
            throw new MvException(src, dest, NOT_DIRECTORY);
        }

        Path srcPath = Paths.get(absSrc);
        Path destPath = isSingleSrc ? srcPath.resolveSibling(absDest)
                : Paths.get(absDest).resolve(srcPath.getFileName());

        if (srcFile.equals(destFile)) {
            throw new MvException(String.format("\'%s\' and \'%s\' are the same file", src, src));
        }

        File newDest = destPath.toFile();
        boolean invalidOverwrite = !canOverwrite && newDest.exists();
        if (invalidOverwrite) {
            return null;
        }

        try {
            Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new MvException(src, dest, e);
        }

        return null;
    }
}
