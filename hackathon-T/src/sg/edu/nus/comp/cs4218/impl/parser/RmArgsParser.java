package sg.edu.nus.comp.cs4218.impl.parser;

public class RmArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE = 'r';
    private final static char FLAG_IS_DIRECTORY = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_DIRECTORY);
    }

    public Boolean isDirectoriesOnly() {
        return flags.contains(FLAG_IS_DIRECTORY);
    }

    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public String[] getFilesOrDirectoriesToDelete() {
        String[] itemsToDeleteArr = new String[nonFlagArgs.size()];
        itemsToDeleteArr = nonFlagArgs.toArray(itemsToDeleteArr);
        return itemsToDeleteArr;
    }
}
