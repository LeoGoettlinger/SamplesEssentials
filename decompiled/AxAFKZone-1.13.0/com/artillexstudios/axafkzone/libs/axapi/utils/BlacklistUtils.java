package com.artillexstudios.axafkzone.libs.axapi.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class BlacklistUtils {
   private final Map<Object, Object> map;

   public BlacklistUtils(Map<Object, Object> map) {
      this.map = new HashMap<>(map);
   }

   @NotNull
   private static List<ItemFlag> getItemFlags(@NotNull List<String> flags) {
      List<ItemFlag> flagList = new ArrayList<>(flags.size());

      for (String flag : flags) {
         ItemFlag itemFlag;
         try {
            itemFlag = ItemFlag.valueOf(flag.toUpperCase(Locale.ENGLISH));
         } catch (Exception var6) {
            continue;
         }

         flagList.add(itemFlag);
      }

      return flagList;
   }

   public boolean allows(ItemStack itemStack, boolean strict) {
      ItemMeta meta = itemStack.getItemMeta();
      int counter = 0;
      String material = (String)this.map.getOrDefault("type", this.map.get("material"));
      if (material != null && itemStack.getType().name().equalsIgnoreCase(material)) {
         if (!strict) {
            return false;
         }

         counter++;
      }

      Optional<Object> flags = Optional.ofNullable(this.map.get("item-flags"));
      if (flags.isPresent()) {
         List<ItemFlag> itemFlags = getItemFlags((List<String>)flags.get());
         if (strict) {
            itemFlags.removeAll(meta.getItemFlags());
            if (itemFlags.isEmpty()) {
               counter++;
            }
         } else {
            Set<ItemFlag> metaFlags = meta.getItemFlags();

            for (ItemFlag itemFlag : itemFlags) {
               if (metaFlags.contains(itemFlag)) {
                  return false;
               }
            }
         }
      }

      Optional<Object> name = Optional.ofNullable(this.map.get("name"));
      if (name.isPresent()) {
         String displayName = meta.getDisplayName();
         if (displayName.equalsIgnoreCase((String)name.get())) {
            if (!strict) {
               return false;
            }

            counter++;
         }
      }

      Optional<Object> lore = Optional.ofNullable(this.map.get("lore"));
      if (lore.isPresent()) {
         List<String> loreLines = (List<String>)flags.get();
         if (strict) {
            loreLines.removeAll(meta.getLore());
            if (loreLines.isEmpty()) {
               counter++;
            }
         } else {
            List<String> metaLore = meta.getLore();
            if (metaLore == null) {
               return false;
            }

            for (String line : metaLore) {
               if (loreLines.contains(line)) {
                  return false;
               }
            }
         }
      }

      Optional<Object> customModelData = Optional.ofNullable(this.map.get("custom-model-data"));
      if (customModelData.isPresent()) {
         int modelData = meta.getCustomModelData();
         if (customModelData.get().equals(modelData)) {
            if (!strict) {
               return false;
            }

            counter++;
         }
      }

      return strict ? counter != this.map.size() : true;
   }
}
