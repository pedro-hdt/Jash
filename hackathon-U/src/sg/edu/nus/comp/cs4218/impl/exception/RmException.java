package sg.edu.nus.comp.cs4218.impl.exception;

public class RmException extends  AbstractApplicationException {
    private static final long serialVersionUID = 993331242467089104L;

    public RmException(String message) {
        super("rm: " + message);
    }
}
