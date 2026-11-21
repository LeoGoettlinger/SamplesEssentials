package com.donutorders.manager;

import com.donutorders.DonutOrders;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AllowedItemsManager {
   private final DonutOrders plugin;
   private List<Material> allowedMaterials = List.of();

   public AllowedItemsManager(DonutOrders plugin) {
      this.plugin = plugin;
   }

   public void reload() {
      File itemsFile = new File(this.plugin.getDataFolder(), "items.yml");
      if (!itemsFile.exists()) {
         List<String> legacy = this.plugin.getConfig().getStringList("allowed-materials");
         if (!legacy.isEmpty()) {
            FileConfiguration itemsConfig = new YamlConfiguration();
            itemsConfig.set("allowed-items", new ArrayList<>(legacy));

            try {
               itemsConfig.save(itemsFile);
               this.plugin.getLogger().info("[DonutOrders] Created items.yml from legacy config.yml allowed-materials.");
            } catch (IOException var5) {
               this.plugin.getLogger().log(Level.WARNING, "[DonutOrders] Failed to create items.yml from legacy config.", (Throwable)var5);
               this.plugin.saveResource("items.yml", false);
            }
         } else {
            this.plugin.saveResource("items.yml", false);
         }
      }

      FileConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
      List<String> names = itemsConfig.getStringList("allowed-items");
      if (names.isEmpty()) {
         this.migrateFromLegacyConfig(itemsConfig, itemsFile);
         names = itemsConfig.getStringList("allowed-items");
      }

      this.allowedMaterials = Collections.unmodifiableList(this.parseMaterials(names));
      this.plugin.getLogger().info("[DonutOrders] Loaded " + this.allowedMaterials.size() + " allowed order material(s) from items.yml.");
   }

   public List<Material> getAllowedMaterials() {
      return this.allowedMaterials;
   }

   public boolean isAllowed(Material material) {
      return material != null && this.allowedMaterials.contains(material);
   }

   private void migrateFromLegacyConfig(FileConfiguration itemsConfig, File itemsFile) {
      List<String> legacy = this.plugin.getConfig().getStringList("allowed-materials");
      if (!legacy.isEmpty()) {
         itemsConfig.set("allowed-items", new ArrayList<>(legacy));

         try {
            itemsConfig.save(itemsFile);
            this.plugin.getLogger().info("[DonutOrders] Migrated allowed-materials from config.yml to items.yml.");
         } catch (IOException var5) {
            this.plugin.getLogger().log(Level.WARNING, "[DonutOrders] Failed to write migrated items.yml.", (Throwable)var5);
         }
      }
   }

   private List<Material> parseMaterials(List<String> names) {
      Set<Material> unique = new LinkedHashSet<>();

      for (String name : names) {
         if (name != null && !name.isBlank()) {
            try {
               unique.add(Material.valueOf(name.trim().toUpperCase()));
            } catch (IllegalArgumentException var6) {
               this.plugin.getLogger().warning("[DonutOrders] Invalid material in items.yml: " + name);
            }
         }
      }

      return new ArrayList<>(unique);
   }
}
