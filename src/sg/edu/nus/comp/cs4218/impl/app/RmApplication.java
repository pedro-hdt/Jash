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
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class RmApplication implements RmInterface {

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws RmException {

        // For clarification 'rm -d' will delete directories iff they are empty
        // 'rm -r' will recursively delete stuff so no matter if it is empty
        // Therefore, 'rm -r -d' == 'rm -r' @assumption

        for (String f : fileName) {

            File file = IOUtils.resolveFilePath(f).toFile();

            // if any of the arguments is invalid, the operation fails at that point without recovery @assumption
            if (!file.exists())
                throw new RmException(ERR_FILE_NOT_FOUND);

            if (file.isDirectory()) {

                if (isRecursive) { // if it is recursive we don't care
                    Environment.currentDirectory = file.getAbsolutePath(); // go into the directory
                    remove(isEmptyFolder, true, file.list()); // remove recursively
                    Environment.currentDirectory = file.getParent(); // come out
                } else if (!isEmptyFolder || file.list().length != 0) { // otherwise check if directory is empty
                    throw new RmException(String.format("cannot remove %s: %s", file.toString(), ERR_IS_DIR));
                }

            }

            file.delete();

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
            throw new RmException(e.getMessage());
        }

        Boolean recursive = parser.isRecursive();
        Boolean emptyFolder = parser.isEmptyFolder();
        List<String> fileList = parser.getFileList();

        if (fileList.isEmpty())
            throw new RmException(ERR_NO_FILE_ARGS);

        String[] files = fileList.toArray(new String[parser.getFileList().size()]);

        remove(emptyFolder, recursive, files);

    }
}
