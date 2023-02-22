package me.gujix64.trademarket;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.gujix64.trademarket.MarketItemClass.marketItems;

public class MarketInventory {
    public static List<String> createLore(MarketItemClass.MarketItem item, int currentitem)
    {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Price: " + ChatColor.GRAY + item.getCostItem().getAmount() + "x " + item.getCostItem().getType());
        lore.add(ChatColor.GREEN + "Owner: " + ChatColor.GRAY + item.getOwner());
        lore.add(ChatColor.GREEN+ "Item Sell ID: " + ChatColor.GRAY + currentitem);
        lore.add(ChatColor.GREEN+ "Time remaining: " + ChatColor.GRAY + item.time+" minutes");
        return lore;
    }
    private static ItemStack createNextPageArrow()
    {
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta itemMeta = nextPage.getItemMeta();
        itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Next Page");
        nextPage.setItemMeta(itemMeta);
        return nextPage;
    }
    private static ItemStack createPrevPageArrow()
    {
        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta itemMeta = prevPage.getItemMeta();
        itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Prev Page");
        prevPage.setItemMeta(itemMeta);
        return prevPage;
    }
    public static List<String> marketOpenPlayerList = new ArrayList<>();
    public static List<Inventory> marketPage = new ArrayList<>();
    public static HashMap<String,Integer> currentPage = new HashMap<>();
    public static void openMarket(Player player)
    {
        if(marketPage != null) {
            marketPage.clear();
        }
        Inventory bufor = Bukkit.createInventory(null,54,"Market");
        int current_item = 1;
        int max_item = 1;
        int page = 0;
        for(MarketItemClass.MarketItem item: marketItems)
        {
            if(current_item < 45)
            {
                ItemStack itemStack = item.getSellingItem();
                ItemMeta meta = itemStack.getItemMeta();
                meta.setLore(createLore(item,max_item));
                itemStack.setItemMeta(meta);
                bufor.addItem(itemStack);
                current_item++;
                max_item++;

            }
            else
            {
                ItemStack nextPage = createNextPageArrow();
                ItemStack prevPage = createPrevPageArrow();
                bufor.setItem(53,nextPage);
                bufor.setItem(45,prevPage);
                Inventory bufor2 = Bukkit.createInventory(null,54,"Page " + page);
                bufor2.setContents(bufor.getContents());
                marketPage.add(page,bufor2);
                bufor.clear();
                current_item = 1;
                page++;
                ItemStack itemStack = item.getSellingItem();
                ItemMeta meta = itemStack.getItemMeta();
                meta.setLore(createLore(item,max_item));
                itemStack.setItemMeta(meta);
                bufor.addItem(itemStack);
                current_item++;
                max_item++;
            }
        }
        ItemStack nextPage = createNextPageArrow();
        ItemStack prevPage = createPrevPageArrow();
        bufor.setItem(53,nextPage);
        bufor.setItem(45,prevPage);
        Inventory bufor2 = Bukkit.createInventory(null,54,"Page " + page);
        bufor2.setContents(bufor.getContents());
        marketPage.add(page,bufor2);
        player.openInventory(marketPage.get(0));
        if(marketOpenPlayerList.contains(player.getName()))
        {

            marketOpenPlayerList.remove(player.getName());
        }
        marketOpenPlayerList.add(player.getName());
        if(currentPage.containsKey(player.getName()))
        {
            currentPage.remove(player.getName());
        }
        currentPage.put(player.getName(),0);
    }
    public static void openNextPage(Player player)
    {
        int player_page = currentPage.get(player.getName());
        int max_page = marketItems.size()/45;
        if(player_page < max_page)
        {
            player.closeInventory();
            player.openInventory(marketPage.get(player_page+1));
            currentPage.remove(player.getName());
            currentPage.put(player.getName(),player_page+1);
            marketOpenPlayerList.add(player.getName());
        }
    }
    public static void openPrevPage(Player player)
    {
        int player_page = currentPage.get(player.getName());
        if(player_page > 0)
        {
            player.closeInventory();
            player.openInventory(marketPage.get(player_page-1));
            currentPage.remove(player.getName());
            currentPage.put(player.getName(),player_page-1);
            marketOpenPlayerList.add(player.getName());

        }
    }
    public static void updateInventory() {
        for (int i = 0; i < marketOpenPlayerList.size(); ++i)
        {
            for(Player player :Bukkit.getOnlinePlayers())
            {
                if(player.getName().equalsIgnoreCase(marketOpenPlayerList.get(i)))
                {
                    player.closeInventory();
                    openMarket(player);
                }
            }
        }
    }

}
