package com.Sw3ys.bigenderchest;

import com.Sw3ys.bigenderchest.commands.ECCommand;
import com.Sw3ys.bigenderchest.commands.ECSeeCommand;
import com.Sw3ys.bigenderchest.gui.SelectorGUI;
import com.Sw3ys.bigenderchest.listeners.DataListener;
import com.Sw3ys.bigenderchest.listeners.EnderChestListener;
import com.Sw3ys.bigenderchest.listeners.InventoryListener;
import com.Sw3ys.bigenderchest.listeners.SearchListener;
import com.Sw3ys.bigenderchest.manager.EnderChestManager;
import com.Sw3ys.bigenderchest.manager.LidAnimator;
import com.Sw3ys.bigenderchest.storage.ChestStore;
import com.Sw3ys.bigenderchest.storage.MySqlChestStore;
import com.Sw3ys.bigenderchest.storage.YamlChestStore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BiggerEnderChest extends JavaPlugin {
   private int pluginConfiguration;
   private static BiggerEnderChest instance;
   private ChestStore store;
   private EnderChestManager manager;
   private SelectorGUI selector;
   private SearchListener search;
   private LidAnimator animator;
   private final Map<UUID, Long> cooldowns = new HashMap<>();

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.store = this.createStore();
      this.manager = new EnderChestManager(this, this.store);
      this.selector = new SelectorGUI(this, this.manager);
      this.search = new SearchListener(this, this.manager);
      this.animator = new LidAnimator(this);
      PluginManager pm = this.getServer().getPluginManager();
      pm.registerEvents(new InventoryListener(this, this.manager, this.selector, this.animator), this);
      pm.registerEvents(this.search, this);
      pm.registerEvents(new EnderChestListener(this, this.manager, this.animator), this);
      pm.registerEvents(new DataListener(this.manager), this);
      this.getCommand("ec").setExecutor(new ECCommand(this.manager));
      this.getCommand("ecsee").setExecutor(new ECSeeCommand(this.selector, this.manager));
   }

   public void onDisable() {
      if (this.manager != null) {
         this.manager.flushAll();
      }

      if (this.store != null) {
         this.store.close();
      }
   }

   private ChestStore createStore() {
      this.pluginConfiguration = this.initializePluginConfiguration();
      String type = this.getConfig().getString("storage.type", "yaml").toLowerCase();
      if (type.equals("mysql")) {
         try {
            MySqlChestStore mysql = new MySqlChestStore(this);
            mysql.prepare();
            this.getLogger().info("Using MySQL storage backend (cross-server sync enabled).");
            return mysql;
         } catch (Throwable var3) {
            this.getLogger().severe("MySQL backend failed to start (" + var3.getMessage() + "); falling back to local YAML storage.");
         }
      }

      YamlChestStore yaml = new YamlChestStore(this);
      yaml.prepare();
      return yaml;
   }

   public static BiggerEnderChest get() {
      return instance;
   }

   public SearchListener getSearch() {
      return this.search;
   }

   public Map<UUID, Long> getCooldowns() {
      return this.cooldowns;
   }
}
