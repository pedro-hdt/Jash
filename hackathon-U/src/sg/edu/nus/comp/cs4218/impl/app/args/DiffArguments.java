package sg.edu.nus.comp.cs4218.impl.app.args;

import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;

public class DiffArguments {

    public static final char CHAR_SAME_FILES_MSG = 's';
    public static final char CHAR_BLANK_LINES_IGNORE = 'B';
    public static final char CHAR_DIFF_FILES_MSG = 'q';

    private List<String> files;
    private List<String> dirs;
    private boolean ignoreBlankLines, outputSameFilesMessage, outputDifferentFilesMessage;

    public DiffArguments() {
        this.files = new ArrayList<>();
        this.dirs = new ArrayList<>();
        ignoreBlankLines = false;
        outputDifferentFilesMessage = false;
        outputSameFilesMessage = false;
    }

    /**
     * Handles argument list parsing for the `diff` application.
     *
     * @param args Array of arguments to parse
     * @throws Exception
     */
    public void parse(String... args) throws Exception {
        if (args == null) {
            throw new Exception(ERR_NULL_ARGS);
        }

        for (String arg : args) {
            if (arg.isEmpty()) {
                continue;
            }
            if (arg.charAt(0) == CHAR_FLAG_PREFIX) {
                if (arg.equals(CHAR_FLAG_PREFIX + "" + CHAR_SAME_FILES_MSG)) {
                    this.outputSameFilesMessage = true;
                } else if (arg.equals(CHAR_FLAG_PREFIX + "" + CHAR_BLANK_LINES_IGNORE)) {
                    this.ignoreBlankLines = true;
                } else if (arg.equals(CHAR_FLAG_PREFIX + "" + CHAR_DIFF_FILES_MSG)) {
                    this.outputDifferentFilesMessage = true;
                } else {
                    // Read standard input if we reach this point
                    this.files.add(arg);
                }
            } else {
                if (arg.contains(".")) {
                    this.files.add(arg);
                } else {
                    this.dirs.add(arg);
                }
            }
        }
    }

    public List<String> getFiles() {
        return files;
    }

    public List<String> getDirectories() {
        return dirs;
    }

    public boolean isIgnoreBlankLines() {
        return ignoreBlankLines;
    }

    public boolean isOutputDifferentFilesMessage() {
        return outputDifferentFilesMessage;
    }

    public boolean isOutputSameFilesMessage() {
        return outputSameFilesMessage;
    }
}

