package sg.edu.nus.comp.cs4218.impl.app.args;

import sg.edu.nus.comp.cs4218.exception.MvException;

import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;

public class MvArguments {
    public static final char CHAR_NOT_OVERWRITE_OPTION = 'n';
    private final List<String> files;

    private boolean notOverwrite;
    public MvArguments() {
        this.notOverwrite = false;
        this.files = new ArrayList<>();
    }

    /**
     * Handles argument list parsing for the `mv` application.
     *
     * @param args Array of arguments to parse
     * @throws Exception
     */
    public void parse(String... args) throws MvException {
        boolean parsingFlag = true;
        // Parse arguments
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg.isEmpty()) {
                    continue;
                }
                // `parsingFlag` is to ensure all flags come first, followed by files.
                if (parsingFlag && arg.charAt(0) == CHAR_FLAG_PREFIX) {
                    // Loop through to see if we have any invalid flags
                    for (char c : arg.toCharArray()) {
                        if (c == CHAR_FLAG_PREFIX || c == CHAR_NOT_OVERWRITE_OPTION) {
                            continue;
                        } else {
                            throw new MvException(String.format(ERR_INVALID_FLAG, arg));
                        }
                    }

                    for (char c : arg.toCharArray()) {
                        if (c == CHAR_FLAG_PREFIX) {
                            continue;
                        }
                        if (c == CHAR_NOT_OVERWRITE_OPTION) {
                            this.notOverwrite = true;
                        }
                    }
                } else {
                    parsingFlag = false;
                    this.files.add(arg.trim());
                }
            }
        }
    }

    public List<String> getFiles() {
        return files;
    }

    public boolean doesNotOverWrite() {
        return this.notOverwrite;
    }
}
