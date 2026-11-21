package com.itzazeem.asbounty;

import com.itzazeem.asbounty.commands.BountyCommand;
import com.itzazeem.asbounty.listeners.KillListener;
import com.itzazeem.asbounty.manager.BountyManager;
import com.itzazeem.asbounty.manager.ConfigManager;
import com.itzazeem.asbounty.manager.HeadCacheManager;
import com.itzazeem.asbounty.manager.SignManager;
import com.itzazeem.asbounty.manager.SoundManager;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class AsBounty extends JavaPlugin implements Listener {
   private static AsBounty instance;
   private Economy economy;
   private BountyManager bountyManager;
   private ConfigManager configManager;
   private SoundManager soundManager;
   private SignManager signManager;
   private HeadCacheManager headCacheManager;
   private Connection databaseConnection;
   private Connection headDatabaseConnection;

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.saveResource("lang.yml", false);
      this.saveResource("gui/confirm-gui.yml", false);
      this.saveResource("gui/main-gui.yml", false);
      this.configManager = new ConfigManager(this);
      this.soundManager = new SoundManager(this);
      this.signManager = new SignManager(this);
      this.setupDatabase();
      this.setupHeadDatabase();
      this.bountyManager = new BountyManager(this);
      this.headCacheManager = new HeadCacheManager(this);
      if (this.setupEconomy()) {
         this.getLogger().info("Successfully hooked into Economy provider: " + this.economy.getName());
      } else if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         this.getLogger().warning("Vault plugin not detected! Economy functionality is suspended until Vault is installed.");
      } else {
         this.getLogger().warning("Vault is installed, but NO Economy provider (e.g., EssentialsX) was found!");
         this.getLogger().warning("Please install an economy plugin like EssentialsX so Vault can manage player balances.");
         this.getLogger().warning("AsBounty will remain enabled and will automatically hook once an economy provider is registered.");
      }

      this.getServer().getPluginManager().registerEvents(this, this);
      BountyCommand bountyCommand = new BountyCommand(this);
      bountyCommand.setSignManager(this.signManager);
      PluginCommand command = this.getCommand("bounty");
      if (command != null) {
         command.setExecutor(bountyCommand);
         command.setTabCompleter(bountyCommand);
      }

      this.getServer().getPluginManager().registerEvents(new KillListener(this), this);
      this.getLogger().info("========================================");
      this.getLogger().info(" AsBounty v" + this.getDescription().getVersion() + " successfully enabled!");
      this.getLogger().info(" Developed by itzazeem");
      this.getLogger().info(" Minecraft 1.21.x - 26.x Supported");
      this.getLogger().info("========================================");
   }

   public void onDisable() {
      if (this.databaseConnection != null) {
         try {
            this.databaseConnection.close();
         } catch (SQLException var3) {
            var3.printStackTrace();
         }
      }

      if (this.headDatabaseConnection != null) {
         try {
            this.headDatabaseConnection.close();
         } catch (SQLException var2) {
            var2.printStackTrace();
         }
      }

      this.getLogger().info("AsBounty disabled.");
   }

   public boolean hasEconomy() {
      return this.economy != null ? true : this.setupEconomy();
   }

   public boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return false;
         } else {
            this.economy = (Economy)rsp.getProvider();
            return this.economy != null;
         }
      }
   }

   @EventHandler
   public void onServiceRegister(ServiceRegisterEvent event) {
      if (event.getProvider().getService().equals(Economy.class) && this.setupEconomy()) {
         this.getLogger().info("Hooked into newly registered Economy provider: " + this.economy.getName());
      }
   }

   @EventHandler
   public void onServiceUnregister(ServiceUnregisterEvent event) {
      if (event.getProvider().getService().equals(Economy.class)) {
         this.economy = null;
         this.getLogger().warning("Economy provider unregistered.");
      }
   }

   private void setupDatabase() {
      try {
         if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
         }

         Class.forName("org.sqlite.JDBC");
         File dbFile = new File(this.getDataFolder(), "bounties.db");
         this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

         try (Statement stmt = this.databaseConnection.createStatement()) {
            stmt.execute(
               "    CREATE TABLE IF NOT EXISTS bounties (\n        id INTEGER PRIMARY KEY AUTOINCREMENT,\n        target_uuid VARCHAR(36) NOT NULL,\n        target_name VARCHAR(16) NOT NULL,\n        amount DOUBLE NOT NULL,\n        added_by VARCHAR(36),\n        timestamp BIGINT NOT NULL,\n        claimed BOOLEAN DEFAULT 0\n    )\n"
            );
         }
      } catch (ClassNotFoundException | SQLException var7) {
         var7.printStackTrace();
      }
   }

   private void setupHeadDatabase() {
      try {
         if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
         }

         Class.forName("org.sqlite.JDBC");
         File dbFile = new File(this.getDataFolder(), "heads.db");
         this.headDatabaseConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

         try (Statement stmt = this.headDatabaseConnection.createStatement()) {
            stmt.execute(
               "    CREATE TABLE IF NOT EXISTS player_heads (\n        uuid VARCHAR(36) PRIMARY KEY,\n        name VARCHAR(16) NOT NULL,\n        head_base64 TEXT,\n        last_updated BIGINT NOT NULL\n    )\n"
            );
         }
      } catch (ClassNotFoundException | SQLException var7) {
         var7.printStackTrace();
      }
   }

   public static AsBounty getInstance() {
      return instance;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public BountyManager getBountyManager() {
      return this.bountyManager;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public SoundManager getSoundManager() {
      return this.soundManager;
   }

   public SignManager getSignManager() {
      return this.signManager;
   }

   public HeadCacheManager getHeadCacheManager() {
      return this.headCacheManager;
   }

   public Connection getDatabaseConnection() {
      return this.databaseConnection;
   }

   public Connection getHeadDatabaseConnection() {
      return this.headDatabaseConnection;
   }
}
