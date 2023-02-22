package me.gujix64.trademarket;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Operations {

    public static boolean ValidateNumber(String arg1) {
        try {
            Integer.parseInt(arg1);
            return true;
        } catch (NumberFormatException var4) {
            return false;
        }
    }
    public static boolean checkMaterial(String arg1, Player player) {
        if (Material.matchMaterial(arg1) != null ) {
            return true;
        } else {
            player.sendMessage("Invalid item ID provided");
            return false;
        }
    }
}
