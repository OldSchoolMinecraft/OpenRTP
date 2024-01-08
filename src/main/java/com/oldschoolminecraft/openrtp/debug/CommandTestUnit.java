package com.oldschoolminecraft.openrtp.debug;

import com.oldschoolminecraft.openrtp.util.CSVWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.ArrayList;

public class CommandTestUnit extends Thread
{
    public volatile boolean continueRunning = true;

    private final CraftPlayer npc;
    private final int testIterations;
    private final ArrayList<String[]> unitTestResults = new ArrayList<>(); // Location - Iteration - NPC Health

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
                if (!continueRunning) break;
                Bukkit.dispatchCommand(npc, "wild"); // force the test unit NPC to run the command
                while (npc.getLocation() == lastLoc); // wait for location to change
                Thread.sleep(10000); // wait 10 seconds, to ensure accurate health data about the player, if they end up teleporting to a dangerous place.
                unitTestResults.add(new String[] { npc.getLocation().toString(), String.valueOf(i), String.valueOf(npc.getHealth()) }); // add the result to the list
                lastLoc = npc.getLocation(); // update last location
                System.out.println("[OpenRTP] Test unit completed iteration: " + i);
            }
            System.out.println("[OpenRTP] The command test unit has completed all " + testIterations + " iterations. The results will now be written to disk.");
            CSVWriter.writeResultsToCSV(unitTestResults, new String[] { "Location", "Iteration", "HP" }, "plugins/OpenRTP/debug_results.csv");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
