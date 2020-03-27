package sg.edu.nus.comp.cs4218.exception;

public class MvException extends AbstractApplicationException {

    private static final long serialVersionUID = 5001123456291923161L;

    public MvException(String message) {
        super("mv: " + message);
    }

}