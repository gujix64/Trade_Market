package me.gujix64.trademarket;

import me.gujix64.trademarket.settings.settings;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MarketItemClass {
    public static List<MarketItem> marketItems  = new ArrayList<>();
    public static void addMarketItem(ItemStack sellingItem, ItemStack costItem, String owner) {
        MarketItem marketItem = new MarketItem(sellingItem, costItem, owner);
        marketItems.add(marketItem);
    }
    public static class MarketItem {
        private ItemStack sellingItem;
        private ItemStack costItem;
        private String owner;
        public int time;
        public MarketItem(ItemStack sellingItem, ItemStack costItem, String owner) {
            this.sellingItem = sellingItem;
            this.costItem = costItem;
            this.owner = owner;
            this.time = settings.get().getInt("time");
        }

        public ItemStack getSellingItem() {
            return sellingItem;
        }

        public void setSellingItem(ItemStack sellingItem) {
            this.sellingItem = sellingItem;
        }

        public ItemStack getCostItem() {
            return costItem;
        }

        public void setCostItem(ItemStack costItem) {
            this.costItem = costItem;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }
    }

}
