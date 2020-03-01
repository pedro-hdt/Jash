package sg.edu.nus.comp.cs4218.exception;

public class CutException extends AbstractApplicationException {

    private static final long serialVersionUID = -5519088977890596239L;

    public CutException(String message) {
        super("cut: " + message);
    }
}
