package me.byteful.plugin.leveltools;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import me.byteful.plugin.leveltools.api.AnvilCombineMode;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockDataManagerFactory;
import me.byteful.plugin.leveltools.api.event.LevelToolsLoadEvent;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import me.byteful.plugin.leveltools.api.trigger.TriggerRegistry;
import me.byteful.plugin.leveltools.config.ConfigManager;
import me.byteful.plugin.leveltools.libs.bstats.bukkit.Metrics;
import me.byteful.plugin.leveltools.libs.crunch.CompiledExpression;
import me.byteful.plugin.leveltools.libs.crunch.Crunch;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.BukkitCommandHandler;
import me.byteful.plugin.leveltools.listeners.BlockPlacementListener;
import me.byteful.plugin.leveltools.listeners.TriggerListener;
import me.byteful.plugin.leveltools.listeners.anvil.AnvilListener;
import me.byteful.plugin.leveltools.listeners.anvil.LegacyAnvilListener;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import me.byteful.plugin.leveltools.util.Text;
import me.byteful.plugin.leveltools.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LevelToolsPlugin extends JavaPlugin {
   private static LevelToolsPlugin instance;
   private BukkitCommandHandler commandManager;
   private AnvilCombineMode anvilCombineMode;
   private UpdateChecker updateChecker;
   private CompiledExpression levelXpFormula;
   private Metrics metrics;
   private BlockDataManager blockDataManager;
   private Scheduler scheduler;
   private ConfigManager configManager;
   private ProfileManager profileManager;
   private TriggerRegistry triggerRegistry;

   public static LevelToolsPlugin getInstance() {
      return instance;
   }

   public void onEnable() {
      this.sendStartupBanner();
      instance = this;
      this.scheduler = LevelToolsUtil.createScheduler(this);
      this.updateChecker = new UpdateChecker(this, this.scheduler);
      this.configManager = new ConfigManager(this);
      this.configManager.loadAll();
      this.setAnvilCombineMode();
      this.setLevelXpFormula();
      this.getLogger().info("Loaded configuration...");
      this.profileManager = new ProfileManager(this.getLogger());
      this.triggerRegistry = new TriggerRegistry();
      Bukkit.getPluginManager().callEvent(new LevelToolsLoadEvent(this, LevelToolsLoadEvent.LoadPhase.PRE_LOAD));
      this.triggerRegistry.registerDefaults();
      this.getLogger().info("Registered triggers...");
      Bukkit.getPluginManager().callEvent(new LevelToolsLoadEvent(this, LevelToolsLoadEvent.LoadPhase.POST_TRIGGERS));
      this.loadProfiles();
      this.getLogger().info("Loaded profiles...");
      Bukkit.getPluginManager().callEvent(new LevelToolsLoadEvent(this, LevelToolsLoadEvent.LoadPhase.POST_PROFILES));
      this.blockDataManager = BlockDataManagerFactory.createBlockDataManager(this.getDataFolder().toPath(), this.getConfig(), this.scheduler);
      this.blockDataManager.load();
      this.getLogger().info("Loaded block data manager...");
      if (this.getConfig().getBoolean("update.start")) {
         this.updateChecker.check();
      }

      if (this.getConfig().getBoolean("update.periodically")) {
         long delay = 20L * TimeUnit.DAYS.toSeconds(1L);
         this.scheduler.syncTimer(() -> this.updateChecker.check(), delay, delay);
      }

      this.registerListeners();
      this.getLogger().info("Registered listeners...");
      this.commandManager = BukkitCommandHandler.create(this);
      this.commandManager
         .setHelpWriter(
            (command, actor) -> String.format("&7- &b/%s %s&7: &e%s", command.getPath().toRealString(), command.getUsage(), command.getDescription())
         );
      this.commandManager.register(new Object[]{new LevelToolsCommand()});
      this.commandManager.registerBrigadier();
      this.getLogger().info("Registered commands...");
      if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         new LevelToolsPlaceholders().register();
      }

      this.metrics = new Metrics(this, 21451);
      Bukkit.getPluginManager().callEvent(new LevelToolsLoadEvent(this, LevelToolsLoadEvent.LoadPhase.COMPLETE));
      this.getLogger().info("Successfully started " + this.getDescription().getFullName() + "!");
   }

   public void onDisable() {
      if (this.metrics != null) {
         this.metrics.shutdown();
      }

      if (this.blockDataManager != null) {
         try {
            this.blockDataManager.close();
         } catch (IOException var2) {
            throw new RuntimeException(var2);
         }
      }

      instance = null;
      this.getLogger().info("Successfully stopped " + this.getDescription().getFullName() + ".");
   }

   private void sendStartupBanner() {
      Bukkit.getConsoleSender().sendMessage(Text.colorize(" &b         _____"));
      Bukkit.getConsoleSender().sendMessage(Text.colorize(" &d|          &b|     &8Created by &2byteful"));
      Bukkit.getConsoleSender()
         .sendMessage(
            Text.colorize(
               String.format(" &d|          &b|     &8Running &6%s &8on &6MC %s", this.getDescription().getFullName(), LevelToolsUtil.getServerVersion())
            )
         );
      Bukkit.getConsoleSender().sendMessage(Text.colorize(" &d|_____     &b|     &8Join &9&nhttps://discord.gg/G8BDgqsuyw&8 for support!"));
      Bukkit.getConsoleSender().sendMessage("");
   }

   private void loadProfiles() {
      this.profileManager
         .load(
            this.configManager.getTriggerProfilesConfig(),
            this.configManager.getRewardProfilesConfig(),
            this.configManager.getDisplayProfilesConfig(),
            this.configManager.getItemProfilesConfig()
         );
   }

   private void registerListeners() {
      PluginManager pm = Bukkit.getPluginManager();
      pm.registerEvents(new BlockPlacementListener(this.blockDataManager, this.scheduler), this);
      pm.registerEvents(new TriggerListener(this.profileManager, this.triggerRegistry, this.blockDataManager), this);
      if (LevelToolsUtil.requiresLegacyAnvilListener()) {
         pm.registerEvents(new LegacyAnvilListener(), this);
      } else {
         pm.registerEvents(new AnvilListener(), this);
      }
   }

   public void reloadPlugin() {
      Bukkit.getPluginManager().callEvent(new LevelToolsLoadEvent(this, LevelToolsLoadEvent.LoadPhase.PRE_LOAD));
      this.configManager.reload();
      this.setAnvilCombineMode();
      this.setLevelXpFormula();
      Bukkit.getPluginManager().callEvent(new LevelToolsLoadEvent(this, LevelToolsLoadEvent.LoadPhase.POST_TRIGGERS));
      this.loadProfiles();
      Bukkit.getPluginManager().callEvent(new LevelToolsLoadEvent(this, LevelToolsLoadEvent.LoadPhase.POST_PROFILES));
      Bukkit.getPluginManager().callEvent(new LevelToolsLoadEvent(this, LevelToolsLoadEvent.LoadPhase.COMPLETE));
      this.getLogger().info("Reloaded LevelTools!");
   }

   public void setAnvilCombineMode() {
      this.anvilCombineMode = AnvilCombineMode.fromName(Objects.requireNonNull(this.getConfig().getString("anvil_combine")));
   }

   public void setLevelXpFormula() {
      this.levelXpFormula = Crunch.compileExpression(this.getConfig().getString("level_xp_formula").replace("{current_level}", "$1"));
   }

   public AnvilCombineMode getAnvilCombineMode() {
      return this.anvilCombineMode;
   }

   public CompiledExpression getLevelXpFormula() {
      return this.levelXpFormula;
   }

   public BukkitCommandHandler getCommandManager() {
      return this.commandManager;
   }

   public UpdateChecker getUpdateChecker() {
      return this.updateChecker;
   }

   public Scheduler getScheduler() {
      return this.scheduler;
   }

   public BlockDataManager getBlockDataManager() {
      return this.blockDataManager;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public ProfileManager getProfileManager() {
      return this.profileManager;
   }

   public TriggerRegistry getTriggerRegistry() {
      return this.triggerRegistry;
   }
}
