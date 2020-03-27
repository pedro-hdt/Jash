package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CutArgsParser extends ArgsParser {
    private final static char FLAG_IS_CUT_BY_CHAR_POSITION = 'c';
    private final static char FLAG_IS_CUT_BY_BYTE_POSITION = 'b';
    private final static String DASH = "-";
    private final static String COMMA = ",";


    public CutArgsParser() {
        super();
        legalFlags.add(FLAG_IS_CUT_BY_CHAR_POSITION);
        legalFlags.add(FLAG_IS_CUT_BY_BYTE_POSITION);
    }

    public Boolean isCutByCharPos() {
        return flags.contains(FLAG_IS_CUT_BY_CHAR_POSITION);
    }
    public Boolean isCutByBytePos() {
        return flags.contains(FLAG_IS_CUT_BY_BYTE_POSITION);
    }
    public List<String> getFiles() {
        return nonFlagArgs.subList(1, nonFlagArgs.size());
    }
    public Boolean isRange() {
        return nonFlagArgs.get(0).contains(DASH);
    }

    public List<String> getStartAndEndIndex() {
        String indices = nonFlagArgs.get(0);
        if (indices.contains(DASH)) {
            return new ArrayList<String>(Arrays.asList(indices.split(DASH)));
        } else if (indices.contains(COMMA)) {
            return new ArrayList<String>(Arrays.asList(indices.split(COMMA)));
        } else {
            return new ArrayList<String>(Arrays.asList(indices, indices));
        }
    }

    @Override
    protected void validateArgs() throws InvalidArgsException {
        super.validateArgs();
        if (nonFlagArgs.size() == 0) {
            throw new InvalidArgsException(ERR_NO_ARGS);
        }
    }

}
