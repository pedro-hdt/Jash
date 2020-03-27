package sg.edu.nus.comp.cs4218.exception;

import java.io.IOException;

public class CutException extends AbstractApplicationException {
    private static final long serialVersionUID = -544736942414546043L;
    public static String CUT_PREFIX = "cut: ";

    public CutException(String message) {
        super(CUT_PREFIX + message);
    }

    public CutException(IOException exception) {
        super("cut: " + exception.getMessage());
    }
}
