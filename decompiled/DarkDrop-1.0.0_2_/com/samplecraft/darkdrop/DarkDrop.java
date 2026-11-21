package com.samplecraft.darkdrop;

import com.samplecraft.darkdrop.command.DarkDropCommand;
import com.samplecraft.darkdrop.gui.CommandInputSession;
import com.samplecraft.darkdrop.listener.ChestListener;
import com.samplecraft.darkdrop.listener.CommandInputListener;
import com.samplecraft.darkdrop.listener.GUIListener;
import com.samplecraft.darkdrop.manager.ConfigManager;
import com.samplecraft.darkdrop.manager.DropManager;
import com.samplecraft.darkdrop.manager.SettingsManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DarkDrop extends JavaPlugin {
   private static DarkDrop instance;
   private ConfigManager configManager;
   private SettingsManager settingsManager;
   private DropManager dropManager;
   private CommandInputSession commandInputSession;

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.configManager = new ConfigManager(this);
      this.configManager.load();
      this.settingsManager = new SettingsManager(this);
      this.dropManager = new DropManager(this);
      this.commandInputSession = new CommandInputSession(this);
      DarkDropCommand command = new DarkDropCommand(this);
      if (this.getCommand("darkdrop") != null) {
         this.getCommand("darkdrop").setExecutor(command);
         this.getCommand("darkdrop").setTabCompleter(command);
      } else {
         this.getLogger().severe("Befehl 'darkdrop' wurde nicht in plugin.yml gefunden!");
      }

      this.getServer().getPluginManager().registerEvents(new GUIListener(this), this);
      this.getServer().getPluginManager().registerEvents(new CommandInputListener(this), this);
      this.getServer().getPluginManager().registerEvents(new ChestListener(this), this);
      this.getLogger().info("DarkDrop aktiviert. Author: SampleCraft");
      this.getLogger().info("Server Bukkit-Version: " + this.getServer().getBukkitVersion());
      this.getLogger().info("Server Minecraft-Version: " + this.getServer().getMinecraftVersion());
   }

   public void onDisable() {
      if (this.dropManager != null) {
         this.dropManager.cancelAll();
      }

      if (this.settingsManager != null) {
         this.settingsManager.saveAll();
      }

      this.getLogger().info("DarkDrop deaktiviert.");
      instance = null;
   }

   public static DarkDrop getInstance() {
      return instance;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public SettingsManager getSettingsManager() {
      return this.settingsManager;
   }

   public DropManager getDropManager() {
      return this.dropManager;
   }

   public CommandInputSession getCommandInputSession() {
      return this.commandInputSession;
   }
}
