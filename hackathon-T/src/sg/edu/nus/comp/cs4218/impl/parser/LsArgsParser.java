package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;

public class LsArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE = 'R';
    private final static char FLAG_IS_FOLDERS = 'd';

    public LsArgsParser() {
        super();
        legalFlags.add(FLAG_IS_FOLDERS);
        legalFlags.add(FLAG_IS_RECURSIVE);
    }

    @Override
    public void parse(String... args) throws InvalidArgsException {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            boolean isEnclosingArg = i == 0 || i == args.length - 1;
            if (isEnclosingArg && arg.length() > 1 && arg.charAt(0) == CHAR_FLAG_PREFIX) {
                boolean containsValidFlag = false;
                for (int charIndex = 1; charIndex < arg.length(); charIndex++) {
                    if (legalFlags.contains(arg.charAt(charIndex))) {
                        containsValidFlag = true;
                        flags.add(arg.charAt(charIndex));
                    }
                }

                if (!containsValidFlag) {
                    throw new InvalidArgsException(ILLEGAL_FLAG_MSG + arg.charAt(1));
                }
            } else {
                nonFlagArgs.add(arg);
            }
        }
        validateArgs();
    }

    public Boolean isFoldersOnly() {
        return flags.contains(FLAG_IS_FOLDERS);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}
