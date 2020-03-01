package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class RmApplication implements RmInterface {

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws RmException {

        List<String> missingFiles = new ArrayList<>();

        for (String f : fileName) {

            File file = IOUtils.resolveFilePath(f).toFile();

            if (!file.exists()) {
                missingFiles.add(f); // signal this file does not exist to report it later then skip it
                continue;
            }

            if (file.isDirectory()) {

                String[] contents = file.list();

                if (isRecursive && contents.length != 0) { // if recursive and not empty go ahead
                    Environment.currentDirectory = file.getAbsolutePath(); // go into the directory
                    remove(isEmptyFolder, true, contents);       // remove recursively
                    Environment.currentDirectory = file.getParent();       // come out
                } else if (!isRecursive && (contents.length != 0 || !isEmptyFolder)) {
                    // if not recursive, then the only way this could possibly work:
                    // -d was specified, and the directory is empty
                    throw new RmException(String.format("cannot remove %s: %s", file.toString(), ERR_IS_DIR));
                }

            }

            file.delete();

        }

        if (!missingFiles.isEmpty()) {
            StringBuilder sb = new StringBuilder();//NOPMD
            for (String f : missingFiles) {
                sb.append(f);
                sb.append(" skipped: ");
                sb.append(ERR_FILE_NOT_FOUND);
                sb.append(STRING_NEWLINE);
            }
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
