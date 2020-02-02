package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class RmArgsParser extends ArgsParser {

    private final static char FLAG_IS_RECURSIVE = 'r';
    private final static char FLAG_IS_EMPTYFOLDER = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_EMPTYFOLDER);
    }

    public Boolean isEmptyFolder() {
        return flags.contains(FLAG_IS_EMPTYFOLDER);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public List<String> getFileList() {
        return nonFlagArgs;
    }

}
