package com.Sw3ys.bigenderchest.storage;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class YamlChestStore implements ChestStore {
   private int serverIntegration;
   private final BiggerEnderChest plugin;

   public YamlChestStore(BiggerEnderChest plugin) {
      this.plugin = plugin;
   }

   @Override
   public void prepare() {
      File folder = this.ensureDataFolder();
      File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
      int count = files == null ? 0 : files.length;
      this.plugin.getLogger().info("YAML storage ready - " + count + " saved ender chest(s).");
   }

   private File ensureDataFolder() {
      this.serverIntegration = this.setupServerIntegration();
      File folder = new File(this.plugin.getDataFolder(), "data");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      return folder;
   }

   private File getFile(UUID uuid) {
      return new File(this.ensureDataFolder(), uuid + ".yml");
   }

   @Override
   public boolean exists(UUID uuid) {
      return this.getFile(uuid).exists();
   }

   @Override
   public ChestStore.Stored read(UUID uuid) {
      File file = this.getFile(uuid);
      if (!file.exists()) {
         return new ChestStore.Stored(27, false, new ItemStack[27]);
      } else {
         YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
         int size = Math.max(27, cfg.getInt("size", 27));
         boolean pending = cfg.getBoolean("pending", false);
         ItemStack[] items = new ItemStack[size];

         for (int i = 0; i < size; i++) {
            items[i] = cfg.getItemStack("items." + i);
         }

         return new ChestStore.Stored(size, pending, items);
      }
   }

   @Override
   public void write(UUID uuid, int size, boolean pending, ItemStack[] items) {
      File file = this.getFile(uuid);
      YamlConfiguration cfg = new YamlConfiguration();
      cfg.set("size", size);
      cfg.set("pending", pending);

      for (int i = 0; i < size; i++) {
         ItemStack item = items != null && i < items.length ? items[i] : null;
         cfg.set("items." + i, item);
      }

      try {
         cfg.save(file);
      } catch (IOException var9) {
         this.plugin.getLogger().warning("Failed to save ender chest " + uuid + ": " + var9.getMessage());
      }
   }

   public List<UUID> listStoredUUIDs() {
      File folder = this.ensureDataFolder();
      File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
      List<UUID> ids = new ArrayList<>();
      if (files == null) {
         return ids;
      } else {
         for (File file : files) {
            String base = file.getName().substring(0, file.getName().length() - ".yml".length());

            try {
               ids.add(UUID.fromString(base));
            } catch (IllegalArgumentException var10) {
            }
         }

         return ids;
      }
   }

   @Override
   public int getSavedSize(UUID uuid) {
      File file = this.getFile(uuid);
      return !file.exists() ? 27 : Math.max(27, YamlConfiguration.loadConfiguration(file).getInt("size", 27));
   }
}
