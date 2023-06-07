package com.oldschoolminecraft.openrtp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class LocationFinder extends Thread
{
    private final Random rng = new Random();

    private final World world;
    private final LocationCallback callback;

    private final double range_min, range_max;

    private Location location;

    public LocationFinder(RTPConfig config, World world, LocationCallback callback)
    {
        this.world = world;
        this.callback = callback;
        range_min = config.getConfigDouble("range_min");
        range_max = config.getConfigDouble("range_max");
    }

    public void run()
    {
        while (location == null)
        {
            try
            {
                Location tmp = getRandomizedLocation(world, range_min, range_max);
                if (isSafeLocation(tmp))
                {
                    location = tmp.clone();
                    location.setX(Math.round(tmp.getX()) + 0.5);
                    location.setZ(Math.round(tmp.getZ()) + 0.5);
                }
            } catch (Exception ignored) {}
        }

        callback.pipe(location);
    }

    private static boolean isSafeLocation(Location location)
    {
        //        System.out.println("Is location SAFE? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return location.getBlock().isEmpty()
                && onSolidGround(location)
                && !hasFire(location)
                && !hasWater(location)
                && !hasLava(location)
                && !insideSolidBlocks(location);
    }

    private static boolean insideSolidBlocks(Location location)
    {
        //        System.out.println("Location inside solid block? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return !location.getBlock().isEmpty() || !location.clone().add(0, 1, 0).getBlock().isEmpty();
    }

    private static boolean hasWater(Location location)
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

    private static boolean hasLava(Location location)
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

    private static boolean hasFire(Location location)
    {
        Location aboveLocation = location.clone().add(0, 1, 0);
        Location belowLocation = location.clone().subtract(0, 1, 0);
        //        System.out.println("Location has fire? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return location.getBlock().getType() == Material.FIRE && aboveLocation.getBlock().getType() == Material.FIRE && belowLocation.getBlock().getType() == Material.FIRE;
    }

    private static boolean onSolidGround(Location location)
    {
        // Check if there is solid ground below the given location
        Location belowLocation = location.clone().subtract(0, 1, 0);
        //        System.out.println("Location on solid ground? " + (flag ? "Yes" : "No") + " (" + location + ")");
        return !belowLocation.getBlock().isLiquid() && !belowLocation.getBlock().isEmpty();
    }

    private Location getRandomizedLocation(World world, double range_min, double range_max)
    {
        return new Location(world, generateRandomDouble(range_min, range_max), Math.round(70 + generateRandomDouble(0, 58)) + 0.63, generateRandomDouble(range_min, range_max));
    }

    private double generateRandomDouble(double min, double max)
    {
        return Math.round(min + (max - min) * rng.nextDouble()) + 0.5D;
    }
}
