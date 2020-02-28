package sg.edu.nus.comp.cs4218.exception;

public class CpException extends AbstractApplicationException {
    private static final long serialVersionUID = -7709372556191948215L;

    public CpException(String message) {
        super("cp: " + message);
    }
}
