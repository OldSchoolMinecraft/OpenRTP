package com.oldschoolminecraft.openrtp.debug;

import java.io.IOException;
import java.io.OutputStream;

public class DummyOutputStream extends OutputStream
{
    @Override
    public void write(int b) throws IOException {}
}
