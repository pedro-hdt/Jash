package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class RmApplication implements RmInterface {

    public static final String ERR_DOT_DIR = "refusing to remove '.' or '..' directory";

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws RmException {

        boolean hasFailedFiles = false;
        StringBuilder sb = new StringBuilder();//NOPMD

        for (String f : fileName) {

            // prevent removing directories ending in . or ..
            if (".".equals(f) || "..".equals(f)
                    || (f.length() > 1 && f.substring(f.length() - 2).equals(StringUtils.fileSeparator() + "."))
                    || (f.length() > 2 && f.substring(f.length() - 3).equals(StringUtils.fileSeparator() + ".."))) {
                sb.append(f);
                sb.append(" skipped: ");
                sb.append(ERR_DOT_DIR);
                sb.append(STRING_NEWLINE);
                hasFailedFiles = true;
                continue;
            }

            File file = IOUtils.resolveFilePath(f).toFile();

            if (!file.exists()) {
                sb.append(f);
                sb.append(" skipped: ");
                sb.append(ERR_FILE_NOT_FOUND);
                sb.append(STRING_NEWLINE);
                hasFailedFiles = true; // signal this file does not exist to report it later then skip it
                continue;
            }

            if (file.isDirectory()) {

                if (file.list().length == 0) {
                    if (!isEmptyFolder && !isRecursive) {
                        sb.append(f);
                        sb.append(" skipped: ");
                        sb.append(ERR_IS_DIR);
                        sb.append(STRING_NEWLINE);
                        hasFailedFiles = true;
                        continue;
                    }
                } else {

                    String[] contents = file.list();

                    if (isRecursive) { // if recursive and not empty go ahead
                        Environment.currentDirectory = file.getAbsolutePath(); // go into the directory
                        remove(isEmptyFolder, true, contents); // remove recursively
                        Environment.currentDirectory = file.getParent(); // come out
                    } else {
                        // if not recursive, then rm fails
                        throw new RmException(String.format("cannot remove %s: %s", file.toString(), ERR_IS_DIR));
                    }

                }

            }

            file.delete();

        }

        if (hasFailedFiles) {
            throw new RmException(STRING_NEWLINE + sb.toString().trim());
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
            throw (RmException)new RmException(e.getMessage()).initCause(e);
        }

        Boolean recursive = parser.isRecursive();
        Boolean emptyFolder = parser.isEmptyFolder();
        List<String> fileList = parser.getFileList();

        if (fileList.isEmpty()) {
            throw new RmException(ERR_NO_FILE_ARGS);
        }

        String[] files = fileList.toArray(new String[0]);

        remove(emptyFolder, recursive, files);

    }
}
