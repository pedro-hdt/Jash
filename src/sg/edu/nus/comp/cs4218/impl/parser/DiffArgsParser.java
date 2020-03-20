package sg.edu.nus.comp.cs4218.impl.parser;

public class DiffArgsParser extends ArgsParser {
    private final static char FLAG_SHOW_SAME = 's';
    private final static char FLAG_NO_BLANK = 'B';
    private final static char FLAG_IS_SIMPLE = 'q';

    public DiffArgsParser() {
        super();
        legalFlags.add(FLAG_SHOW_SAME);
        legalFlags.add(FLAG_NO_BLANK);
        legalFlags.add(FLAG_IS_SIMPLE);
    }

    public Boolean isShowSame() { return flags.contains(FLAG_SHOW_SAME); }

    public Boolean isNoBlank() { return flags.contains(FLAG_NO_BLANK); }

    public Boolean isSimple() { return flags.contains(FLAG_IS_SIMPLE); }

    public String[] getFiles() {
        return nonFlagArgs.toArray(new String[0]);
    }
}
