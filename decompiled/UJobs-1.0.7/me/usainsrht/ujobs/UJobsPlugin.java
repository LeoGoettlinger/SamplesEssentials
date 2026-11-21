package me.usainsrht.ujobs;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;
import lombok.Generated;
import me.usainsrht.ujobs.bstats.bukkit.Metrics;
import me.usainsrht.ujobs.commands.MainCommand;
import me.usainsrht.ujobs.listeners.InventoryListener;
import me.usainsrht.ujobs.listeners.JoinListener;
import me.usainsrht.ujobs.listeners.QuitListener;
import me.usainsrht.ujobs.listeners.SaveListener;
import me.usainsrht.ujobs.listeners.job_actions.AnvilMergeListener;
import me.usainsrht.ujobs.listeners.job_actions.BreakListener;
import me.usainsrht.ujobs.listeners.job_actions.BreedListener;
import me.usainsrht.ujobs.listeners.job_actions.CraftListener;
import me.usainsrht.ujobs.listeners.job_actions.EnchantListener;
import me.usainsrht.ujobs.listeners.job_actions.FishListener;
import me.usainsrht.ujobs.listeners.job_actions.HarvestListener;
import me.usainsrht.ujobs.listeners.job_actions.KillListener;
import me.usainsrht.ujobs.listeners.job_actions.LootGenerateListener;
import me.usainsrht.ujobs.listeners.job_actions.PlaceListener;
import me.usainsrht.ujobs.listeners.job_actions.RaidListener;
import me.usainsrht.ujobs.listeners.job_actions.SmeltListener;
import me.usainsrht.ujobs.listeners.job_actions.TameListener;
import me.usainsrht.ujobs.listeners.job_actions.TradeListener;
import me.usainsrht.ujobs.listeners.job_actions.timber.TreeFallListener;
import me.usainsrht.ujobs.managers.BossBarManager;
import me.usainsrht.ujobs.managers.ConfigManager;
import me.usainsrht.ujobs.managers.JobGUIManager;
import me.usainsrht.ujobs.managers.JobManager;
import me.usainsrht.ujobs.managers.LeaderboardManager;
import me.usainsrht.ujobs.managers.NPCManager;
import me.usainsrht.ujobs.models.BuiltInActions;
import me.usainsrht.ujobs.morepaperlib.MorePaperLib;
import me.usainsrht.ujobs.placeholders.JobPlaceholders;
import me.usainsrht.ujobs.storage.DatabaseStorage;
import me.usainsrht.ujobs.storage.PDCStorage;
import me.usainsrht.ujobs.storage.Storage;
import me.usainsrht.ujobs.yaml.YamlCommand;
import me.usainsrht.ujobs.yaml.YamlMessage;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class UJobsPlugin extends JavaPlugin {
   private int persistentDataStore;
   public static volatile UJobsPlugin instance;
   private MiniMessage miniMessage;
   private Economy economy;
   private Storage storage;
   private MorePaperLib morePaperLib;
   private ConfigManager configManager;
   private JobManager jobManager;
   private LeaderboardManager leaderboardManager;
   private JobGUIManager guiManager;
   private BossBarManager bossBarManager;
   private NPCManager npcManager;

   public void onEnable() {
      instance = this;
      this.miniMessage = MiniMessage.miniMessage();
      this.morePaperLib = new MorePaperLib(this);
      this.saveDefaultConfig();
      this.setupEconomy();
      this.initializeStorage();
      this.initializeManagers();
      this.registerEventsAndCommands();
      this.registerPlaceholders();
      int pluginId = 30282;
      new Metrics(this, pluginId);
      this.getLogger().info("UJobs has been enabled successfully!");
   }

   public void onDisable() {
      if (this.storage != null) {
         if (this.storage instanceof PDCStorage pdcStorage) {
            pdcStorage.saveNowAll();
         }

         this.storage.save();
         this.storage.close();
      }

      if (this.leaderboardManager != null) {
         this.leaderboardManager.save();
      }

      if (this.configManager != null) {
         this.configManager.saveLeaderboard();
      }

      if (this.bossBarManager != null) {
         this.bossBarManager.removeAllBossBars();
      }

      this.getLogger().info("UJobs has been disabled!");
   }

   private void initializeManagers() {
      try {
         this.configManager = new ConfigManager(this);
         this.jobManager = new JobManager(this);
         this.leaderboardManager = new LeaderboardManager(this);
         this.bossBarManager = new BossBarManager(this);
         this.guiManager = new JobGUIManager(this);
         this.npcManager = new NPCManager(this);
         this.configManager.loadConfigs();
         this.jobManager.loadJobs();
         this.leaderboardManager.load(this.configManager.getLeaderboardConfig());
         this.getLogger().info("All managers initialized successfully!");
      } catch (Exception var2) {
         this.getLogger().log(Level.SEVERE, "Failed to initialize managers", (Throwable)var2);
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   private void registerEventsAndCommands() {
      this.getServer().getPluginManager().registerEvents(new InventoryListener(), this);
      this.getServer().getPluginManager().registerEvents(new JoinListener(this), this);
      this.getServer().getPluginManager().registerEvents(new QuitListener(this), this);
      this.getServer().getPluginManager().registerEvents(new SaveListener(this), this);
      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.BREAK)) {
         this.getServer().getPluginManager().registerEvents(new BreakListener(this.jobManager), this);
         if (this.getServer().getPluginManager().isPluginEnabled("UltimateTimber")) {
            this.getServer().getPluginManager().registerEvents(new TreeFallListener(this.jobManager), this);
         }
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.PLACE)) {
         this.getServer().getPluginManager().registerEvents(new PlaceListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Entity.TAME)) {
         this.getServer().getPluginManager().registerEvents(new TameListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Entity.BREED)) {
         this.getServer().getPluginManager().registerEvents(new BreedListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Entity.KILL)) {
         this.getServer().getPluginManager().registerEvents(new KillListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Special.ENCHANT)) {
         this.getServer().getPluginManager().registerEvents(new EnchantListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.FISH)) {
         this.getServer().getPluginManager().registerEvents(new FishListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Special.RAID)) {
         this.getServer().getPluginManager().registerEvents(new RaidListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Special.GENERATE_LOOT)) {
         this.getServer().getPluginManager().registerEvents(new LootGenerateListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.TRADE)) {
         this.getServer().getPluginManager().registerEvents(new TradeListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.ANVIL_MERGE)) {
         this.getServer().getPluginManager().registerEvents(new AnvilMergeListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.CRAFT)) {
         this.getServer().getPluginManager().registerEvents(new CraftListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.HARVEST)) {
         this.getServer().getPluginManager().registerEvents(new HarvestListener(this.jobManager), this);
      }

      if (this.jobManager.getActionJobMap().containsKey(BuiltInActions.Material.SMELT)) {
         this.getServer().getPluginManager().registerEvents(new SmeltListener(this.jobManager), this);
      }

      LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
      manager.registerEventHandler(
         LifecycleEvents.COMMANDS,
         event -> {
            Commands commands = (Commands)event.registrar();
            YamlCommand mainCommand = YamlCommand.builder()
               .name(this.getConfig().getString("command.name", "ujobs"))
               .description(this.getConfig().getString("command.description", "UJobs main command"))
               .aliases(this.getConfig().getStringList("command.aliases"))
               .permission(this.getConfig().getString("command.permission"))
               .permissionMessage(new YamlMessage(this.getConfig().get("command.permission_message")))
               .build();
            commands.register(this.getPluginMeta(), MainCommand.create(this, mainCommand), mainCommand.getDescription(), mainCommand.getAliases());
         }
      );
   }

   private void setupEconomy() {
      this.persistentDataStore = this.setupPersistentDataStore();
      if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return;
         }

         this.economy = (Economy)rsp.getProvider();
      }
   }

   private void initializeStorage() {
      String storageType = this.getConfig().getString("storage.type", "pdc").toLowerCase(Locale.ROOT);

      try {
         switch (storageType) {
            case "sqlite":
            case "mysql":
            case "postgresql":
               if (!this.hasDatabaseLibraries()) {
                  this.getLogger().severe("Database libraries are missing from classpath. Falling back to pdc storage.");
                  this.getLogger().severe("Expected classes: com.zaxxer.hikari.HikariConfig and selected JDBC driver.");
                  this.storage = new PDCStorage(this);
                  return;
               }

               this.storage = new DatabaseStorage(this, storageType);
               this.getLogger().info("Using " + storageType + " storage backend.");
               break;
            case "pdc":
               this.storage = new PDCStorage(this);
               this.getLogger().info("Using pdc storage backend.");
               break;
            default:
               this.getLogger().warning("Unknown storage type '" + storageType + "'. Falling back to pdc.");
               this.storage = new PDCStorage(this);
         }
      } catch (Throwable var4) {
         this.getLogger().log(Level.SEVERE, "Failed to initialize storage backend '" + storageType + "', falling back to pdc", var4);
         this.storage = new PDCStorage(this);
      }
   }

   private boolean hasDatabaseLibraries() {
      return this.hasClass("com.zaxxer.hikari.HikariConfig")
         && (this.hasClass("org.sqlite.JDBC") || this.hasClass("com.mysql.cj.jdbc.Driver") || this.hasClass("org.postgresql.Driver"));
   }

   private boolean hasClass(String className) {
      try {
         Class.forName(className, false, this.getClassLoader());
         return true;
      } catch (ClassNotFoundException var3) {
         return false;
      }
   }

   private void registerPlaceholders() {
      if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         new JobPlaceholders(this).register();
      }
   }

   private void startLeaderboardTimer() {
      this.morePaperLib.scheduling().asyncScheduler().runAtFixedRate(() -> {}, Duration.ofMinutes(2L), Duration.ofHours(1L));
   }

   @Generated
   public MiniMessage getMiniMessage() {
      return this.miniMessage;
   }

   @Generated
   public Economy getEconomy() {
      return this.economy;
   }

   @Generated
   public Storage getStorage() {
      return this.storage;
   }

   @Generated
   public MorePaperLib getMorePaperLib() {
      return this.morePaperLib;
   }

   @Generated
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public JobManager getJobManager() {
      return this.jobManager;
   }

   @Generated
   public LeaderboardManager getLeaderboardManager() {
      return this.leaderboardManager;
   }

   @Generated
   public JobGUIManager getGuiManager() {
      return this.guiManager;
   }

   @Generated
   public BossBarManager getBossBarManager() {
      return this.bossBarManager;
   }

   @Generated
   public NPCManager getNpcManager() {
      return this.npcManager;
   }
}
