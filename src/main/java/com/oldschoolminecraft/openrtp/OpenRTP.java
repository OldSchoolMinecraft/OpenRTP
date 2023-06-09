package com.oldschoolminecraft.openrtp;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class OpenRTP extends JavaPlugin
{
    private static final Gson gson = new Gson();

    private Essentials essentials;
    private Random rng;
    private RTPConfig config;

    public void onEnable()
    {
        essentials = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
        rng = new Random();
        config = new RTPConfig(new File(getDataFolder(), "config.yml"));

        System.out.println("OpenRTP enabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (label.equalsIgnoreCase("wild"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage(ChatColor.RED + "You must be a player to do this!");
                return true;
            }

            Player ply = (Player) sender;

            if (ply.isSleeping() || ply.isInsideVehicle())
            {
                ply.sendMessage(ChatColor.RED + "You need to be standing.");
                return true;
            }

            boolean immortality_enabled = config.getConfigBoolean("immortality_enabled");
            boolean autohome_enabled = config.getConfigBoolean("autohome_enabled");
            long immortalityDuration = timeToTicks(config.getConfigString("immortality_duration"));
            long autohomeDelay = timeToTicks(config.getConfigString("autohome_delay"));
            long commandCooldown = timeToMillis(config.getConfigString("command_cooldown"));
            long lastUsedTime; // default

            try
            {
                lastUsedTime = getLastCommandUsage(ply.getName());
            } catch (Exception ex) {
                ply.sendMessage(ChatColor.RED + "An error occurred. Please try again later.");
                return true;
            }

            long currentTime = System.currentTimeMillis();
            long timeElapsed = currentTime - lastUsedTime;
            long timeLeft = commandCooldown - timeElapsed;

            if (timeLeft > 0 && !(ply.hasPermission("openrtp.admin") || ply.isOp()))
            {
                long seconds = (timeLeft / 1000) % 60;
                long minutes = (timeLeft / (1000 * 60)) % 60;
                long hours = (timeLeft / (1000 * 60 * 60)) % 24;
                long days = timeLeft / (1000 * 60 * 60 * 24);

                StringBuilder waitTime = new StringBuilder();

                if (days > 0) waitTime.append(days).append(days == 1 ? " day " : " days ");
                if (hours > 0) waitTime.append(hours).append(hours == 1 ? " hour " : " hours ");
                if (minutes > 0) waitTime.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
                if (seconds > 0) waitTime.append(seconds).append(seconds == 1 ? " second" : " seconds");

                ply.sendMessage(ChatColor.RED + "You need to wait: " + ChatColor.YELLOW + waitTime.toString().trim() + ChatColor.RED + " before you can randomly teleport again.");
                return true;
            }

            String teleportMsg = ChatColor.GREEN + "You are about to be randomly teleported...";
            User user = essentials.getOfflineUser(ply.getName());

            boolean alreadyHasGodMode = user.isGodModeEnabled();
            if (immortality_enabled)
            {
                user.setGodModeEnabled(true);
                if (!alreadyHasGodMode) teleportMsg += " You are now temporarily immortal.";
            }

            ply.sendMessage(teleportMsg);

            LocationFinder locationFinder = new LocationFinder(config, ply.getWorld(), (loc) ->
            {
                try
                {
                    saveLastCommandUsage(ply.getName(), System.currentTimeMillis());
                } catch (Exception ex) {
                    ply.sendMessage(ChatColor.RED + "An error occurred. Please try again later.");
                    return;
                }

                System.out.println("[OpenRTP] Teleporting " + ply.getName() + " to random location: " + loc);
                ply.teleport(loc);

                if (immortality_enabled)
                {
                    getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                    {
                        if (!alreadyHasGodMode)
                        {
                            user.setGodModeEnabled(false);
                            ply.sendMessage(ChatColor.RED + "Your immortality has expired.");
                        }
                    }, immortalityDuration);
                }

                if (autohome_enabled)
                {
                    getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                    {
                        user.setHome("wild_tp", ply.getLocation());
                        ply.sendMessage(ChatColor.YELLOW + "A home has been set for you: " + ChatColor.GREEN + "wild_tp.");
                        ply.sendMessage(ChatColor.YELLOW + "You can delete it with: " + ChatColor.RED + "/delhome wild_tp.");
                    }, autohomeDelay);
                }
            });

            locationFinder.start();

            return true;
        }

        if (label.equalsIgnoreCase("openrtp"))
        {
            if (!(sender.hasPermission("openrtp.admin") || sender.isOp()))
            {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }

            if (args.length == 0)
            {
                sender.sendMessage(ChatColor.YELLOW + "OpenRTP " + ChatColor.GRAY + "version " + ChatColor.DARK_GRAY + getDescription().getVersion() + ChatColor.GRAY + "made by " + ChatColor.DARK_GRAY + getDescription().getAuthors().get(0));
            } else if (args.length == 1) {
                if (args[0].equals("reload"))
                {
                    config.reload();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid sub-command.");
                }
            }

            return true;
        }

        return false;
    }

    private long getLastCommandUsage(String username) throws IOException
    {
        File dataDir = new File(getDataFolder(), "data/");
        if (!dataDir.exists()) dataDir.mkdirs();
        File dataFile = new File(dataDir, username + ".json");
        // if file doesn't exist, return a timestamp 24 hours in the past so that the check passes
        if (!dataFile.exists()) return Instant.now().minus(24, ChronoUnit.HOURS).toEpochMilli();
        try (FileReader reader = new FileReader(dataFile))
        {
            JsonObject data = gson.fromJson(reader, JsonObject.class);
            return data.get("lastRTP").getAsLong();
        }
    }

    private void saveLastCommandUsage(String username, long timestamp) throws IOException
    {
        File dataDir = new File(getDataFolder(), "data/");
        if (!dataDir.exists()) dataDir.mkdirs();
        File dataFile = new File(dataDir, username + ".json");
        try (FileWriter writer = new FileWriter(dataFile))
        {
            JsonObject data = new JsonObject();
            data.addProperty("lastRTP", timestamp);
            gson.toJson(data, writer);
        }
    }

    private int timeToTicks(String formattedTime)
    {
        if (formattedTime.isEmpty())
            return 0;

        int multiplier = 1;
        int timeValue = Integer.parseInt(formattedTime.substring(0, formattedTime.length() - 1));
        char timeUnit = formattedTime.charAt(formattedTime.length() - 1);

        switch (timeUnit) {
            case 's':
                multiplier = 20; // 20 ticks per second in Minecraft
                break;
            case 'm':
                multiplier = 20 * 60; // 20 ticks per second * 60 seconds per minute
                break;
            case 'h':
                multiplier = 20 * 60 * 60; // 20 ticks per second * 60 seconds per minute * 60 minutes per hour
                break;
            default:
                throw new IllegalArgumentException("Invalid time unit. Supported units are 's', 'm', and 'h'.");
        }

        return timeValue * multiplier;
    }

    private long timeToMillis(String formattedTime)
    {
        long multiplier;
        char unit = formattedTime.charAt(formattedTime.length() - 1);
        String value = formattedTime.substring(0, formattedTime.length() - 1);

        switch (unit)
        {
            case 's':
                multiplier = 1000L; // 1 second = 1000 milliseconds
                break;
            case 'm':
                multiplier = 60000L; // 1 minute = 60 seconds = 60000 milliseconds
                break;
            case 'h':
                multiplier = 3600000L; // 1 hour = 60 minutes = 3600 seconds = 3600000 milliseconds
                break;
            default:
                throw new IllegalArgumentException("Invalid time unit: " + unit);
        }

        return Long.parseLong(value) * multiplier;
    }

    public void onDisable()
    {
        System.out.println("OpenRTP disabled");
    }
}
