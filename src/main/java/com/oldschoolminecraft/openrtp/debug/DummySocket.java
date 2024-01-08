package com.oldschoolminecraft.openrtp.debug;

import org.checkerframework.common.value.qual.IntVal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;

public class DummySocket extends Socket
{
    private static Random random = new Random();

    @Override
    public void connect(SocketAddress endpoint) throws IOException {}

    @Override
    public void close() throws IOException {}

    @Override
    public void shutdownInput() throws IOException {}

    @Override
    public void shutdownOutput() throws IOException {}

    @Override
    public SocketAddress getRemoteSocketAddress()
    {
        return new InetSocketAddress("127.0.0.1", Math.max(49152, random.nextInt(65535)));
    }

    @Override
    public InputStream getInputStream()
    {
        return new DummyInputStream();
    }

    @Override
    public OutputStream getOutputStream()
    {
        return new DummyOutputStream();
    }
}
