package sg.edu.nus.comp.cs4218.impl.app.args;

import sg.edu.nus.comp.cs4218.exception.DiffException;

import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;

public class DiffArguments {
    public static final char CHAR_SIMPLE_OPTION = 'q';
    public static final char CHAR_BLANK_OPTION = 'B';
    public static final char CHAR_SHOW_SAME_OPTION = 's';

    private final List<String> files;
    private boolean isSimple = false;
    private boolean isBlank = false;
    private boolean isShowSame = false;

    public DiffArguments() {
        this.files = new ArrayList<>();
    }

    public void parse(String... args) throws DiffException {
        boolean isParsingFlag = true;
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg.isEmpty()) {
                    continue;
                }
                if (isParsingFlag && arg.charAt(0) == CHAR_FLAG_PREFIX && arg.length() > 1) {
                    for (char c : arg.toCharArray()) {
                        if (c == CHAR_FLAG_PREFIX) {
                            continue;
                        }
                        if (c == CHAR_BLANK_OPTION) {
                            isBlank = true;
                            continue;
                        }
                        if (c == CHAR_SHOW_SAME_OPTION) {
                            isShowSame = true;
                            continue;
                        }
                        if (c == CHAR_SIMPLE_OPTION) {
                            isSimple = true;
                            continue;
                        }
                        throw new DiffException(ERR_INVALID_FLAG);
                    }
                } else {
                    isParsingFlag = false;
                    this.files.add(arg.trim());
                }
            }
        }
    }

    public boolean checkIfSimple() {
        return isSimple;
    }

    public boolean checkIfBlank() {
        return isBlank;
    }

    public boolean checkIfShowSame() {
        return isShowSame;
    }

    public List<String> getFiles() {
        return files;
    }

}
