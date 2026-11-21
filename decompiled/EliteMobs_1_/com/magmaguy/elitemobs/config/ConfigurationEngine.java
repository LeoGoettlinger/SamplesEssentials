package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ConfigurationEngine extends com.magmaguy.elitemobs.magmacore.config.ConfigurationEngine {
   public static String setString(File file, FileConfiguration fileConfiguration, String key, String defaultValue, boolean translatable) {
      fileConfiguration.addDefault(key, defaultValue);
      return translatable
         ? TranslationsConfig.add(file.getName(), key, fileConfiguration.getString(key))
         : ChatColorConverter.convert(fileConfiguration.getString(key));
   }

   public static String setString(List<String> comments, File file, FileConfiguration fileConfiguration, String key, String defaultValue, boolean translatable) {
      String value = setString(file, fileConfiguration, key, defaultValue, translatable);
      setComments(fileConfiguration, key, comments);
      return value;
   }

   public static List setList(File file, FileConfiguration fileConfiguration, String key, List defaultValue, boolean translatable) {
      fileConfiguration.addDefault(key, defaultValue);
      return translatable ? TranslationsConfig.add(file.getName(), key, fileConfiguration.getList(key)) : fileConfiguration.getList(key);
   }

   public static List setList(List<String> comment, File file, FileConfiguration fileConfiguration, String key, List defaultValue, boolean translatable) {
      List value = setList(file, fileConfiguration, key, defaultValue, translatable);
      setComments(fileConfiguration, key, comment);
      return value;
   }

   public static ItemStack setItemStack(File file, FileConfiguration fileConfiguration, String key, ItemStack itemStack, boolean translatable) {
      fileConfiguration.addDefault(key + ".material", itemStack.getType().toString());
      if (itemStack.getType().equals(Material.PLAYER_HEAD)) {
         fileConfiguration.addDefault(key + ".owner", ((SkullMeta)itemStack.getItemMeta()).getOwner());
      }

      Material material;
      try {
         material = Material.valueOf(fileConfiguration.getString(key + ".material"));
      } catch (Exception var12) {
         Logger.warn("Material type " + fileConfiguration.getString(key + ".material") + " is not valid! Correct it to make a valid item.");
         return null;
      }

      String name = "";

      try {
         name = setString(file, fileConfiguration, key + ".name", itemStack.getItemMeta().getDisplayName(), true);
      } catch (Exception var11) {
         Logger.warn("Item name " + fileConfiguration.getString(key + ".name") + " is not valid! Correct it to make a valid item.");
      }

      List<String> lore = new ArrayList<>();

      try {
         lore = setList(
            file,
            fileConfiguration,
            key + ".lore",
            itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore() ? itemStack.getItemMeta().getLore() : null,
            true
         );
      } catch (Exception var13) {
         Logger.warn("Item lore " + fileConfiguration.getString(key + ".lore") + " is not valid! Correct it to make a valid item.");
      }

      ItemStack fileItemStack = ItemStackGenerator.generateItemStack(material, name, lore);
      if (material == Material.PLAYER_HEAD) {
         String owner = fileConfiguration.getString(key + ".owner");
         if (owner != null && !owner.isBlank()) {
            SkullMeta skullMeta = (SkullMeta)fileItemStack.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            fileItemStack.setItemMeta(skullMeta);
         }
      }

      return fileItemStack;
   }
}
