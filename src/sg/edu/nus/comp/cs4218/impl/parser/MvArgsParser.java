package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class MvArgsParser extends ArgsParser {
    private final static char FLAG_NO_OVERWRITE = 'n';
    
    public MvArgsParser() {
        super();
        legalFlags.add(FLAG_NO_OVERWRITE);
    }
    
    public boolean shouldOverwrite() {
        return !flags.contains(FLAG_NO_OVERWRITE);
    }
    
    public String getTargetOperand() {
        return nonFlagArgs.get(nonFlagArgs.size() - 1);
    }
    
    public List<String> getSourceOperands() {
        return nonFlagArgs.subList(0, nonFlagArgs.size() - 1);
    }
}
