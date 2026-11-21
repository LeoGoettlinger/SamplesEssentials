package com.oheers.fish;

import com.devskiller.friendly_id.FriendlyId;
import com.oheers.fish.api.EMFAPI;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.selling.SoldFish;
import com.oheers.fish.api.events.EMFPluginReloadEvent;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.commands.admin.AdminCommand;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.DimensionFishingConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.manager.DataManager;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.events.McMMOTreasureEvent;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.rods.RodManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.plugin.ConfigurationManager;
import com.oheers.fish.plugin.DependencyManager;
import com.oheers.fish.plugin.EventManager;
import com.oheers.fish.plugin.IntegrationManager;
import com.oheers.fish.plugin.MetricsManager;
import com.oheers.fish.plugin.PluginDataManager;
import com.oheers.fish.plugin.loading.EMFVersionLoader;
import com.oheers.fish.plugin.loading.EMFVersionProvider;
import com.oheers.fish.update.UpdateChecker;
import com.oheers.fish.utils.MinecraftVersionHelper;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.evenmorefish.fish.libs.daisylib.DaisyLib;
import org.evenmorefish.fish.libs.dimensionfishing.DimensionFishing;
import org.evenmorefish.fish.libs.inventorygui.InventoryGui;
import org.evenmorefish.fish.libs.vanishchecker.VanishChecker;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EvenMoreFish extends EMFPlugin {
   private final EMFVersionLoader loader;
   private final EMFVersionProvider versionProvider;
   private final DimensionFishing dimensionFishing;
   public static final Random RANDOM = new Random();
   private final Toggle toggle;
   private final boolean isFolia = FishUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer");
   private volatile boolean isUpdateAvailable;
   private DependencyManager dependencyManager;
   private ConfigurationManager configurationManager;
   private PluginDataManager pluginDataManager;
   private IntegrationManager integrationManager;
   private EventManager eventManager;
   private MetricsManager metricsManager;
   private static EvenMoreFish instance;
   private EMFAPI api;

   @NonNull
   public static EvenMoreFish getInstance() {
      if (instance == null) {
         throw new IllegalStateException("Plugin not initialized yet!");
      } else {
         return instance;
      }
   }

   public EvenMoreFish() {
      this.loader = new EMFVersionLoader(this, this.getClassLoader());
      this.versionProvider = this.loader.getVersionProvider();
      this.toggle = new Toggle(this);
      if (MinecraftVersionHelper.isAtLeastVersion("1.21.1") && !this.isFolia) {
         this.dimensionFishing = new DimensionFishing(this, DimensionFishingConfig.getInstance());
      } else {
         this.dimensionFishing = null;
      }
   }

   public void onLoad() {
      instance = this;
      this.loadCommands();
      this.versionProvider.load();
      if (this.dimensionFishing != null) {
         this.dimensionFishing.load();
      }
   }

   public void onEnable() {
      DaisyLib.get().init(this);
      this.api = new EMFAPI();
      this.dependencyManager = new DependencyManager(this);
      this.dependencyManager.loadBundledDependencies();
      this.configurationManager = new ConfigurationManager(this);
      this.configurationManager.loadConfigurations();
      this.dependencyManager.checkDependencies();
      this.integrationManager = new IntegrationManager(this);
      this.integrationManager.loadAddons();
      this.pluginDataManager = new PluginDataManager(this);
      this.eventManager = new EventManager(this);
      this.eventManager.registerCoreListeners();
      this.eventManager.registerOptionalListeners();
      FishManager.getInstance().load();
      BaitManager.getInstance().load();
      RodManager.getInstance().load();
      CompetitionManager.getInstance().load();
      new UpdateChecker(this).checkUpdate().thenAccept(available -> {
         this.isUpdateAvailable = available;
         if (available) {
            this.getLogger().warning("A new update is available! Download it from https://modrinth.com/plugin/evenmorefish");
         }
      });
      this.metricsManager = new MetricsManager(this);
      this.metricsManager.setupMetrics();
      CompetitionManager.getInstance().getAutoRunner().start();
      this.versionProvider.enable();
      if (this.dimensionFishing != null) {
         this.dimensionFishing.enable();
         this.integrationManager.setupDimensionFishing();
      }

      CompetitionManager.getInstance().resumeFromFile();
      this.getLogger().info(() -> "EvenMoreFish by Oheers : Enabled");
   }

   public void onDisable() {
      CompetitionManager.getInstance().getAutoRunner().stop();
      if (this.dimensionFishing != null) {
         this.dimensionFishing.disable();
      }

      this.terminateGuis();
      Competition active = CompetitionManager.getInstance().getActiveCompetition();
      if (active != null) {
         active.end(false, true);
      }

      if (this.pluginDataManager != null) {
         this.pluginDataManager.shutdown();
      }

      CompetitionManager.getInstance().unload();
      RodManager.getInstance().unload();
      BaitManager.getInstance().unload();
      FishManager.getInstance().unload();
      this.integrationManager.unloadAddons();
      this.loader.onDisable();
      this.getLogger().info(() -> "EvenMoreFish by Oheers : Disabled");
   }

   @Override
   public boolean isDebugSession() {
      return MainConfig.getInstance().shouldDebug();
   }

   private void terminateGuis() {
      this.getServer().getOnlinePlayers().forEach(player -> {
         InventoryGui inventoryGui = InventoryGui.getOpen(player);
         if (inventoryGui != null) {
            inventoryGui.close();
         }
      });
   }

   @Override
   public void reload(@Nullable CommandSender sender) {
      this.terminateGuis();
      this.configurationManager.reloadConfigurations();
      FishManager.getInstance().reload();
      BaitManager.getInstance().reload();
      RodManager.getInstance().reload();
      HandlerList.unregisterAll(McMMOTreasureEvent.getInstance());
      this.eventManager.registerOptionalListeners();
      CompetitionManager.getInstance().reload();
      Economy.getInstance().setEconomyTypes(EMFRegistry.ECONOMY_TYPE.getRegistry().values());
      if (sender != null) {
         ConfigMessage.RELOAD_SUCCESS.getMessage().send(sender);
      }

      this.resendCommands();
      this.versionProvider.reload();
      if (this.dimensionFishing != null) {
         this.dimensionFishing.reload(sender);
      }

      new EMFPluginReloadEvent().callEvent();
   }

   public Toggle getToggle() {
      return this.toggle;
   }

   public EMFVersionProvider getVersionProvider() {
      return this.versionProvider;
   }

   public boolean isUpdateAvailable() {
      return this.isUpdateAvailable;
   }

   @Deprecated(
      forRemoval = true
   )
   public EMFAPI getApi() {
      return this.api;
   }

   public List<Player> getVisibleOnlinePlayers() {
      return MainConfig.getInstance().shouldRespectVanish() ? VanishChecker.getVisibleOnlinePlayers() : List.copyOf(Bukkit.getOnlinePlayers());
   }

   public DependencyManager getDependencyManager() {
      return this.dependencyManager;
   }

   public PluginDataManager getPluginDataManager() {
      return this.pluginDataManager;
   }

   public EventManager getEventManager() {
      return this.eventManager;
   }

   public MetricsManager getMetricsManager() {
      return this.metricsManager;
   }

   @Nullable
   public DimensionFishing getDimensionFishing() {
      return this.dimensionFishing;
   }

   @Override
   public boolean isRunningOnFolia() {
      return this.isFolia;
   }

   @Override
   public void sendMessage(@NonNull String id, @NonNull Player player) {
      try {
         ConfigMessage message = ConfigMessage.valueOf(id.toUpperCase(Locale.ROOT));
         message.send(player);
      } catch (IllegalArgumentException var4) {
         Logging.warn("Invalid message id " + id, var4);
      }
   }

   @Override
   public void logSoldFish(@NonNull SoldFish sold) {
      if (DatabaseUtil.isDatabaseOnline() && sold.getPlayer() != null) {
         UUID uuid = sold.getPlayer().getUniqueId();
         String transactionId = FriendlyId.createFriendlyId();
         Timestamp timestamp = Timestamp.from(Instant.now());
         IFish fish = sold.getFish();
         String fishName = fish.getId();
         String rarityId = fish.getRarity().getId();
         int quantity = sold.getQuantity();
         float length = fish.getLength();
         double finalValue = sold.getFinalValue();
         double rawValue = sold.getValue();
         this.pluginDataManager.getDatabaseWorker().execute(() -> {
            int userId = this.pluginDataManager.getUserManager().getUserId(uuid);
            if (userId == 0) {
               this.getLogger().warning("Skipping sold fish database update because user id could not be resolved for " + uuid);
            } else {
               Database database = this.pluginDataManager.getDatabase();
               database.createTransaction(transactionId, userId, timestamp);
               database.createSale(transactionId, fishName, rarityId, quantity, length, finalValue);
               DataManager<UserReport> userReportDataManager = this.pluginDataManager.getUserReportDataManager();
               UserReport report = userReportDataManager.get(uuid.toString());
               if (report == null) {
                  this.getLogger().warning("Skipping sold fish report update because user report could not be loaded for " + uuid);
               } else {
                  report.incrementFishSold(quantity);
                  report.incrementMoneyEarned(rawValue);
                  userReportDataManager.update(uuid.toString(), report);
               }
            }
         });
      }
   }

   @Override
   public void sendSoldMessage(double value, int count, @NonNull Player player) {
      EMFMessage message = ConfigMessage.FISH_SALE.getMessage();
      message.setSellPrice(Economy.getInstance().getWorthFormat(value, true));
      message.setAmount(count);
      message.setPlayer(player);
      message.send(player);
   }

   public void loadCommands() {
      this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
         ((Commands)event.registrar()).register(new MainCommand().get(), MainConfig.getInstance().getMainCommandAliases());
         if (MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
            String shortcut = MainConfig.getInstance().getAdminShortcutCommandName();
            ((Commands)event.registrar()).register(new AdminCommand(shortcut).get());
         }
      }));
   }

   public void resendCommands() {
      Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
   }
}
