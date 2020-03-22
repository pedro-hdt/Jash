package sg.edu.nus.comp.cs4218.impl.parser;

public class CutArgsParser extends ArgsParser {
    private final static char CUT_BY_CHAR_POS = 'c';
    private final static char CUT_BY_BYTE_POS = 'b';
    
    public CutArgsParser() {
        super();
        legalFlags.add(CUT_BY_CHAR_POS);
        legalFlags.add(CUT_BY_BYTE_POS);
    }
    
    public Boolean isCutByCharPos() {
        return flags.contains(CUT_BY_CHAR_POS);
    }
    
    public Boolean isCutByBytePos() {
        return flags.contains(CUT_BY_BYTE_POS);
    }
    
    public String getList() {
        return nonFlagArgs.get(0); // can be a list of comma separated numbers, a range of numbers or a single number
    }
    
    public String[] getFiles() {
        return nonFlagArgs.subList(1, nonFlagArgs.size()).stream().toArray(String[]::new);
    }
}
