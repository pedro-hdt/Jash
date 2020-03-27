package sg.edu.nus.comp.cs4218.impl.exception;

public class CpException extends  AbstractApplicationException {
    private static final long serialVersionUID = 134525675419999152L;

    public CpException(String message) {
        super("cp: " + message);
    }
}
