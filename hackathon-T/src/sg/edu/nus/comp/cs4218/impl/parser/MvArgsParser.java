package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.LinkedList;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;

@SuppressWarnings("PMD.LongVariable")
public class MvArgsParser extends ArgsParser {
    private final static char IS_OVERWRITE_DISABLED = 'n';
    private boolean nonFlagArgsParsed;
    private String[] srcPaths;
    private String destPath;

    public MvArgsParser() {
        super();
        legalFlags.add(IS_OVERWRITE_DISABLED);
        this.nonFlagArgsParsed = false;
    }

    public Boolean canOverwrite() {
        return !flags.contains(IS_OVERWRITE_DISABLED);
    }

    private void parseNonFlagArgs() throws InvalidArgsException {
        if (nonFlagArgsParsed) {
            return;
        }

        if (nonFlagArgs.size() < 2) {
            throw new InvalidArgsException(ERR_NO_ARGS);
        }
        LinkedList<String> args = new LinkedList<>(nonFlagArgs);
        this.destPath = args.removeLast();
        this.srcPaths = args.toArray(new String[0]);
        this.nonFlagArgsParsed = true;
    }

    public String[] getSrcPaths() throws InvalidArgsException {
        try {
            parseNonFlagArgs();
        } catch (InvalidArgsException e) {
            throw e;
        }
        return this.srcPaths;
    }

    public String getDestPath() throws InvalidArgsException {
        try {
            parseNonFlagArgs();
        } catch (InvalidArgsException e) {
            throw e;
        }
        return this.destPath;
    }
}
