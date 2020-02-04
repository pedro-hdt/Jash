package sg.edu.nus.comp.cs4218.impl.exception;

public class PasteException extends AbstractApplicationException {

    private static final long serialVersionUID = -4669170442459813673L;

    public PasteException(String message) {
        super("paste: " + message);
    }
}
