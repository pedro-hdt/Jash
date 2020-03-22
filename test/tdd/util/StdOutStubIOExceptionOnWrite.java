package tdd.util;

import java.io.IOException;
import java.io.OutputStream;

@SuppressWarnings("PMD")
public class StdOutStubIOExceptionOnWrite extends OutputStream {
    @Override
    public void write(int bytes) throws IOException {
        throw new IOException();
    }

}

