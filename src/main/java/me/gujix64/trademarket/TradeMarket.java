package me.gujix64.trademarket;


import me.gujix64.trademarket.lang.lang;
import me.gujix64.trademarket.settings.settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.gujix64.trademarket.MarketInventory.*;
import static me.gujix64.trademarket.MarketItemClass.*;
import static me.gujix64.trademarket.Operations.ValidateNumber;
import static me.gujix64.trademarket.Operations.checkMaterial;

public final class TradeMarket extends JavaPlugin implements Listener {

    public String col(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    private Map<String, List<ItemStack>> receiveItems = new HashMap<>();
    private List<String> ReceiveOpen = new ArrayList<>();

    public void saveMarketItemsToFile() {

        try {
            File itemsFile = new File(this.getDataFolder(), "market.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
            config.set("items", null);
            for (int i = 0; i < marketItems.size(); i++) {
                MarketItemClass.MarketItem item = marketItems.get(i);
                String itemKey = "items." + i;
                config.set(itemKey + ".sellingItem", item.getSellingItem());
                config.set(itemKey + ".costItem", item.getCostItem());
                config.set(itemKey + ".owner", item.getOwner());
            }
            config.save(itemsFile);
        } catch (IOException e) {
            getLogger().severe("Error saving items.yml: " + e.getMessage());
        }
    }

    public List<MarketItem> loadMarketItemsFromFile() {
        List<MarketItem> marketItems = new ArrayList<>();

        File itemsFile = new File(this.getDataFolder(), "market.yml");
        if (!itemsFile.exists()) {
            return marketItems;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            return marketItems;
        }

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            ItemStack sellingItem = itemSection.getItemStack("sellingItem");
            ItemStack costItem = itemSection.getItemStack("costItem");
            String owner = itemSection.getString("owner");
            MarketItem marketItem = new MarketItem(sellingItem, costItem, owner);
            marketItem.time = settings.get().getInt("time");
            marketItems.add(marketItem);
        }

        return marketItems;
    }

    private void saveReceiveItems() {
        File file = new File(getDataFolder(), "receive.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, List<ItemStack>> entry : receiveItems.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadIReceiveItems() {
        File file = new File(getDataFolder(), "receive.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            List<ItemStack> value = (List<ItemStack>) config.getList(key);
            receiveItems.put(key, value);
        }
    }
    private void addItemToOwner(String owner, ItemStack item) {
        List<ItemStack> playerItems = receiveItems.get(owner);
        if (playerItems == null) {
            playerItems = new ArrayList<ItemStack>();
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("Item ID: " + playerItems.size());
        meta.setLore(lore);
        item.setItemMeta(meta);
        playerItems.add(item);
        receiveItems.put(owner, playerItems);
        saveReceiveItems();
    }
    private void decreaseTime(List<MarketItem> exampleList)
    {
        List<MarketItem> toRemove = new ArrayList<>();
        for(int i = 0; i < exampleList.size(); ++i)
        {
            exampleList.get(i).time--;
            if(exampleList.get(i).time <= 1)
            {
                String owner = exampleList.get(i).getOwner();
                ItemStack itemStack = exampleList.get(i).getSellingItem();
                ItemMeta meta = itemStack.getItemMeta();
                List<String> list_remover = meta.getLore();
                list_remover.clear();
                meta.setLore(list_remover);
                itemStack.setItemMeta(meta);
                addItemToOwner(owner,itemStack);
                toRemove.add(exampleList.get(i));


            }
            else
            {
                ItemStack itemStack = exampleList.get(i).getSellingItem();
                ItemMeta meta = itemStack.getItemMeta();
                List<String> old_meta = meta.getLore();
                if(old_meta != null)
                old_meta.remove(3);
                old_meta.add(3,ChatColor.GREEN+ "Time remaining: " + ChatColor.GRAY + exampleList.get(i).time);
                MarketItem bufor = exampleList.get(i);
                bufor.getSellingItem().getItemMeta().setLore(old_meta);
                exampleList.remove(i);
                exampleList.add(i,bufor);


            }
        }
        updateInventory();
        exampleList.removeAll(toRemove);
        saveMarketItemsToFile();

    }
    @Override
    public void onEnable() {
        settings.setup();
        settings.defaultsetup();
        settings.get().options().copyDefaults(true);
        settings.save();
        marketItems = loadMarketItemsFromFile();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        loadIReceiveItems();
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                decreaseTime(marketItems);
            }
        }, 0L, 1200L);
        lang.setup();
        lang.defaultsetup();
        lang.get().options().copyDefaults(true);
        lang.save();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("trademarket"))
        {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
                return false;
            }
            if(args.length == 1 && args[0].equalsIgnoreCase("help"))
            {
                Player player = (Player) sender;
                player.sendMessage(col(lang.get().getString("help-1")));
                player.sendMessage(col(lang.get().getString("help-2")));
                player.sendMessage(col(lang.get().getString("help-3")));
                player.sendMessage(col(lang.get().getString("help-4")));
            }
            if(args.length == 1 && args[0].equalsIgnoreCase("info"))
            {
                Player player = (Player) sender;
                player.sendMessage(ChatColor.GREEN + "Author: Gujix64");
                player.sendMessage(ChatColor.GREEN + "Version: 1.0 - Beta");
                player.sendMessage(ChatColor.GREEN + "Contact: Gujix64#1418");

            }
            if(args.length == 3 && args[0].equalsIgnoreCase("sell"))
            {
                Player player = (Player) sender;
                ItemStack itemToSell = player.getInventory().getItemInMainHand();
                String costItemID = args[1];
                if(!ValidateNumber(args[2]))
                {
                    return false;
                }
                int costItemValue = Integer.parseInt(args[2]);
                if (itemToSell.getType() == Material.AIR) {
                    player.sendMessage(col(lang.get().getString("error-1")));
                    return false;
                }
                if(!checkMaterial(args[1],player))
                {
                    return false;
                }
                int number = 0;
                for(int i = 0; i<marketItems.size(); ++i)
                {
                    if(marketItems.get(i).getOwner().equalsIgnoreCase(player.getName()))
                    {
                        number++;
                    }
                }
                if(number >= settings.get().getInt("max_items"))
                {
                    player.sendMessage(col(lang.get().getString("error-2")));
                    return false;
                }
                Material material = Material.matchMaterial(args[1]);
                ItemStack costItemStack = new ItemStack(material,costItemValue);
                addMarketItem(itemToSell,costItemStack,player.getName());
                saveMarketItemsToFile();
                player.sendMessage(ChatColor.GRAY +  "" + itemToSell.getAmount() + "x " + itemToSell.getType() + " added to market for " + costItemValue + "x " + costItemID + ".");
                player.getInventory().setItemInMainHand(null);
                return true;
            }
            else if(args.length == 0)
            {
                Player player = (Player) sender;
                openMarket(player);
            }
            else if(args.length == 1 && args[0].equalsIgnoreCase("receive"))
            {
                Player player = (Player) sender;
                String owner = player.getName();
                if (!receiveItems.containsKey(owner) || receiveItems.get(owner).isEmpty()) {
                    player.sendMessage(col(lang.get().getString("error-3")));
                    return true;
                }
                List<ItemStack> playerItems = receiveItems.get(owner);
                Inventory inventory = Bukkit.createInventory(null, 27, "Receive Items");
                for (ItemStack item : playerItems) {
                    inventory.addItem(item);
                }

                player.openInventory(inventory);
                ReceiveOpen.add(player.getName());
                return true;
            }
        }

        return false;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {

        Player player = (Player) event.getWhoClicked();
        if(marketOpenPlayerList.contains(player.getName()))
        {

            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            if(event.getCurrentItem().getType() == Material.ARROW && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.LIGHT_PURPLE+ "Next Page"))
            {
                openNextPage(player);
                return;
            }
            if(event.getCurrentItem().getType() == Material.ARROW && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.LIGHT_PURPLE+ "Prev Page"))
            {
                openPrevPage(player);
                return;
            }
            for(MarketItemClass.MarketItem item: marketItems)
            {
                if (item.getSellingItem().isSimilar(event.getCurrentItem()))
                {
                    if (player.getInventory().containsAtLeast(item.getCostItem(),item.getCostItem().getAmount()))
                    {
                        if(item.getOwner().equalsIgnoreCase(player.getName()))
                        {
                            player.sendMessage(col(lang.get().getString("error-4")));
                            return;
                        }
                        player.getInventory().removeItem(item.getCostItem());
                        ItemMeta itemMeta = item.getSellingItem().getItemMeta();
                        String key = itemMeta.getLore().get(1);
                        String value = itemMeta.getLore().get(0);
                        List<String> lore = itemMeta.getLore();
                        lore.clear();
                        itemMeta.setLore(lore);
                        item.getSellingItem().setItemMeta(itemMeta);
                        player.getInventory().addItem(item.getSellingItem());
                        event.getClickedInventory().removeItem(event.getCurrentItem());
                        marketItems.remove(item);
                        saveMarketItemsToFile();
                        player.sendMessage( col(lang.get().getString("message-2")) + item.getSellingItem().getAmount()+ "x " + item.getSellingItem().getType() + " "+ col(lang.get().getString("message-3"))+" " + item.getCostItem().getAmount() + "x " + item.getCostItem().getType());
                        updateInventory();
                        String owner = key.substring(11);
                        Pattern pattern = Pattern.compile("7(\\d+)x");
                        Matcher matcher = pattern.matcher(value);
                        int price = 0;
                        if (matcher.find()) {
                            price = Integer.parseInt(matcher.group(1));
                        }

                        String item_id = value.split(" ")[2];
                        Material material = Material.matchMaterial(item_id);
                        ItemStack itemStack1 = new ItemStack(material, price);
                        addItemToOwner(owner,itemStack1);
                        Player ownerPlayer = Bukkit.getPlayer(owner);
                        if(ownerPlayer != null)
                        {
                            ownerPlayer.sendMessage(col(lang.get().getString("message-4")));
                        }
                    } else
                    {
                        player.sendMessage(col(lang.get().getString("error-5")) + item.getCostItem().getType() + " "+col(lang.get().getString("message-5"))+ " " + item.getSellingItem().getAmount() + "x " + item.getSellingItem().getType());
                    }
                    break;
                }
            }


        }

        Inventory clickedInventory = event.getClickedInventory();
        if(ReceiveOpen.contains(event.getWhoClicked().getName())) {
            if (clickedInventory != null && clickedInventory.getHolder() instanceof Player) {
                event.setCancelled(true);
                return;

            }
        }
        Inventory inventory = event.getInventory();
        if (inventory == null || !event.getView().getTitle().equals("Receive Items")) {
            return;
        }
        if (inventory != null && event.getView().getTitle().equalsIgnoreCase("Receive Items")) {

            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType() != Material.AIR) {
                String owner = player.getName();
                List<ItemStack> playerItems = receiveItems.get(owner);
                playerItems.remove(item);
                receiveItems.remove(owner);
                receiveItems.put(owner, playerItems);
                saveReceiveItems();
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();
                if(lore != null)
                    lore.clear();
                meta.setLore(lore);
                item.setItemMeta(meta);
                inventory.remove(item);
                player.getInventory().addItem(item);
            }
        }






    }
    @EventHandler
    public void OnPlayerQuitEvent(PlayerQuitEvent event)
    {
        marketOpenPlayerList.remove(event.getPlayer().getName());
        ReceiveOpen.remove(event.getPlayer().getName());
    }
    @EventHandler
    public void OnPlayerJoinEvent(PlayerJoinEvent event)
    {
        marketOpenPlayerList.remove(event.getPlayer().getName());
        ReceiveOpen.remove(event.getPlayer().getName());
        if(receiveItems.containsKey(event.getPlayer().getName()))
        {
            event.getPlayer().sendMessage("");
            event.getPlayer().sendMessage("");
            event.getPlayer().sendMessage(col(lang.get().getString("message-6")));
            event.getPlayer().sendMessage("");
            event.getPlayer().sendMessage("");
        }

    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if(marketOpenPlayerList.contains(player.getName()))
        {
            marketOpenPlayerList.remove(player.getName());
        }
        if(ReceiveOpen.contains(player.getName()))
        {
            ReceiveOpen.remove(event.getPlayer().getName());
        }

    }
}
