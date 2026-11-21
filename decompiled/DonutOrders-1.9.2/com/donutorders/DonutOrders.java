package com.donutorders;

import com.donutorders.command.OrderAdminCommand;
import com.donutorders.command.OrderCommand;
import com.donutorders.command.OrdersCommand;
import com.donutorders.libs.bstats.bukkit.Metrics;
import com.donutorders.listener.ChatListener;
import com.donutorders.listener.InventoryListener;
import com.donutorders.listener.LuckPermsLimitListener;
import com.donutorders.listener.PlayerJoinListener;
import com.donutorders.listener.PlayerQuitListener;
import com.donutorders.manager.AllowedItemsManager;
import com.donutorders.manager.ChatInputHandler;
import com.donutorders.manager.GUIManager;
import com.donutorders.manager.OrderLimitManager;
import com.donutorders.manager.OrderManager;
import com.donutorders.placeholder.DonutOrdersExpansion;
import com.donutorders.scheduler.FoliaScheduler;
import com.donutorders.storage.StorageManager;
import com.donutorders.util.EnchantOrderUtils;
import com.donutorders.util.MessageHelper;
import com.donutorders.util.ModrinthUpdateChecker;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DonutOrders extends JavaPlugin {
   private int dependencyHooks;
   private static DonutOrders instance;
   private Economy economy;
   private StorageManager storageManager;
   private OrderManager orderManager;
   private ChatInputHandler chatInputHandler;
   private GUIManager guiManager;
   private AllowedItemsManager allowedItemsManager;
   private OrderLimitManager orderLimitManager;
   private FileConfiguration messages;
   private boolean pluginReady;
   private volatile String updateLatestVersion;
   private volatile String updateDownloadUrl;

   public void onEnable() {
      this.dependencyHooks = this.setupDependencyHooks();
      instance = this;
      this.getLogger()
         .info(
            FoliaScheduler.IS_FOLIA
               ? "[DonutOrders] Folia detected — using regionised schedulers."
               : "[DonutOrders] Paper/Spigot detected — using BukkitScheduler."
         );
      this.saveDefaultConfig();
      this.saveResourceIfAbsent("messages.yml");
      this.reloadMessages();
      if (!this.setupEconomy()) {
         this.getLogger().severe("[DonutOrders] Vault economy not found! Disabling plugin.");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         try {
            this.storageManager = new StorageManager(this.getDataFolder());
         } catch (SQLException var2) {
            this.getLogger().log(Level.SEVERE, "[DonutOrders] Failed to initialise database.", (Throwable)var2);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }

         this.registerStartupOrdersCommand();
         this.storageManager.loadAll(this::finishEnable);
      }
   }

   private void finishEnable() {
      try {
         this.completeEnable();
      } catch (Throwable var2) {
         this.getLogger().log(Level.SEVERE, "[DonutOrders] Failed to finish startup after loading orders.", var2);
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   private void completeEnable() {
      this.allowedItemsManager = new AllowedItemsManager(this);
      this.allowedItemsManager.reload();
      this.chatInputHandler = new ChatInputHandler();
      this.orderLimitManager = new OrderLimitManager();
      this.orderManager = new OrderManager(this.storageManager, this.economy);
      this.guiManager = new GUIManager(this.storageManager, this.orderManager, this.chatInputHandler);
      this.registerCommand("orders", new OrdersCommand(this.guiManager));
      this.registerCommand("order", new OrderCommand(this.guiManager));
      this.registerCommand("orderadmin", new OrderAdminCommand(this.guiManager));
      this.pluginReady = true;
      PluginManager pm = this.getServer().getPluginManager();
      pm.registerEvents(new InventoryListener(this.guiManager), this);
      pm.registerEvents(new ChatListener(this.chatInputHandler), this);
      pm.registerEvents(new PlayerJoinListener(this.orderLimitManager), this);
      pm.registerEvents(new PlayerQuitListener(this.guiManager, this.chatInputHandler, this.orderLimitManager), this);
      if (pm.getPlugin("LuckPerms") != null) {
         LuckPerms luckPerms = (LuckPerms)Bukkit.getServicesManager().load(LuckPerms.class);
         if (luckPerms != null) {
            new LuckPermsLimitListener(this.orderLimitManager).register(luckPerms);
            this.getLogger().info("[DonutOrders] LuckPerms detected — rank-based order limits enabled.");
         }
      }

      if (pm.getPlugin("PlaceholderAPI") != null) {
         new DonutOrdersExpansion(this, this.orderLimitManager).register();
         this.getLogger().info("[DonutOrders] PlaceholderAPI detected — %donutorders_order_limit% registered.");
      }

      FoliaScheduler.runGlobalRepeating(() -> this.orderManager.runExpiryCheck(), 1200L, 1200L);
      this.storageManager.startSyncTask();
      FoliaScheduler.runGlobalRepeating(() -> this.chatInputHandler.tickTimeouts(), 600L, 600L);
      this.getLogger()
         .info(
            "[DonutOrders] Enabled successfully. "
               + this.storageManager.getAllOrders().size()
               + " orders loaded."
               + (this.storageManager.isMysql() ? " (MySQL backend)" : " (SQLite backend)")
         );

      try {
         new Metrics(this, 33559);
      } catch (Throwable var3) {
         this.getLogger().log(Level.WARNING, "[DonutOrders] Failed to start bStats metrics.", var3);
      }

      ModrinthUpdateChecker.checkAsync(this);
   }

   public void setUpdateAvailable(String latestVersion, String downloadUrl) {
      this.updateLatestVersion = latestVersion;
      this.updateDownloadUrl = downloadUrl;
   }

   public void clearUpdateAvailable() {
      this.updateLatestVersion = null;
      this.updateDownloadUrl = null;
   }

   public boolean hasUpdateAvailable() {
      return this.updateLatestVersion != null && !this.updateLatestVersion.isBlank();
   }

   public String getUpdateLatestVersion() {
      return this.updateLatestVersion;
   }

   public String getUpdateDownloadUrl() {
      return this.updateDownloadUrl != null ? this.updateDownloadUrl : "https://modrinth.com/plugin/donut-orders";
   }

   private void registerStartupOrdersCommand() {
      this.registerStartupCommand("orders");
      this.registerStartupCommand("order");
      this.registerStartupCommand("orderadmin");
   }

   private void registerStartupCommand(String name) {
      PluginCommand cmd = this.getCommand(name);
      if (cmd == null) {
         this.getLogger().severe("[DonutOrders] /" + name + " command was not found in plugin.yml.");
      } else {
         cmd.setExecutor((sender, command, label, args) -> {
            sender.sendMessage(MessageHelper.get("plugin-starting", "&eᴅᴏɴᴜᴛᴏʀᴅᴇʀꜱ ɪꜱ ꜱᴛɪʟʟ ꜱᴛᴀʀᴛɪɴɢ. ᴘʟᴇᴀꜱᴇ ᴛʀʏ ᴀɢᴀɪɴ ɪɴ ᴀ ᴍᴏᴍᴇɴᴛ."));
            return true;
         });
         cmd.setTabCompleter((sender, command, alias, args) -> Collections.emptyList());
      }
   }

   private void registerCommand(String name, Object executor) {
      PluginCommand cmd = this.getCommand(name);
      if (cmd == null) {
         this.getLogger().severe("[DonutOrders] /" + name + " command was not found in plugin.yml.");
      } else {
         if (executor instanceof CommandExecutor commandExecutor) {
            cmd.setExecutor(commandExecutor);
         }

         if (executor instanceof TabCompleter tabCompleter) {
            cmd.setTabCompleter(tabCompleter);
         }
      }
   }

   public void onDisable() {
      if (this.guiManager != null) {
         for (Player player : Bukkit.getOnlinePlayers()) {
            try {
               this.guiManager.returnHeldDeliveryItems(player);
               player.closeInventory();
            } catch (Throwable var4) {
               this.getLogger().log(Level.WARNING, "[DonutOrders] Failed to return delivery items for " + player.getName(), var4);
            }
         }
      }

      if (this.storageManager != null) {
         this.storageManager.close();
      }

      this.getLogger().info("[DonutOrders] Disabled. Database connection closed.");
   }

   public void reloadPluginConfig() {
      this.reloadConfig();
      this.reloadMessages();
      EnchantOrderUtils.clearBookEnchantmentCache();
      if (this.allowedItemsManager != null) {
         this.allowedItemsManager.reload();
      }

      if (this.orderLimitManager != null) {
         this.orderLimitManager.refreshAllOnline();
      }
   }

   private void reloadMessages() {
      this.saveResourceIfAbsent("messages.yml");
      File file = new File(this.getDataFolder(), "messages.yml");
      this.messages = YamlConfiguration.loadConfiguration(file);
      InputStream defStream = this.getResource("messages.yml");
      if (defStream != null) {
         YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
         this.messages.setDefaults(defaults);
         this.messages.options().copyDefaults(false);
      }
   }

   public FileConfiguration getMessages() {
      return this.messages;
   }

   private void saveResourceIfAbsent(String resourceName) {
      File target = new File(this.getDataFolder(), resourceName);
      if (!target.exists()) {
         this.saveResource(resourceName, false);
      }
   }

   private boolean setupEconomy() {
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

   public static DonutOrders getInstance() {
      return instance;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public AllowedItemsManager getAllowedItemsManager() {
      return this.allowedItemsManager;
   }

   public OrderLimitManager getOrderLimitManager() {
      return this.orderLimitManager;
   }

   public boolean isPluginReady() {
      return this.pluginReady;
   }

   public static String colorize(String text) {
      if (text == null) {
         return "";
      } else {
         text = translateHex(text);
         return ChatColor.translateAlternateColorCodes('&', text);
      }
   }

   private static String translateHex(String text) {
      Matcher matcher = Pattern.compile("#[a-fA-F0-9]{6}").matcher(text);
      StringBuilder sb = new StringBuilder();

      while (matcher.find()) {
         String hex = matcher.group();
         StringBuilder replacement = new StringBuilder("§x");

         for (char c : hex.substring(1).toCharArray()) {
            replacement.append('§').append(c);
         }

         matcher.appendReplacement(sb, replacement.toString());
      }

      matcher.appendTail(sb);
      return sb.toString();
   }
}
