package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.ExitInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.io.InputStream;
import java.io.OutputStream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

public class ExitApplication implements ExitInterface {

    /**
     * Runs the exit application.
     *
     * @param args   Array of arguments for the application, not used.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws ExitException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws ExitException {
        // Format: exit
        if (args != null && args.length != 0) {
            // ExitException is for successful exit only
            throw new ExitException(ERR_TOO_MANY_ARGS);
        }
        terminateExecution();
    }

    /**
     * Terminate shell.
     *
     * @throws Exception
     */
    @Override
    public void terminateExecution() throws ExitException {
        throw new ExitException("Shell terminated gracefully.");
    }
}
