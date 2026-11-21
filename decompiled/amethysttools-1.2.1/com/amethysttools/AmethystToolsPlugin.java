package com.amethysttools;

import com.amethysttools.command.AmethystToolsCommand;
import com.amethysttools.listener.AmethystPickaxeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class AmethystToolsPlugin extends JavaPlugin {
   private int serverIntegration;
   private NamespacedKey toolKey;
   private NamespacedKey expiresAtKey;
   private NamespacedKey itemTypeKey;
   private AmethystToolService toolService;
   private BukkitTask loreTask;
   private File messagesFile;
   private FileConfiguration messagesConfig;

   public void onEnable() {
      this.ensureManagedFilesAreCurrent();
      this.toolKey = new NamespacedKey(this, "amethyst_tool");
      this.expiresAtKey = new NamespacedKey(this, "expires_at");
      this.itemTypeKey = new NamespacedKey(this, "tool_item_key");
      this.toolService = new AmethystToolService(this, this.toolKey, this.expiresAtKey, this.itemTypeKey);
      PluginCommand command = this.getCommand("amethysttools");
      if (command != null) {
         AmethystToolsCommand executor = new AmethystToolsCommand(this, this.toolService);
         command.setExecutor(executor);
         command.setTabCompleter(executor);
      }

      Bukkit.getPluginManager().registerEvents(new AmethystPickaxeListener(this, this.toolService), this);
      this.startLoreUpdateTask();
   }

   public void onDisable() {
      if (this.loreTask != null) {
         this.loreTask.cancel();
      }
   }

   public void reloadPluginConfig() {
      this.ensureManagedFilesAreCurrent();
      this.toolService.reloadBlockedMaterials();
   }

   public FileConfiguration getMessagesConfig() {
      if (this.messagesConfig == null) {
         this.loadMessagesConfig();
      }

      return this.messagesConfig;
   }

   private void loadMessagesConfig() {
      this.messagesFile = new File(this.getDataFolder(), "messages.yml");
      this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
   }

   private void ensureManagedFilesAreCurrent() {
      String expectedVersion = this.getDescription().getVersion();
      this.ensureVersionedResource("config.yml", "config-version", expectedVersion);
      this.ensureVersionedResource("messages.yml", "messages-version", expectedVersion);
      this.reloadConfig();
      this.loadMessagesConfig();
   }

   private void ensureVersionedResource(String fileName, String versionKey, String expectedVersion) {
      this.serverIntegration = this.setupServerIntegration();
      File targetFile = new File(this.getDataFolder(), fileName);
      if (!targetFile.exists()) {
         this.saveResource(fileName, false);
      } else {
         FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(targetFile);
         String currentVersion = currentConfig.getString(versionKey, "");
         if (!expectedVersion.equals(currentVersion)) {
            String fileVersionForName = this.normalizeVersionForFilename(currentVersion);
            File backupFile = this.createBackupFile(fileName, fileVersionForName);

            try {
               Files.move(targetFile.toPath(), backupFile.toPath());
            } catch (IOException var13) {
               this.getLogger().severe("Failed to archive " + fileName + " before regeneration: " + var13.getMessage());
               return;
            }

            try {
               this.saveResource(fileName, false);
               this.getLogger().warning(fileName + " version mismatch (" + versionKey + "=" + currentVersion + ", expected " + expectedVersion + ").");
               this.getLogger().warning("Archived old file as " + backupFile.getName() + " and regenerated a new " + fileName + ".");
            } catch (IllegalArgumentException var12) {
               this.getLogger().severe("Failed to regenerate " + fileName + ": " + var12.getMessage());

               try {
                  Files.move(backupFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  this.getLogger().warning("Restored original " + fileName + " from backup because regeneration failed.");
               } catch (IOException var11) {
                  this.getLogger().severe("Failed to restore original " + fileName + ": " + var11.getMessage());
               }
            }
         }
      }
   }

   private File createBackupFile(String fileName, String fileVersionForName) {
      String baseName = fileName.endsWith(".yml") ? fileName.substring(0, fileName.length() - 4) : fileName;
      File backupFile = new File(this.getDataFolder(), "__old_" + baseName + "_" + fileVersionForName + "__.yml");

      for (int duplicateIndex = 1; backupFile.exists(); duplicateIndex++) {
         backupFile = new File(this.getDataFolder(), "__old_" + baseName + "_" + fileVersionForName + "__" + duplicateIndex + ".yml");
      }

      return backupFile;
   }

   private String normalizeVersionForFilename(String version) {
      return version != null && !version.isBlank() ? version.replaceAll("[^a-zA-Z0-9._-]", "_") : "unknown";
   }

   private void startLoreUpdateTask() {
      this.loreTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
         for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack[] contents = player.getInventory().getContents();
            this.updateInventory(contents, player);
            player.getInventory().setContents(contents);
            ItemStack[] armor = player.getInventory().getArmorContents();
            this.updateInventory(armor, player);
            player.getInventory().setArmorContents(armor);
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (offhand != null) {
               player.getInventory().setItemInOffHand(this.toolService.updateOrExpire(offhand, player));
            }
         }
      }, 20L, 1200L);
   }

   private void updateInventory(ItemStack[] contents, Player owner) {
      for (int i = 0; i < contents.length; i++) {
         contents[i] = this.toolService.updateOrExpire(contents[i], owner);
      }
   }
}
