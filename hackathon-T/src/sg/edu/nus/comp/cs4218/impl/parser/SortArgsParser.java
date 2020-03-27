package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

@SuppressWarnings("PMD.LongVariable")
public class SortArgsParser extends ArgsParser {
    private final static char FLAG_IS_FIRST_WORD_NUMBER = 'n';
    private final static char FLAG_IS_REVERSE_ORDER = 'r';
    private final static char FLAG_IS_CASE_INDEPENDENT = 'f';

    public SortArgsParser() {
        super();
        legalFlags.add(FLAG_IS_FIRST_WORD_NUMBER);
        legalFlags.add(FLAG_IS_REVERSE_ORDER);
        legalFlags.add(FLAG_IS_CASE_INDEPENDENT);
    }

    public Boolean isFirstWordNumber() {
        return flags.contains(FLAG_IS_FIRST_WORD_NUMBER);
    }

    public Boolean isReverseOrder() {
        return flags.contains(FLAG_IS_REVERSE_ORDER);
    }

    public Boolean isCaseIndependent() {
        return flags.contains(FLAG_IS_CASE_INDEPENDENT);
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
