package sg.edu.nus.comp.cs4218.impl;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_STREAM_CLOSED;

public class ShellImpl implements Shell {

    /**
     * Main method for the Shell Interpreter program.
     *
     * @param args List of strings arguments, unused.
     */
    public static void main(String... args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); // NOPMD
        Shell shell = new ShellImpl();

        while (true) {
            try {
                String currentDirectory = Environment.currentDirectory;
                System.out.write(currentDirectory.getBytes());
                String commandString = "";
                try {
                    commandString = reader.readLine();
                } catch (IOException e) {
                    System.out.printf("%s: %s\n", "Streams are closed, process is terminated:", e.getMessage());
                    break; // Streams are closed, terminate process
                }

                if (!StringUtils.isBlank(commandString)) {
                    shell.parseAndEvaluate(commandString, System.out);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            System.out.println(ERR_STREAM_CLOSED);
        }
    }

    @Override
    public void parseAndEvaluate(String commandString, OutputStream stdout)
            throws AbstractApplicationException, ShellException {
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        command.evaluate(System.in, stdout);
    }
}
