package com.oldschoolminecraft.openrtp;

import org.bukkit.util.config.Configuration;

import java.io.File;

public class RTPConfig extends Configuration
{
    public RTPConfig(File file)
    {
        super(file);
        reload();
    }

    public void reload()
    {
        this.load();
        this.write();
        this.save();
    }

    private void write()
    {
        generateConfigOption("range_min", -60000);
        generateConfigOption("range_max", 60000);
        generateConfigOption("command_cooldown", "4h"); // use essentials time format/parsing
        generateConfigOption("immortality_duration", "5s"); // use essentials time format/parsing
        generateConfigOption("immortality_enabled", true);
        generateConfigOption("safety_iterations", 5);
        generateConfigOption("autohome_delay", "5s");
        generateConfigOption("autohome_enabled", true);
    }

    private void generateConfigOption(String key, Object defaultValue)
    {
        if (this.getProperty(key) == null)
        {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    //Getters Start
    public Object getConfigOption(String key)
    {
        return this.getProperty(key);
    }

    public Object getConfigOption(String key, Object defaultValue)
    {
        Object value = getConfigOption(key);
        if (value == null)
        {
            value = defaultValue;
        }
        return value;

    }

    public String getConfigString(String key)
    {
        return String.valueOf(getConfigOption(key));
    }

    public Integer getConfigInteger(String key)
    {
        return Integer.valueOf(getConfigString(key));
    }

    public Long getConfigLong(String key)
    {
        return Long.valueOf(getConfigString(key));
    }

    public Double getConfigDouble(String key)
    {
        return Double.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key)
    {
        return Boolean.valueOf(getConfigString(key));
    }

    //Getters End

    private boolean convertToNewAddress(String newKey, String oldKey)
    {
        if (this.getString(newKey) != null)
        {
            return false;
        }
        if (this.getString(oldKey) == null)
        {
            return false;
        }
        System.out.println("Converting Config: " + oldKey + " to " + newKey);
        Object value = this.getProperty(oldKey);
        this.setProperty(newKey, value);
        this.removeProperty(oldKey);
        return true;
    }
}
