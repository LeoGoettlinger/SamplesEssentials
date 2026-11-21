package org.sethomegui;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.sethomegui.Commands.DelHomeCommand;
import org.sethomegui.Commands.DelHomeTabCompleter;
import org.sethomegui.Commands.HomeAdminCommand;
import org.sethomegui.Commands.HomeAdminTabCompleter;
import org.sethomegui.Commands.HomeTabCompleter;
import org.sethomegui.Commands.MainCommands;
import org.sethomegui.Commands.SetHomeCommand;
import org.sethomegui.Listeners.AdminChatListener;
import org.sethomegui.Listeners.AdminMenuClickListener;
import org.sethomegui.Listeners.ChatPromptListener;
import org.sethomegui.Listeners.ConfirmationMenuListener;
import org.sethomegui.Listeners.HomesMenuClickListener;
import org.sethomegui.Listeners.MenuClickListener;
import org.sethomegui.Listeners.PlayerDataListener;
import org.sethomegui.Managers.AdminGUIManager;
import org.sethomegui.Managers.GUIManager;
import org.sethomegui.Managers.HomeManager;
import org.sethomegui.Managers.TeleportManager;
import org.sethomegui.Placeholders.GlobalPlaceholders;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.settings.general.GeneralSettings;
import org.sethomegui.SetHomeGUI.libs.settings.loader.LoaderSettings;
import org.sethomegui.SetHomeGUI.libs.settings.updater.UpdaterSettings;
import org.sethomegui.Storage.StorageManager;

public final class SetHomeGUI extends JavaPlugin {
   private int pluginBridge;
   private YamlDocument mainConfig;
   private YamlDocument guisConfig;
   private YamlDocument actionsConfig;
   private YamlDocument guiAdminConfig;
   private GUIManager guiManager;
   private HomeManager homeManager;
   private TeleportManager teleportManager;
   private AdminGUIManager adminGUIManager;
   private StorageManager storageManager;
   private NamespacedKey actionKey;
   private NamespacedKey targetHomeKey;

   public void onEnable() {
      this.setupFiles();
      int pluginId = 23348;
      new Metrics(this, pluginId);
      this.actionKey = new NamespacedKey(this, "sethomegui_action");
      this.targetHomeKey = new NamespacedKey(this, "sethomegui_target_home");
      this.storageManager = new StorageManager(this);
      this.storageManager.initialize();
      this.guiManager = new GUIManager(this);
      this.homeManager = new HomeManager(this);
      this.teleportManager = new TeleportManager(this);
      this.adminGUIManager = new AdminGUIManager(this);
      this.adminGUIManager.loadPlayersCacheAsync();
      this.getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
      this.getServer().getPluginManager().registerEvents(new ChatPromptListener(this), this);
      this.getServer().getPluginManager().registerEvents(new HomesMenuClickListener(this), this);
      this.getServer().getPluginManager().registerEvents(new ConfirmationMenuListener(this), this);
      this.getServer().getPluginManager().registerEvents(new AdminMenuClickListener(this), this);
      this.getServer().getPluginManager().registerEvents(new AdminChatListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         this.getLogger().info("PlaceholderAPI found! Registering placeholders...");
         new GlobalPlaceholders(this).register();
      } else {
         this.getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
      }

      if (this.getCommand("home") != null) {
         this.getCommand("home").setExecutor(new MainCommands(this));
         this.getCommand("home").setTabCompleter(new HomeTabCompleter(this));
      }

      if (this.getCommand("sethome") != null) {
         this.getCommand("sethome").setExecutor(new SetHomeCommand(this));
      }

      if (this.getCommand("delhome") != null) {
         this.getCommand("delhome").setExecutor(new DelHomeCommand(this));
         this.getCommand("delhome").setTabCompleter(new DelHomeTabCompleter(this));
      }

      if (this.getCommand("homeadmin") != null) {
         this.getCommand("homeadmin").setExecutor(new HomeAdminCommand(this));
         this.getCommand("homeadmin").setTabCompleter(new HomeAdminTabCompleter());
      }

      this.getLogger().info("SetHomeGUI has been successfully enabled on Folia!");
   }

   private void setupFiles() {
      this.pluginBridge = this.preparePluginBridge();

      try {
         if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
         }

         UpdaterSettings updaterSettings = UpdaterSettings.builder().setKeepAll(true).build();
         LoaderSettings loaderSettings = LoaderSettings.builder().setAutoUpdate(true).build();
         this.mainConfig = YamlDocument.create(
            new File(this.getDataFolder(), "config.yml"), this.getResource("config.yml"), GeneralSettings.DEFAULT, loaderSettings, updaterSettings
         );
         this.guisConfig = YamlDocument.create(
            new File(this.getDataFolder(), "gui.yml"), this.getResource("gui.yml"), GeneralSettings.DEFAULT, loaderSettings, updaterSettings
         );
         this.actionsConfig = YamlDocument.create(
            new File(this.getDataFolder(), "actions.yml"), this.getResource("actions.yml"), GeneralSettings.DEFAULT, loaderSettings, updaterSettings
         );
         this.getLogger().info("Configuration files successfully loaded without data loss!");
      } catch (IOException var3) {
         this.getLogger().severe("Could not load config.yml and gui.yml correctly!");
         var3.printStackTrace();
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   public YamlDocument getMainConfig() {
      return this.mainConfig;
   }

   public YamlDocument getGuisConfig() {
      return this.guisConfig;
   }

   public YamlDocument getActionsConfig() {
      return this.actionsConfig;
   }

   public GUIManager getGuiManager() {
      return this.guiManager;
   }

   public HomeManager getHomeManager() {
      return this.homeManager;
   }

   public TeleportManager getTeleportManager() {
      return this.teleportManager;
   }

   public AdminGUIManager getAdminGUIManager() {
      return this.adminGUIManager;
   }

   public StorageManager getStorageManager() {
      return this.storageManager;
   }

   public NamespacedKey getActionKey() {
      return this.actionKey;
   }

   public NamespacedKey getTargetHomeKey() {
      return this.targetHomeKey;
   }

   public YamlDocument getGuiAdminConfig() {
      return this.guiAdminConfig;
   }

   public void onDisable() {
      if (this.storageManager != null) {
         this.storageManager.shutdown();
      }

      this.getLogger().info("SetHomeGUI has been safely disabled.");
   }
}
