package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class PasteArgsParser extends ArgsParser {
    public PasteArgsParser() {
        super();
    }

    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
