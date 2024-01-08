package com.oldschoolminecraft.openrtp.debug;

import com.legacyminecraft.poseidon.event.PlayerDeathEvent;
import com.oldschoolminecraft.openrtp.OpenRTP;
import com.oldschoolminecraft.openrtp.debug.CommandTestUnit;
import com.oldschoolminecraft.openrtp.debug.NPCNetHandler;
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
    private final OpenRTP plugin;
    private final boolean debug;

    public PlayerHandler(OpenRTP plugin)
    {
        this.plugin = plugin;
        debug = plugin.getConfig().getBoolean("debug", false);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (!(event.getPlayer().hasPermission("essentials.god") || event.getPlayer().isOp()))
            plugin.tryLoadUser(event.getPlayer().getName()).setGodModeEnabled(false);

        if (!debug) return;
        startTestUnit(event.getPlayer(), "CommandTestUnit");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        try
        {
            if (!debug) return;

            testUnit.continueRunning = false;
            testUnit.interrupt();
            testUnit.join();
            System.out.println("[OpenRTP] Command unit test FAILED, the NPC has died.");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void startTestUnit(Player player, String displayName)
    {
        if (!debug) return;
        if (testUnit != null) return;

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        Player target = Bukkit.getServer().getPlayer(displayName);
        if (target != null) displayName += new Random().nextInt(); // try to make sure name is unused, it's still possible to fail though.

        EntityPlayer npc = new EntityPlayer(server, world, displayName, ((CraftPlayer) player).getHandle().itemInWorldManager);
        npc.netServerHandler = new NPCNetHandler(server, new NPCNetworkManager(), npc);
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
