package me.gujix64.trademarket.lang;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class lang {
    private static File file;
    private static FileConfiguration lang;

    public static void setup()
    {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("TradeMarket").getDataFolder(), "lang.yml");
        if(!file.exists())
        {
            try{
                file.createNewFile();
            }catch (IOException e) {
                //Exception
            }
        }
        lang = YamlConfiguration.loadConfiguration(file);

    }

    public static FileConfiguration get()
    {
        return lang;
    }
    public static void save()
    {
        try{
            lang.save(file);
        }catch (IOException e)
        {
            System.out.println("[Trade Market] Can't save the file. Contact with the owner to fix your issue sizeof#2792");
        }
    }

    public static void reload()
    {
        lang = YamlConfiguration.loadConfiguration(file);
    }
    public static void defaultsetup()
    {

        get().addDefault("help-1","&a /trademarket sell <item id> <item amount> - Sells item in main hand for item id and item amount in arguments");
        get().addDefault("help-2","&a /trademarket receive - Here player can take items");
        get().addDefault("help-3","&a /trademarket - Opens market");
        get().addDefault("help-4","&a /trademarket info - information about plugin");
        get().addDefault("error-1","&4 You must hold an item to sell it on the market.");
        get().addDefault("error-2","&4You have max amount of items on sale");
        get().addDefault("error-3","&4You have no items to receive");
        get().addDefault("error-4","&4You cant buy own item");
        get().addDefault("message-1","&f added to market for");
        get().addDefault("message-2","&f You have successfully purchased");
        get().addDefault("message-3","&f for");
        get().addDefault("message-4","&f Someone bought your item! Receive it by typing /trademarket receive");
        get().addDefault("error-5","&4 You do not have enough");
        get().addDefault("message-5","&4 to purchase");
        get().addDefault("message-6","&4 You have items in your receive section check it out!");
    }

}
