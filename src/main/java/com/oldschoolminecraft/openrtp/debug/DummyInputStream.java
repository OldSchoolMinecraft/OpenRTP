package com.oldschoolminecraft.openrtp.debug;

import java.io.IOException;
import java.io.InputStream;

public class DummyInputStream extends InputStream
{
    @Override
    public int read() throws IOException
    {
        return -1; // end of stream
    }
}
