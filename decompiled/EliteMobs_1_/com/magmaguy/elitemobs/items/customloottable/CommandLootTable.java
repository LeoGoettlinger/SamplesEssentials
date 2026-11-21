package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.StaticItemNamesConfig;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandLootTable extends CustomLootEntry implements Serializable {
   private static final long serialVersionUID = -8058067487928551461L;
   private String command = null;

   public CommandLootTable(List<CustomLootEntry> entries, String rawString, String configFilename) {
      this(entries, fields(rawString), configFilename);
   }

   CommandLootTable(List<CustomLootEntry> entries, Map<String, Object> definition, String configFilename) {
      this.commonFields(definition, true, new String[]{"command"});
      this.command = text(definition, "command");
      entries.add(this);
   }

   @Override
   public boolean locationDrop(int itemTier, Player player, Location location) {
      return this.directDrop(itemTier, player);
   }

   @Override
   public boolean directDrop(int itemTier, Player player) {
      if (this.command == null || this.command.isBlank() || this.getAmount() <= 0) {
         return false;
      } else if (!this.getPermission().isEmpty() && !player.hasPermission(this.getPermission())) {
         return false;
      } else {
         boolean dispatched = false;

         for (int i = 0; i < this.getAmount(); i++) {
            dispatched |= Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.command.replace("%player%", player.getName()));
         }

         return dispatched;
      }
   }

   @Override
   public ItemStack previewDrop(int itemTier, Player player) {
      ItemStack paperItem = new ItemStack(Material.PAPER);
      ItemMeta paperMeta = paperItem.getItemMeta();
      paperMeta.setDisplayName(StaticItemNamesConfig.getLootPreviewCommandDisplayName());
      paperMeta.setLore(
         List.of(
            StaticItemNamesConfig.getLootPreviewCommandLabel() + this.command,
            StaticItemNamesConfig.getLootPreviewChanceLabel() + this.getChance(),
            StaticItemNamesConfig.getLootPreviewTimesLabel() + this.getAmount()
         )
      );
      paperItem.setItemMeta(paperMeta);
      return paperItem;
   }

   @Generated
   public String getCommand() {
      return this.command;
   }
}
