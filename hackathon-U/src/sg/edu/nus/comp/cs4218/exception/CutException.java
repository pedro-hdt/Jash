package sg.edu.nus.comp.cs4218.exception;

public class CutException extends  AbstractApplicationException {
    private static final long serialVersionUID = 198347585419999152L;

    public CutException(String message) {
        super("cut: " + message);
    }
}
