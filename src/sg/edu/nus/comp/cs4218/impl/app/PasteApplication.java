package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.InputStream;
import java.io.OutputStream;

public class PasteApplication implements PasteInterface { // TODO implement me
    @Override
    public String mergeStdin(InputStream stdin) throws Exception {
        return null;
    }

    @Override
    public String mergeFile(String... fileName) throws Exception {
        return null;
    }

    @Override
    public String mergeFileAndStdin(InputStream stdin, String... fileName) throws Exception {
        return null;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {

    }
}
