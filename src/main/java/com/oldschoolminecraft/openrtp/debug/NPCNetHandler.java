package com.oldschoolminecraft.openrtp.debug;

import net.minecraft.server.*;

public class NPCNetHandler extends NetServerHandler
{
    public NPCNetHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer)
    {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void a(Packet packet)
    {
        // drop everything
    }

    @Override
    public boolean c()
    {
        return true; // no idea what this flag is for, but it's true!
    }
}
