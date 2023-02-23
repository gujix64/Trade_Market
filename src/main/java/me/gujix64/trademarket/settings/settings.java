package me.gujix64.trademarket.settings;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class settings {
    private static File file;
    private static FileConfiguration settings;

    public static void setup()
    {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("TradeMarket").getDataFolder(), "settings.yml");
        if(!file.exists())
        {
            try{
                file.createNewFile();
            }catch (IOException e) {
                //Exception
            }
        }
        settings = YamlConfiguration.loadConfiguration(file);

    }

    public static FileConfiguration get()
    {
        return settings;
    }
    public static void save()
    {
        try{
            settings.save(file);
        }catch (IOException e)
        {
            System.out.println("[Trade Market] Can't save the file. Contact with the owner to fix your issue sizeof#2792");
        }
    }

    public static void reload()
    {
        settings = YamlConfiguration.loadConfiguration(file);
    }
    public static void defaultsetup()
    {

        get().addDefault("time",60);
        get().addDefault("max_items",9);

    }

}
