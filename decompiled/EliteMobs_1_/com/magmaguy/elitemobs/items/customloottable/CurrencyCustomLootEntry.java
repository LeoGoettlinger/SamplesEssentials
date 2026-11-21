package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.StaticItemNamesConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.items.ItemLootShower;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CurrencyCustomLootEntry extends CustomLootEntry implements Serializable {
   private static final long serialVersionUID = -4310216171170221549L;
   private int currencyAmount = 0;

   public CurrencyCustomLootEntry(List<CustomLootEntry> entries, int currencyAmount) {
      this.currencyAmount = currencyAmount;
      entries.add(this);
   }

   public CurrencyCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
      this.parse(fields(rawString));
      entries.add(this);
   }

   public CurrencyCustomLootEntry(List<CustomLootEntry> entries, Map<?, ?> configMap, String configFilename) {
      this.parse(fields(configMap));
      entries.add(this);
   }

   private void parse(Map<String, Object> definition) {
      this.commonFields(definition, true, new String[]{"currencyamount"});
      this.currencyAmount = integer(definition, "currencyamount");
   }

   @Override
   public boolean locationDrop(int itemTier, Player player, Location location) {
      if (this.currencyAmount > 0 && this.getAmount() > 0) {
         new ItemLootShower(location, player, this.currencyAmount);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean directDrop(int itemTier, Player player) {
      if (this.currencyAmount > 0 && this.getAmount() > 0) {
         EconomyHandler.addCurrency(player.getUniqueId(), this.currencyAmount);
         player.sendMessage(
            ItemSettingsConfig.getDirectDropCoinMessage()
               .replace("$amount", this.currencyAmount + "")
               .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
         );
         return true;
      } else {
         return false;
      }
   }

   @Override
   public ItemStack previewDrop(int itemTier, Player player) {
      ItemStack paperItem = new ItemStack(Material.PAPER);
      ItemMeta paperMeta = paperItem.getItemMeta();
      paperMeta.setDisplayName(StaticItemNamesConfig.getLootPreviewCurrencyDisplayName());
      paperMeta.setLore(
         List.of(StaticItemNamesConfig.getLootPreviewAmountLabel() + this.currencyAmount, StaticItemNamesConfig.getLootPreviewChanceLabel() + this.getChance())
      );
      paperItem.setItemMeta(paperMeta);
      return paperItem;
   }

   @Generated
   public int getCurrencyAmount() {
      return this.currencyAmount;
   }
}
