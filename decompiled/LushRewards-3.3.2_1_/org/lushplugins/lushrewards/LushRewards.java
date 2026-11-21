package org.lushplugins.lushrewards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.util.FileUtil;
import org.lushplugins.lushrewards.command.RewardsCommand;
import org.lushplugins.lushrewards.config.ConfigManager;
import org.lushplugins.lushrewards.data.DataManager;
import org.lushplugins.lushrewards.hook.FloodgateHook;
import org.lushplugins.lushrewards.hook.PlaceholderAPIHook;
import org.lushplugins.lushrewards.libraries.bstats.bukkit.Metrics;
import org.lushplugins.lushrewards.libraries.lushlib.LushLib;
import org.lushplugins.lushrewards.libraries.lushlib.manager.Manager;
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;
import org.lushplugins.lushrewards.libraries.lushlib.plugin.SpigotPlugin;
import org.lushplugins.lushrewards.libraries.lushlib.utils.Updater;
import org.lushplugins.lushrewards.libraries.morepaperlib.MorePaperLib;
import org.lushplugins.lushrewards.listener.RewardUserListener;
import org.lushplugins.lushrewards.module.RewardModule;
import org.lushplugins.lushrewards.module.RewardModuleTypeManager;
import org.lushplugins.lushrewards.module.playtimerewards.PlaytimeRewardsModule;
import org.lushplugins.lushrewards.module.playtimetracker.PlaytimeTrackerModule;
import org.lushplugins.lushrewards.notifications.NotificationHandler;
import org.lushplugins.lushrewards.rewards.RewardManager;
import org.lushplugins.lushrewards.storage.migrator.Version3DataMigrator;
import org.lushplugins.lushrewards.utils.gson.LocalDateTypeAdapter;
import org.lushplugins.lushrewards.utils.gson.UserDataExclusionStrategy;
import org.lushplugins.lushrewards.utils.placeholder.LocalPlaceholders;

public final class LushRewards extends SpigotPlugin {
   private int permissionNodes;
   private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
      .addSerializationExclusionStrategy(new UserDataExclusionStrategy())
      .create();
   private static LushRewards plugin;
   private static MorePaperLib morePaperLib;
   private ConfigManager configManager;
   private DataManager dataManager;
   private NotificationHandler notificationHandler;
   private LocalPlaceholders localPlaceholders;
   private Updater updater;

   public void onLoad() {
      plugin = this;
      morePaperLib = new MorePaperLib(plugin);
      LushLib.getInstance().enable(this);
      this.registerManager(new Manager[]{new RewardModuleTypeManager(), new RewardManager()});
   }

   public void onEnable() {
      this.permissionNodes = this.loadPermissionNodes();
      File oldDataFolder = new File(this.getDataFolder().getParentFile(), "ActivityRewarder");
      if (!this.getDataFolder().exists() && oldDataFolder.exists()) {
         if (FileUtil.copy(oldDataFolder, this.getDataFolder())) {
            File dataFolder = new File(this.getDataFolder(), "data");

            for (File file : dataFolder.listFiles()) {
               file.delete();
            }
         }

         this.getLogger().info("Importing data from 'ActivityRewarder', this could take a moment");
         long start = Instant.now().toEpochMilli();

         try {
            new Version3DataMigrator().convert();
         } catch (FileNotFoundException var7) {
            this.getLogger().severe("Failed to import data");
         }

         this.getLogger().info("Finished importing data (took " + (Instant.now().toEpochMilli() - start) + "ms)");
      }

      this.updater = new Updater(this, "djC8I9ui", "lushrewards.update", "rewards update");
      this.notificationHandler = new NotificationHandler();
      this.localPlaceholders = new LocalPlaceholders();
      this.configManager = new ConfigManager();
      this.configManager.reloadConfig();
      this.dataManager = new DataManager();
      this.dataManager.enable();
      this.addHook("floodgate", () -> this.registerHook(new FloodgateHook()));
      this.addHook("PlaceholderAPI", () -> this.registerHook(new PlaceholderAPIHook()));
      this.getHooks().forEach(Module::enable);
      this.registerListener(new RewardUserListener());
      this.registerCommand(new RewardsCommand());
      this.getModule("playtime-tracker").ifPresent(module -> {
         if (module instanceof PlaytimeTrackerModule playtimeTracker) {
            Bukkit.getOnlinePlayers().forEach(playtimeTracker::startPlaytimeTracker);
         }
      });
      morePaperLib.scheduling().asyncScheduler().runAtFixedRate(() -> {
         for (RewardModule module : getInstance().getEnabledRewardModules()) {
            if (module instanceof PlaytimeRewardsModule playtimeModule) {
               playtimeModule.checkAllOnlineForReset();
            }
         }
      }, Duration.of(1L, ChronoUnit.MINUTES), Duration.of(1L, ChronoUnit.MINUTES));
      new Metrics(this, 22119);
   }

   public void onDisable() {
      if (this.updater != null) {
         this.updater.shutdown();
         this.updater = null;
      }

      if (this.notificationHandler != null) {
         this.notificationHandler.stopNotificationTask();
         this.notificationHandler = null;
      }

      if (this.hooks != null) {
         this.unregisterAllHooks();
         this.hooks = null;
      }

      if (this.modules != null) {
         this.unregisterAllModules();
         this.modules = null;
      }

      if (this.dataManager != null) {
         this.dataManager.disable();
         this.dataManager = null;
      }

      this.configManager = null;
      this.localPlaceholders = null;
      morePaperLib.scheduling().cancelGlobalTasks();
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public DataManager getDataManager() {
      return this.dataManager;
   }

   public NotificationHandler getNotificationHandler() {
      return this.notificationHandler;
   }

   public LocalPlaceholders getLocalPlaceholders() {
      return this.localPlaceholders;
   }

   public Updater getUpdater() {
      return this.updater;
   }

   public Gson getGson() {
      return GSON;
   }

   public List<RewardModule> getRewardModules() {
      return this.modules.values().stream().filter(module -> module instanceof RewardModule).map(module -> (RewardModule)module).toList();
   }

   public List<RewardModule> getEnabledRewardModules() {
      return this.modules.values().stream().filter(module -> module instanceof RewardModule && module.isEnabled()).map(module -> (RewardModule)module).toList();
   }

   public static LushRewards getInstance() {
      return plugin;
   }

   public static MorePaperLib getMorePaperLib() {
      return morePaperLib;
   }
}
