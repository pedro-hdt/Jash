package sg.edu.nus.comp.cs4218.exception;

public class MvException extends AbstractApplicationException {

    private static final long serialVersionUID = -731736942454546043L;

    public MvException(String message) {
        super("mv: " + message);
    }

    public MvException(Exception exception) {
        super("mv: " + exception.getMessage());
    }

    public MvException(String src, String dest, Exception exception) {
        super(String.format("mv: rename %s to %s: %s", src, dest, exception.getMessage()));
    }

    public MvException(String src, String dest, String message) {
        super(String.format("mv: rename %s to %s: %s", src, dest, message));
    }
}
