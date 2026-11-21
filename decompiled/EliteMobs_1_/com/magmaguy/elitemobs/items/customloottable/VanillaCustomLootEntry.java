package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VanillaCustomLootEntry extends CustomLootEntry implements Serializable {
   private static final long serialVersionUID = 849502628548667093L;
   private Material material = null;

   public VanillaCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
      this.parse(fields(rawString));
      entries.add(this);
   }

   public VanillaCustomLootEntry(List<CustomLootEntry> entries, Map<String, Object> configMap, String configFilename) {
      this.parse(fields(configMap));
      entries.add(this);
   }

   private void parse(Map<String, Object> definition) {
      this.commonFields(definition, false, new String[]{"material", "type"});
      if (definition.containsKey("material") && definition.containsKey("type")) {
         throw new IllegalArgumentException("material and type specify the same field");
      } else {
         String value = text(definition, definition.containsKey("material") ? "material" : "type");

         try {
            this.material = Material.valueOf(value.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var4) {
            throw new IllegalArgumentException("invalid material " + value, var4);
         }
      }
   }

   public ItemStack generateItemStack() {
      return new ItemStack(this.material, 1);
   }

   @Override
   public boolean locationDrop(int itemTier, Player player, Location location) {
      if (this.material != null && !this.material.isAir() && this.getAmount() > 0) {
         for (int i = 0; i < this.getAmount(); i++) {
            location.getWorld().dropItem(location, this.generateItemStack());
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean locationDrop(int itemTier, Player player, Location location, EliteEntity eliteEntity) {
      return this.locationDrop(itemTier, player, location);
   }

   @Override
   public boolean directDrop(int itemTier, Player player) {
      if (this.material != null && !this.material.isAir() && this.getAmount() > 0) {
         String name = null;

         for (int i = 0; i < this.getAmount(); i++) {
            ItemStack itemStack = this.generateItemStack();
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack[]{itemStack});
            overflow.values().forEach(leftover -> player.getWorld().dropItem(player.getLocation(), leftover));
            if (name == null && itemStack.getItemMeta() != null) {
               if (itemStack.getItemMeta().hasDisplayName()) {
                  name = itemStack.getItemMeta().getDisplayName();
               } else {
                  name = itemStack.getType().toString().replace("_", " ");
               }
            }
         }

         if (name != null) {
            player.sendMessage(ItemSettingsConfig.getDirectDropMinecraftLootMessage().replace("$itemName", this.getAmount() + "x " + name));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public ItemStack previewDrop(int itemTier, Player player) {
      return this.generateItemStack();
   }

   @Generated
   public Material getMaterial() {
      return this.material;
   }
}
