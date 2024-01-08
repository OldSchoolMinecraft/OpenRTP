package com.oldschoolminecraft.openrtp.debug;

import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

public class NPCNetworkManager extends NetworkManager
{
    public NPCNetworkManager()
    {
        super(new DummySocket(), "NPCNetworkManager", null);
    }

    @Override
    public void a(String s, Object... objects)
    {
        // disconnect, i think
    }

    @Override
    public void b() {}

    @Override
    public void d() {}

    @Override
    public void queue(Packet packet) {}

    @Override
    public int e()
    {
        return 0;
    }
}
