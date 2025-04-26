package com.oldschoolminecraft.openrtp;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

import java.util.Random;

public class PlayerHandler extends PlayerListener
{
    private CommandTestUnit testUnit;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        startTestUnit(event.getPlayer(), "CommandTestUnit");
    }

    public void startTestUnit(Player player, String displayName)
    {
        if (testUnit != null && testUnit.getState() != Thread.State.TERMINATED) return;

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        Player target = Bukkit.getServer().getPlayer(displayName);
        if (target != null) displayName += new Random().nextInt(); // try to make sure name is unused, it's still possible to fail though.

        EntityPlayer npc = new EntityPlayer(server, world, displayName, ((CraftPlayer) player).getHandle().itemInWorldManager);
        Location loc = player.getLocation();
        npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        for (Player all : Bukkit.getOnlinePlayers())
        {
            Packet20NamedEntitySpawn packet = new Packet20NamedEntitySpawn(npc);
            ((CraftPlayer)all).getHandle().netServerHandler.sendPacket(packet);
        }

        CraftPlayer craftPlayer = new CraftPlayer(((CraftServer) Bukkit.getServer()), npc);

        testUnit = new CommandTestUnit(craftPlayer, 1000);
        testUnit.start();
    }
}
