package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class RmArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE = 'r';
    private final static char FLAG_IS_FOLDERS = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_FOLDERS);
        legalFlags.add(FLAG_IS_RECURSIVE);
    }

    public Boolean isFoldersOnly() {
        return flags.contains(FLAG_IS_FOLDERS);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
