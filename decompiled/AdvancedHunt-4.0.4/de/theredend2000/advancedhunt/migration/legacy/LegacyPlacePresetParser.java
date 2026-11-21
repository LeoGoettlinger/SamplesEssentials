package de.theredend2000.advancedhunt.migration.legacy;

import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.model.PlaceItem;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.ItemSerializer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public final class LegacyPlacePresetParser {
   private static final String DEFAULT_GROUP = "Default";
   private static final String TEXTURE_PREFIX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";

   private LegacyPlacePresetParser() {
   }

   public static List<PlaceItem> parseAll(File legacyRootFolder) {
      File configFile = new File(legacyRootFolder, "config.yml");
      if (!configFile.exists()) {
         return Collections.emptyList();
      } else {
         FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
         ConfigurationSection placeSection = config.getConfigurationSection("Place");
         if (placeSection == null) {
            return Collections.emptyList();
         } else {
            List<PlaceItem> presets = new ArrayList<>();

            for (String key : placeSection.getKeys(false)) {
               ConfigurationSection entry = placeSection.getConfigurationSection(key);
               if (entry != null) {
                  PlaceItem preset = parseEntry(key, entry);
                  if (preset != null) {
                     presets.add(preset);
                  }
               }
            }

            return presets;
         }
      }
   }

   private static PlaceItem parseEntry(String key, ConfigurationSection entry) {
      String typeStr = entry.getString("type", "PLAYER_HEAD");
      String texture = entry.getString("texture");

      XMaterial material;
      try {
         material = XMaterial.valueOf(typeStr.toUpperCase());
      } catch (IllegalArgumentException var8) {
         material = XMaterial.PLAYER_HEAD;
      }

      ItemStack item;
      if (material == XMaterial.PLAYER_HEAD && texture != null && !texture.isEmpty()) {
         String fullTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" + texture;
         item = new ItemBuilder(XMaterial.PLAYER_HEAD).setSkullTexture(fullTexture).build();
      } else {
         item = material.parseItem();
         if (item == null) {
            item = XMaterial.PLAYER_HEAD.parseItem();
         }
      }

      String serialized = ItemSerializer.serialize(item);
      if (serialized == null) {
         return null;
      } else {
         String name = "Treasure " + key;
         return new PlaceItem(UUID.randomUUID(), "Default", name, serialized);
      }
   }
}
