package com.oldschoolminecraft.openrtp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class CommandTestUnit extends Thread
{
    private final CraftPlayer npc;
    private final int testIterations;

    public CommandTestUnit(CraftPlayer npc, int testIterations)
    {
        this.npc = npc;
        this.testIterations = testIterations;
    }

    public void start()
    {
        super.start();
        System.out.println("[OpenRTP] A new test unit for the /wild command has been started with " + testIterations + " iterations");
    }

    public void run()
    {
        try
        {
            Location lastLoc = npc.getLocation();
            for (int i = 0; i < testIterations; i++)
            {
                Bukkit.dispatchCommand(npc, "/wild"); // force the test unit NPC to run the command
                while (npc.getLocation() == lastLoc); // wait for location to change

            }
        } catch (Exception ignored) {}
    }
}
