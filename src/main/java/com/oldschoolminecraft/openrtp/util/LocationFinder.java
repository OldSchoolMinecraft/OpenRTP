package com.oldschoolminecraft.openrtp.util;

import com.oldschoolminecraft.openrtp.RTPConfig;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Random;

public class LocationFinder extends Thread
{
    private final Random rng = new Random();

    private final World world;
    private final LocationCallback callback;

    private final double range_min, range_max;

    private Location location;
    private ArrayList<SafetyCheck> safetyChecks = new ArrayList<>();

    public LocationFinder(RTPConfig config, World world, LocationCallback callback)
    {
        this.world = world;
        this.callback = callback;
        range_min = config.getConfigDouble("range_min");
        range_max = config.getConfigDouble("range_max");

        // check if they are on top of solid ground
        safetyChecks.add((loc) -> !isBlockEmpty(loc.clone().subtract(0, 1, 0)));
        // check if there's fire
        safetyChecks.add((loc) -> loc.getBlock().getType() != Material.FIRE && loc.clone().add(0, 1, 0).getBlock().getType() != Material.FIRE);
        // check to see if the surrounding columns are also empty, to ensure they don't clip into blocks
        safetyChecks.add((loc) ->
                        canPlayerFit(loc) &&
                        canPlayerFit(loc.clone().add(1, 0, 0)) &&
                        canPlayerFit(loc.clone().subtract(1, 0, 0)) &&
                        canPlayerFit(loc.clone().add(0, 0, 1)) &&
                        canPlayerFit(loc.clone().subtract(0, 0, 1)) &&
                        canPlayerFit(loc.clone().add(1, 0, 1)) &&
                        canPlayerFit(loc.clone().subtract(1, 0, 1)));
        // check if location has lava at, above, or below
        safetyChecks.add((loc) -> loc.clone().add(0, 1, 0).getBlock().getType() != Material.LAVA &&
                loc.clone().add(0, 1, 0).getBlock().getType() != Material.STATIONARY_LAVA &&
                loc.clone().subtract(0, 1, 0).getBlock().getType() != Material.LAVA &&
                loc.clone().subtract(0, 1, 0).getBlock().getType() != Material.STATIONARY_LAVA &&
                loc.getBlock().getType() != Material.LAVA &&
                loc.getBlock().getType() != Material.STATIONARY_LAVA);
        // check if location has water at, above, or below
        safetyChecks.add((loc) -> loc.clone().add(0, 1, 0).getBlock().getType() != Material.WATER &&
                loc.clone().add(0, 1, 0).getBlock().getType() != Material.STATIONARY_WATER &&
                loc.clone().subtract(0, 1, 0).getBlock().getType() != Material.WATER &&
                loc.clone().subtract(0, 1, 0).getBlock().getType() != Material.STATIONARY_WATER &&
                loc.getBlock().getType() != Material.WATER &&
                loc.getBlock().getType() != Material.STATIONARY_WATER);

        System.out.println("[OpenRTP] LocationFinder created");
    }

    public void run()
    {
        Location loc = getRandomizedLocation(world, range_min, range_max);
        boolean safe;
        while (!(safe = runSafetyChecks(loc)))
        {
            loc = getRandomizedLocation(world, range_min, range_max);
        }

        Chunk chunk = loc.getWorld().getChunkAt(loc);
        location.getWorld().loadChunk(chunk);

        // wait to load
        while (!chunk.isLoaded()) {}

        callback.pipe(loc);
    }

    private boolean runSafetyChecks(Location location)
    {
        int checksPassed = 0;
        for (SafetyCheck check : safetyChecks) if (check.isSafe(location)) checksPassed++;
        return checksPassed == safetyChecks.size();
    }

    private boolean canPlayerFit(Location loc)
    {
        return isBlockEmpty(loc) && isBlockEmpty(loc.clone().add(0, 1, 0));
    }

    private boolean isSafeLocation(Location location)
    {
        //        System.out.println("Is location SAFE? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return isBlockEmpty(location)
                && onSolidGround(location)
                && !hasFire(location)
                && !hasWater(location)
                && !hasLava(location)
                && !insideSolidBlocks(location);
    }

    private boolean insideSolidBlocks(Location location)
    {
        //        System.out.println("Location inside solid block? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return !isBlockEmpty(location) || !isBlockEmpty(location.clone().add(0, 1, 0));
    }

    private boolean hasWater(Location location)
    {
        Location aboveLocation = location.clone().add(0, 1, 0);
        Location belowLocation = location.clone().subtract(0, 1, 0);
        //        System.out.println("Location has water? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return location.getBlock().getType() == Material.WATER || location.getBlock().getType() == Material.STATIONARY_WATER
                || aboveLocation.getBlock().getType() == Material.WATER
                || aboveLocation.getBlock().getType() == Material.STATIONARY_WATER
                || belowLocation.getBlock().getType() == Material.WATER
                || belowLocation.getBlock().getType() == Material.STATIONARY_WATER;
    }

    private boolean hasLava(Location location)
    {
        Location aboveLocation = location.clone().add(0, 1, 0);
        Location belowLocation = location.clone().subtract(0, 1, 0);
        //        System.out.println("Location has lava? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return location.getBlock().getType() == Material.LAVA
                || location.getBlock().getType() == Material.STATIONARY_LAVA
                || aboveLocation.getBlock().getType() == Material.LAVA
                || aboveLocation.getBlock().getType() == Material.STATIONARY_LAVA
                || belowLocation.getBlock().getType() == Material.LAVA
                || belowLocation.getBlock().getType() == Material.STATIONARY_LAVA;
    }

    private boolean hasFire(Location location)
    {
        Location aboveLocation = location.clone().add(0, 1, 0);
        Location belowLocation = location.clone().subtract(0, 1, 0);
        //        System.out.println("Location has fire? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return location.getBlock().getType() == Material.FIRE && aboveLocation.getBlock().getType() == Material.FIRE && belowLocation.getBlock().getType() == Material.FIRE;
    }

    private boolean onSolidGround(Location location)
    {
        // Check if there is solid ground below the given location
        Location belowLocation = location.clone().subtract(0, 1, 0);
        //        System.out.println("Location on solid ground? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return !belowLocation.getBlock().isLiquid() && !isBlockEmpty(belowLocation);
    }

    private Location getRandomizedLocation(World world, double range_min, double range_max)
    {
        double magic = 0.62000000476837D;
        return new Location(world, generateRandomDouble(range_min, range_max), 128, generateRandomDouble(range_min, range_max));
    }

    private double generateRandomDouble(double min, double max)
    {
        return Math.round(min + (max - min) * rng.nextDouble());
    }

    private boolean isBlockEmpty(Location loc)
    {
        return loc.getBlock().getType() == Material.AIR && loc.getBlock().getType() != Material.LEAVES;
    }
}
