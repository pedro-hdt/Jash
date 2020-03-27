package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Application;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

/**
 * A Call Command is a sub-command consisting of at least one non-keyword or quoted.
 * <p>
 * Command format: (<non-keyword> or <quoted>) *
 */
public class CallCommand implements Command {
    private final List<String> argsList;
    private final ApplicationRunner appRunner;
    private final ArgumentResolver argumentResolver;
    private final String APP_REGEX = "^[a-zA-Z0-9_]*$";

    public CallCommand(List<String> argsList, ApplicationRunner appRunner, ArgumentResolver argumentResolver) throws ShellException {
        if (argsList == null || appRunner == null || argumentResolver == null) {
            throw new ShellException(ERR_SYNTAX);
        }
        if (!argsList.isEmpty() && !argsList.get(0).matches(APP_REGEX)) {
            throw new ShellException(ERR_INVALID_APP);
        }
        this.argsList = new ArrayList<String>(argsList);
        this.appRunner = appRunner;
        this.argumentResolver = argumentResolver;
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException {
        if (argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }
        // Handle IO redirection
        IORedirectionHandler redirHandler = new IORedirectionHandler(argsList, stdin, stdout, argumentResolver);
        redirHandler.extractRedirOptions();
        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        InputStream inputStream = redirHandler.getInputStream();
        OutputStream outputStream = redirHandler.getOutputStream();
        // Handle quoting + globing + command substitution
        List<String> parsedArgsList = argumentResolver.parseArguments(noRedirArgsList);
        if (!parsedArgsList.isEmpty()) {
            String app = parsedArgsList.remove(0);
            appRunner.runApp(app, parsedArgsList.toArray(new String[0]), inputStream, outputStream);
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<String> getArgsList() {
        return argsList;
    }
}
