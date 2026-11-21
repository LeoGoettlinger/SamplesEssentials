package com.earth2me.essentials;

import com.earth2me.essentials.adventure.AdventureFacet;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.adventure.ComponentHolder;
import com.earth2me.essentials.adventure.PaperAdventureFacet;
import com.earth2me.essentials.adventure.SpigotAdventureFacet;
import com.earth2me.essentials.commands.EssentialsCommand;
import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.commands.NoChargeException;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import com.earth2me.essentials.commands.PlayerNotFoundException;
import com.earth2me.essentials.commands.QuietAbortException;
import com.earth2me.essentials.config.EssentialsConfiguration;
import com.earth2me.essentials.economy.EconomyLayers;
import com.earth2me.essentials.economy.vault.VaultEconomyProvider;
import com.earth2me.essentials.items.AbstractItemDb;
import com.earth2me.essentials.items.CustomItemResolver;
import com.earth2me.essentials.items.FlatItemDb;
import com.earth2me.essentials.items.LegacyItemDb;
import com.earth2me.essentials.metrics.MetricsWrapper;
import com.earth2me.essentials.paperlib.PaperLib;
import com.earth2me.essentials.perm.PermissionsDefaults;
import com.earth2me.essentials.perm.PermissionsHandler;
import com.earth2me.essentials.signs.SignBlockListener;
import com.earth2me.essentials.signs.SignEntityListener;
import com.earth2me.essentials.signs.SignPlayerListener;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.SimpleTextInput;
import com.earth2me.essentials.updatecheck.UpdateChecker;
import com.earth2me.essentials.userstorage.ModernUserMap;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.ess3.api.IItemDb;
import net.ess3.api.IJails;
import net.ess3.api.TranslatableException;
import net.ess3.nms.refl.providers.ReflDataWorldInfoProvider;
import net.ess3.nms.refl.providers.ReflFormattedCommandAliasProvider;
import net.ess3.nms.refl.providers.ReflKnownCommandsProvider;
import net.ess3.nms.refl.providers.ReflOnlineModeProvider;
import net.ess3.nms.refl.providers.ReflPersistentDataProvider;
import net.ess3.nms.refl.providers.ReflServerStateProvider;
import net.ess3.nms.refl.providers.ReflSpawnEggProvider;
import net.ess3.nms.refl.providers.ReflSpawnerBlockProvider;
import net.ess3.nms.refl.providers.ReflSyncCommandsProvider;
import net.ess3.provider.InventoryViewProvider;
import net.ess3.provider.KnownCommandsProvider;
import net.ess3.provider.PlayerLocaleProvider;
import net.ess3.provider.ProviderListener;
import net.ess3.provider.ServerStateProvider;
import net.ess3.provider.providers.BaseBannerDataProvider;
import net.ess3.provider.providers.BaseInventoryViewProvider;
import net.ess3.provider.providers.BlockMetaSpawnerItemProvider;
import net.ess3.provider.providers.BukkitMaterialTagProvider;
import net.ess3.provider.providers.BukkitSpawnerBlockProvider;
import net.ess3.provider.providers.BukkitTileEntityProvider;
import net.ess3.provider.providers.FixedHeightWorldInfoProvider;
import net.ess3.provider.providers.FlatSpawnEggProvider;
import net.ess3.provider.providers.LegacyBannerDataProvider;
import net.ess3.provider.providers.LegacyBiomeNameProvider;
import net.ess3.provider.providers.LegacyDamageEventProvider;
import net.ess3.provider.providers.LegacyInventoryViewProvider;
import net.ess3.provider.providers.LegacyItemUnbreakableProvider;
import net.ess3.provider.providers.LegacyPlayerLocaleProvider;
import net.ess3.provider.providers.LegacyPotionMetaProvider;
import net.ess3.provider.providers.LegacySpawnEggProvider;
import net.ess3.provider.providers.ModernDamageEventProvider;
import net.ess3.provider.providers.ModernDataWorldInfoProvider;
import net.ess3.provider.providers.ModernItemUnbreakableProvider;
import net.ess3.provider.providers.ModernPersistentDataProvider;
import net.ess3.provider.providers.ModernPlayerLocaleProvider;
import net.ess3.provider.providers.ModernPotionMetaProvider;
import net.ess3.provider.providers.ModernSignDataProvider;
import net.ess3.provider.providers.ModernSyncCommandsProvider;
import net.ess3.provider.providers.PaperBiomeKeyProvider;
import net.ess3.provider.providers.PaperContainerProvider;
import net.ess3.provider.providers.PaperKnownCommandsProvider;
import net.ess3.provider.providers.PaperMaterialTagProvider;
import net.ess3.provider.providers.PaperRecipeBookListener;
import net.ess3.provider.providers.PaperSerializationProvider;
import net.ess3.provider.providers.PaperServerStateProvider;
import net.ess3.provider.providers.PaperTickCountProvider;
import net.ess3.provider.providers.PaperTileEntityProvider;
import net.ess3.provider.providers.PrehistoricPotionMetaProvider;
import net.essentialsx.api.v2.services.BalanceTop;
import net.essentialsx.api.v2.services.mail.MailService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class Essentials extends JavaPlugin implements net.ess3.api.IEssentials {
   private int commandHandlers;
   private static final Logger BUKKIT_LOGGER = Logger.getLogger("Essentials");
   private static Logger LOGGER = null;
   public static boolean TESTING = false;
   private final transient TNTExplodeListener tntListener = new TNTExplodeListener();
   private final transient Set<String> vanishedPlayers = new LinkedHashSet<>();
   private final transient Map<String, IEssentialsCommand> commandMap = new HashMap<>();
   private final transient ProviderFactory providerFactory = new ProviderFactory(this);
   private transient net.ess3.api.ISettings settings;
   private transient Jails jails;
   private transient Warps warps;
   private transient Worth worth;
   private transient List<IConf> confList;
   private transient Backup backup;
   private transient AbstractItemDb itemDb;
   private transient CustomItemResolver customItemResolver;
   private transient PermissionsHandler permissionsHandler;
   private transient AlternativeCommandsHandler alternativeCommandsHandler;
   @Deprecated
   private transient UserMap legacyUserMap;
   private transient ModernUserMap userMap;
   private transient BalanceTopImpl balanceTop;
   private transient ExecuteTimer execTimer;
   private transient MailService mail;
   private transient I18n i18n;
   private transient MetricsWrapper metrics;
   private transient EssentialsTimer timer;
   private transient ProviderListener recipeBookEventProvider;
   private transient Kits kits;
   private transient RandomTeleport randomTeleport;
   private transient UpdateChecker updateChecker;
   private transient AdventureFacet adventureFacet;

   public net.ess3.api.ISettings getSettings() {
      return this.settings;
   }

   public void onLoad() {
      try {
         Class.forName("net.milkbowl.vault.economy.Economy");
         this.getServer().getServicesManager().register(Economy.class, new VaultEconomyProvider(this), this, ServicePriority.Normal);
      } catch (ClassNotFoundException var2) {
      }
   }

   public void onEnable() {
      this.commandHandlers = this.registerCommandHandlers();

      try {
         if (BUKKIT_LOGGER != super.getLogger()) {
            BUKKIT_LOGGER.setParent(super.getLogger());
         }

         LOGGER = EssentialsLogger.getLoggerProvider(this);
         EssentialsLogger.updatePluginLogger(this);
         this.initAdventureFacet();
         this.execTimer = new ExecuteTimer();
         this.execTimer.start();
         EssentialsUpgrade upgrade = new EssentialsUpgrade(this);
         upgrade.upgradeLang();
         this.execTimer.mark("AdventureUpgrade");
         this.i18n = new I18n(this);
         this.i18n.onEnable();
         this.execTimer.mark("I18n1");
         Console.setInstance(this);
         switch (VersionUtil.getServerSupportStatus()) {
            case NMS_CLEANROOM:
               this.getLogger().severe(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverUnsupportedCleanroom")));
               break;
            case DANGEROUS_FORK:
               this.getLogger().severe(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverUnsupportedDangerous")));
               break;
            case STUPID_PLUGIN:
               this.getLogger().severe(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverUnsupportedDumbPlugins")));
               break;
            case UNSTABLE:
               this.getLogger().severe(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverUnsupportedMods")));
               break;
            case OUTDATED:
               this.getLogger().severe(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverUnsupported")));
               break;
            case LIMITED:
               this.getLogger().info(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverUnsupportedLimitedApi")));
         }

         if (VersionUtil.getSupportStatusClass() != null) {
            this.getLogger().info(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverUnsupportedClass", VersionUtil.getSupportStatusClass())));
         }

         if (VersionUtil.getServerBukkitVersion().isSnapshot()) {
            this.getLogger().severe(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverSnapshot")));
         }

         PluginManager pm = this.getServer().getPluginManager();

         for (Plugin plugin : pm.getPlugins()) {
            if (plugin.getDescription().getName().startsWith("Essentials")
               && !plugin.getDescription().getVersion().equals(this.getDescription().getVersion())
               && !plugin.getDescription().getName().equals("EssentialsAntiCheat")) {
               this.getLogger().warning(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("versionMismatch", plugin.getDescription().getName())));
            }
         }

         upgrade.beforeSettings();
         this.execTimer.mark("Upgrade");
         this.confList = new ArrayList<>();
         this.settings = new Settings(this);
         this.confList.add(this.settings);
         this.execTimer.mark("Settings");
         upgrade.preModules();
         this.execTimer.mark("Upgrade2");
         this.mail = new MailServiceImpl(this);
         this.execTimer.mark("Init(Mail)");
         this.userMap = new ModernUserMap(this);
         this.legacyUserMap = new UserMap(this.userMap);
         this.execTimer.mark("Init(Usermap)");
         this.balanceTop = new BalanceTopImpl(this);
         this.execTimer.mark("Init(BalanceTop)");
         this.kits = new Kits(this);
         this.confList.add(this.kits);
         upgrade.convertKits();
         this.execTimer.mark("Kits");
         this.randomTeleport = new RandomTeleport(this);
         this.confList.add(this.randomTeleport);
         this.execTimer.mark("Init(RandomTeleport)");
         upgrade.afterSettings();
         this.execTimer.mark("Upgrade3");
         this.warps = new Warps(this.getDataFolder());
         this.confList.add(this.warps);
         this.execTimer.mark("Init(Warp)");
         this.worth = new Worth(this.getDataFolder());
         this.confList.add(this.worth);
         this.execTimer.mark("Init(Worth)");
         this.itemDb = this.getItemDbFromConfig();
         this.confList.add(this.itemDb);
         this.execTimer.mark("Init(ItemDB)");
         this.customItemResolver = new CustomItemResolver(this);

         try {
            this.itemDb.registerResolver(this, "custom_items", this.customItemResolver);
            this.confList.add(this.customItemResolver);
         } catch (Exception var8) {
            var8.printStackTrace();
            this.customItemResolver = null;
         }

         this.execTimer.mark("Init(CustomItemResolver)");
         this.jails = new Jails(this);
         this.confList.add(this.jails);
         this.execTimer.mark("Init(Jails)");
         EconomyLayers.onEnable(this);
         this.execTimer.mark("Init(EconomyLayers)");
         this.providerFactory.registerProvider(BlockMetaSpawnerItemProvider.class);
         this.providerFactory.registerProvider(ReflSpawnerBlockProvider.class, BukkitSpawnerBlockProvider.class);
         this.providerFactory.registerProvider(LegacySpawnEggProvider.class, ReflSpawnEggProvider.class, FlatSpawnEggProvider.class);
         this.providerFactory.registerProvider(PrehistoricPotionMetaProvider.class, LegacyPotionMetaProvider.class, ModernPotionMetaProvider.class);
         this.providerFactory.registerProvider(LegacyBannerDataProvider.class, BaseBannerDataProvider.class);
         this.providerFactory.registerProvider(ReflServerStateProvider.class, PaperServerStateProvider.class);
         this.providerFactory.registerProvider(PaperContainerProvider.class);
         this.providerFactory.registerProvider(PaperSerializationProvider.class);
         this.providerFactory.registerProvider(ReflKnownCommandsProvider.class, PaperKnownCommandsProvider.class);
         this.providerFactory.registerProvider(ReflFormattedCommandAliasProvider.class);
         this.providerFactory.registerProvider(BukkitMaterialTagProvider.class, PaperMaterialTagProvider.class);
         this.providerFactory.registerProvider(ReflSyncCommandsProvider.class, ModernSyncCommandsProvider.class);
         this.providerFactory.registerProvider(ReflPersistentDataProvider.class, ModernPersistentDataProvider.class);
         this.providerFactory.registerProvider(ReflOnlineModeProvider.class);
         this.providerFactory.registerProvider(LegacyItemUnbreakableProvider.class, ModernItemUnbreakableProvider.class);
         this.providerFactory.registerProvider(FixedHeightWorldInfoProvider.class, ReflDataWorldInfoProvider.class, ModernDataWorldInfoProvider.class);
         this.providerFactory.registerProvider(ModernSignDataProvider.class);
         this.providerFactory.registerProvider(ModernPlayerLocaleProvider.class, LegacyPlayerLocaleProvider.class);
         this.providerFactory.registerProvider(ModernDamageEventProvider.class, LegacyDamageEventProvider.class);
         this.providerFactory.registerProvider(LegacyInventoryViewProvider.class, BaseInventoryViewProvider.class);
         this.providerFactory.registerProvider(LegacyBiomeNameProvider.class);
         this.providerFactory.registerProvider(PaperBiomeKeyProvider.class);
         this.providerFactory.registerProvider(BukkitTileEntityProvider.class, PaperTileEntityProvider.class);
         this.providerFactory.registerProvider(PaperTickCountProvider.class);
         if (!TESTING) {
            this.providerFactory.finalizeRegistration();
         }

         if (PaperLib.isPaper()) {
            try {
               Class.forName("com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent");
               this.recipeBookEventProvider = new PaperRecipeBookListener(event -> {
                  if (this.getUser(((PlayerEvent)event).getPlayer()).isRecipeSee()) {
                     ((Cancellable)event).setCancelled(true);
                  }
               });
               if (this.getSettings().isDebug()) {
                  LOGGER.log(Level.INFO, "Registered Paper Recipe Book Event Listener");
               }
            } catch (ClassNotFoundException var7) {
            }
         }

         this.execTimer.mark("Init(Providers)");
         this.reload();
         ((Settings)this.settings)._lateLoadItemSpawnBlacklist();
         this.backup = new Backup(this);
         this.permissionsHandler = new PermissionsHandler(this, this.settings.useBukkitPermissions());
         this.alternativeCommandsHandler = new AlternativeCommandsHandler(this);
         this.timer = new EssentialsTimer(this);
         this.scheduleSyncRepeatingTask(this.timer, 1000L, 50L);
         net.ess3.api.Economy.setEss(this);
         this.execTimer.mark("RegHandler");
         PermissionsDefaults.registerAllBackDefaults();
         PermissionsDefaults.registerAllHatDefaults();
         if (!TESTING) {
            this.updateChecker = new UpdateChecker(this);
            this.runTaskAsynchronously(
               () -> {
                  this.getLogger().log(Level.INFO, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("versionFetching")));

                  for (ComponentHolder component : this.updateChecker.getVersionMessages(false, true, new CommandSource(this, Bukkit.getConsoleSender()))) {
                     this.getLogger()
                        .log(this.getSettings().isUpdateCheckEnabled() ? Level.WARNING : Level.INFO, this.getAdventureFacet().adventureToLegacy(component));
                  }
               }
            );
            this.metrics = new MetricsWrapper(this, 858, true);
         }

         this.execTimer.mark("Init(External)");
         String timeroutput = this.execTimer.end();
         if (this.getSettings().isDebug()) {
            LOGGER.log(Level.INFO, "Essentials load " + timeroutput);
         }
      } catch (NumberFormatException var9) {
         this.handleCrash(var9);
      } catch (Error var10) {
         this.handleCrash(var10);
         throw var10;
      }

      if (!TESTING) {
         this.getBackup().setPendingShutdown(false);
      }
   }

   public static Logger getWrappedLogger() {
      return LOGGER != null ? LOGGER : BUKKIT_LOGGER;
   }

   public void saveConfig() {
   }

   private void registerListeners(final PluginManager pm) {
      HandlerList.unregisterAll(this);
      if (this.getSettings().isDebug()) {
         LOGGER.log(Level.INFO, "Registering Listeners");
      }

      EssentialsPluginListener pluginListener = new EssentialsPluginListener(this);
      pm.registerEvents(pluginListener, this);
      this.confList.add(pluginListener);
      EssentialsPlayerListener playerListener = new EssentialsPlayerListener(this);
      playerListener.registerEvents();
      EssentialsBlockListener blockListener = new EssentialsBlockListener(this);
      pm.registerEvents(blockListener, this);
      SignBlockListener signBlockListener = new SignBlockListener(this);
      pm.registerEvents(signBlockListener, this);
      SignPlayerListener signPlayerListener = new SignPlayerListener(this);
      pm.registerEvents(signPlayerListener, this);
      SignEntityListener signEntityListener = new SignEntityListener(this);
      pm.registerEvents(signEntityListener, this);
      EssentialsEntityListener entityListener = new EssentialsEntityListener(this);
      pm.registerEvents(entityListener, this);
      Essentials.EssentialsWorldListener worldListener = new Essentials.EssentialsWorldListener(this);
      pm.registerEvents(worldListener, this);
      EssentialsServerListener serverListener = new EssentialsServerListener(this);
      pm.registerEvents(serverListener, this);
      pm.registerEvents(this.tntListener, this);
      if (this.recipeBookEventProvider != null) {
         pm.registerEvents(this.recipeBookEventProvider, this);
      }

      this.jails.resetListener();
   }

   @Override
   public ProviderFactory getProviders() {
      return this.providerFactory;
   }

   public void onDisable() {
      if (this.adventureFacet != null) {
         this.adventureFacet.close();
      }

      boolean stopping = TESTING || this.provider(ServerStateProvider.class).isStopping();
      if (!stopping) {
         LOGGER.log(Level.SEVERE, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("serverReloading")));
      }

      if (!TESTING) {
         this.getBackup().setPendingShutdown(true);
      }

      for (User user : this.getOnlineUsers()) {
         if (user.isVanished()) {
            user.setVanished(false);
            user.sendTl("unvanishedReload");
         }

         if (stopping) {
            user.setLogoutLocation();
            if (!user.isHidden()) {
               user.setLastLogout(System.currentTimeMillis());
            }

            user.cleanup();
         } else {
            user.stopTransaction();
         }
      }

      this.cleanupOpenInventories();
      if (!TESTING && this.getBackup().getTaskLock() != null && !this.getBackup().getTaskLock().isDone()) {
         LOGGER.log(Level.SEVERE, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("backupInProgress")));
         this.getBackup().getTaskLock().join();
      }

      if (this.i18n != null) {
         this.i18n.onDisable();
      }

      if (this.backup != null) {
         this.backup.stopTask();
      }

      if (!TESTING) {
         this.getPermissionsHandler().unregisterContexts();
      }

      net.ess3.api.Economy.setEss(null);
      Trade.closeLog();
      this.getUsers().shutdown();
      EssentialsConfiguration.shutdownExecutor();
      HandlerList.unregisterAll(this);
   }

   @Override
   public void reload() {
      Trade.closeLog();

      for (IConf iConf : this.confList) {
         iConf.reloadConfig();
         this.execTimer.mark("Reload(" + iConf.getClass().getSimpleName() + ")");
      }

      this.i18n.updateLocale(this.settings.getLocale());

      for (String commandName : this.getDescription().getCommands().keySet()) {
         Command command = this.getCommand(commandName);
         if (command != null) {
            command.setDescription(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral(commandName + "CommandDescription")));
            command.setUsage(this.getAdventureFacet().miniToLegacy(I18n.tlLiteral(commandName + "CommandUsage")));
         }
      }

      PluginManager pm = this.getServer().getPluginManager();
      this.registerListeners(pm);
      this.initAdventureFacet();
   }

   private void initAdventureFacet() {
      if (this.adventureFacet != null) {
         this.adventureFacet.close();
      }

      if (VersionUtil.isPaper() && VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_16_5_R01)) {
         this.adventureFacet = new PaperAdventureFacet(
            this.getSettings() != null ? this.getSettings().getPrimaryColor() : null,
            this.getSettings() != null ? this.getSettings().getSecondaryColor() : null
         );
      } else {
         this.adventureFacet = new SpigotAdventureFacet(this);
      }

      AdventureUtil.setAdventureFacet(this.adventureFacet);
   }

   private IEssentialsCommand loadCommand(final String path, final String name, final IEssentialsModule module, final ClassLoader classLoader) throws Exception {
      if (this.commandMap.containsKey(name)) {
         return this.commandMap.get(name);
      } else {
         IEssentialsCommand cmd = (IEssentialsCommand)classLoader.loadClass(path + name).getDeclaredConstructor().newInstance();
         cmd.setEssentials(this);
         cmd.setEssentialsModule(module);
         this.commandMap.put(name, cmd);
         return cmd;
      }
   }

   @Override
   public Map<String, IEssentialsCommand> getCommandMap() {
      return this.commandMap;
   }

   public List<String> onTabComplete(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
      return this.onTabCompleteEssentials(
         sender, command, commandLabel, args, Essentials.class.getClassLoader(), "com.earth2me.essentials.commands.Command", "essentials.", null
      );
   }

   @Override
   public List<String> onTabCompleteEssentials(
      final CommandSender cSender,
      final Command command,
      final String commandLabel,
      final String[] args,
      final ClassLoader classLoader,
      final String commandPath,
      final String permissionPrefix,
      final IEssentialsModule module
   ) {
      if (!this.getSettings().isCommandOverridden(command.getName()) && (!commandLabel.startsWith("e") || commandLabel.equalsIgnoreCase(command.getName()))) {
         Command pc = this.alternativeCommandsHandler.getAlternative(commandLabel);
         if (pc instanceof PluginCommand) {
            try {
               TabCompleter completer = ((PluginCommand)pc).getTabCompleter();
               if (completer != null) {
                  return completer.onTabComplete(cSender, command, commandLabel, args);
               }
            } catch (Exception var15) {
               LOGGER.log(Level.SEVERE, var15.getMessage(), (Throwable)var15);
            }
         }
      }

      try {
         User user = null;
         if (cSender instanceof Player) {
            user = this.getUser((Player)cSender);
         }

         CommandSource sender = new CommandSource(this, cSender);
         if (!this.getSettings().isCommandDisabled(commandLabel)) {
            IEssentialsCommand cmd;
            try {
               cmd = this.loadCommand(commandPath, command.getName(), module, classLoader);
            } catch (Exception var14) {
               sender.sendTl("commandNotLoaded", commandLabel);
               LOGGER.log(Level.SEVERE, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("commandNotLoaded", commandLabel)), (Throwable)var14);
               return Collections.emptyList();
            }

            if (user != null && !user.isAuthorized(cmd, permissionPrefix)) {
               return Collections.emptyList();
            } else if (user != null && user.isJailed() && !user.isAuthorized(cmd, "essentials.jail.allow.")) {
               return Collections.emptyList();
            } else {
               try {
                  return user == null
                     ? cmd.tabComplete(this.getServer(), sender, commandLabel, command, args)
                     : cmd.tabComplete(this.getServer(), user, commandLabel, command, args);
               } catch (Exception var13) {
                  this.showError(sender, var13, commandLabel);
                  LOGGER.log(Level.SEVERE, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("commandFailed", commandLabel)), (Throwable)var13);
                  return Collections.emptyList();
               }
            }
         } else {
            Command newCmd = this.provider(KnownCommandsProvider.class).getKnownCommands().get(commandLabel);
            return newCmd == null || newCmd instanceof PluginIdentifiableCommand && ((PluginIdentifiableCommand)newCmd).getPlugin() == this
               ? Collections.emptyList()
               : newCmd.tabComplete(cSender, commandLabel, args);
         }
      } catch (Throwable var16) {
         LOGGER.log(Level.SEVERE, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("commandFailed", commandLabel)), var16);
         return Collections.emptyList();
      }
   }

   public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
      this.metrics.markCommand(command.getName(), true);
      return this.onCommandEssentials(
         sender, command, commandLabel, args, Essentials.class.getClassLoader(), "com.earth2me.essentials.commands.Command", "essentials.", null
      );
   }

   @Override
   public boolean onCommandEssentials(
      final CommandSender cSender,
      final Command command,
      final String commandLabel,
      final String[] args,
      final ClassLoader classLoader,
      final String commandPath,
      final String permissionPrefix,
      final IEssentialsModule module
   ) {
      if (!this.getSettings().isCommandOverridden(command.getName()) && (!commandLabel.startsWith("e") || commandLabel.equalsIgnoreCase(command.getName()))) {
         if (this.getSettings().isDebug()) {
            LOGGER.log(Level.INFO, "Searching for alternative to: " + commandLabel);
         }

         Command pc = this.alternativeCommandsHandler.getAlternative(commandLabel);
         if (pc != null) {
            this.alternativeCommandsHandler.executed(commandLabel, pc);

            try {
               pc.execute(cSender, commandLabel, args);
            } catch (Exception var22) {
               LOGGER.log(Level.SEVERE, var22.getMessage(), (Throwable)var22);
               if (cSender instanceof Player) {
                  PlayerLocaleProvider localeProvider = this.provider(PlayerLocaleProvider.class);
                  String miniMessageStr = I18n.tlLocale(I18n.getLocale(localeProvider.getLocale((Player)cSender)), "internalError");
                  this.getAdventureFacet().send(cSender, this.getAdventureFacet().deserializeMiniMessage(miniMessageStr));
               } else {
                  cSender.sendMessage(I18n.tlLiteral("internalError"));
               }
            }

            return true;
         }
      }

      try {
         User user = null;
         Block bSenderBlock = null;
         if (cSender instanceof Player) {
            user = this.getUser((Player)cSender);
         } else if (cSender instanceof BlockCommandSender) {
            BlockCommandSender bsender = (BlockCommandSender)cSender;
            bSenderBlock = bsender.getBlock();
         }

         if (bSenderBlock != null) {
            if (this.getSettings().logCommandBlockCommands()) {
               LOGGER.log(
                  Level.INFO,
                  "CommandBlock at "
                     + bSenderBlock.getX()
                     + ","
                     + bSenderBlock.getY()
                     + ","
                     + bSenderBlock.getZ()
                     + " issued server command: /"
                     + commandLabel
                     + " "
                     + EssentialsCommand.getFinalArg(args, 0)
               );
            }
         } else if (user == null && this.getSettings().logConsoleCommands()) {
            LOGGER.log(Level.INFO, cSender.getName() + " issued server command: /" + commandLabel + " " + EssentialsCommand.getFinalArg(args, 0));
         }

         CommandSource sender = new CommandSource(this, cSender);
         if (user != null && !this.getSettings().isCommandDisabled("mail") && !command.getName().equals("mail") && user.isAuthorized("essentials.mail")) {
            user.notifyOfMail();
         }

         if (commandLabel.equalsIgnoreCase("essversion")) {
            sender.sendMessage("This server is running Essentials " + this.getDescription().getVersion());
            return true;
         } else if (!this.getSettings().isCommandDisabled(commandLabel)) {
            IEssentialsCommand cmd;
            try {
               cmd = this.loadCommand(commandPath, command.getName(), module, classLoader);
            } catch (Exception var18) {
               sender.sendTl("commandNotLoaded", commandLabel);
               LOGGER.log(Level.SEVERE, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("commandNotLoaded", commandLabel)), (Throwable)var18);
               return true;
            }

            if (user != null && !user.isAuthorized(cmd, permissionPrefix)) {
               LOGGER.log(Level.INFO, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("deniedAccessCommand", user.getName())));
               user.sendTl("noAccessCommand");
               return true;
            } else if (user != null && user.isJailed() && !user.isAuthorized(cmd, "essentials.jail.allow.")) {
               if (user.getJailTimeout() > 0L) {
                  user.sendTl("playerJailedFor", user.getName(), user.getFormattedJailTime());
               } else {
                  user.sendTl("jailMessage");
               }

               return true;
            } else {
               try {
                  if (user == null) {
                     cmd.run(this.getServer(), sender, commandLabel, command, args);
                  } else {
                     cmd.run(this.getServer(), user, commandLabel, command, args);
                  }

                  return true;
               } catch (QuietAbortException | NoChargeException var19) {
                  return true;
               } catch (NotEnoughArgumentsException var20) {
                  if (this.getSettings().isVerboseCommandUsages() && !cmd.getUsageStrings().isEmpty()) {
                     sender.sendTl("commandHelpLine1", commandLabel);
                     String description = command.getDescription();

                     try {
                        description = sender.tl(command.getName() + "CommandDescription");
                     } catch (MissingResourceException var17) {
                     }

                     sender.sendTl("commandHelpLine2", description);
                     sender.sendTl("commandHelpLine3");

                     for (Entry<String, String> usage : cmd.getUsageStrings().entrySet()) {
                        sender.sendTl(
                           "commandHelpLineUsage",
                           AdventureUtil.parsed(usage.getKey().replace("<command>", commandLabel)),
                           AdventureUtil.parsed(sender.tl(usage.getValue()))
                        );
                     }
                  } else {
                     sender.sendMessage(command.getDescription());
                     sender.sendMessage(command.getUsage().replace("<command>", commandLabel));
                  }

                  if (!var20.getMessage().isEmpty()) {
                     sender.sendComponent(this.getAdventureFacet().deserializeMiniMessage(var20.getMessage()));
                  }

                  if (var20.getCause() != null && this.settings.isDebug()) {
                     var20.getCause().printStackTrace();
                  }

                  return true;
               } catch (Exception var21) {
                  this.showError(sender, var21, commandLabel);
                  if (this.settings.isDebug()) {
                     var21.printStackTrace();
                  }

                  return true;
               }
            }
         } else {
            Command newCmd = this.provider(KnownCommandsProvider.class).getKnownCommands().get(commandLabel);
            if (newCmd == null || newCmd instanceof PluginIdentifiableCommand && this.isEssentialsPlugin(((PluginIdentifiableCommand)newCmd).getPlugin())) {
               sender.sendTl("commandDisabled", commandLabel);
               return true;
            } else {
               return newCmd.execute(cSender, commandLabel, args);
            }
         }
      } catch (Throwable var23) {
         LOGGER.log(Level.SEVERE, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("commandFailed", commandLabel)), var23);
         return true;
      }
   }

   private boolean isEssentialsPlugin(Plugin plugin) {
      return plugin.getDescription().getMain().contains("com.earth2me.essentials") || plugin.getDescription().getMain().contains("net.essentialsx");
   }

   public void cleanupOpenInventories() {
      InventoryViewProvider provider = this.provider(InventoryViewProvider.class);

      for (User user : this.getOnlineUsers()) {
         if (user.isRecipeSee()) {
            InventoryView view = user.getBase().getOpenInventory();
            provider.getTopInventory(view).clear();
            provider.close(view);
            user.setRecipeSee(false);
         }

         if (user.isInvSee() || user.isEnderSee()) {
            provider.close(user.getBase().getOpenInventory());
            user.setInvSee(false);
            user.setEnderSee(false);
         }
      }
   }

   @Override
   public void showError(final CommandSource sender, final Throwable exception, final String commandLabel) {
      if (exception instanceof TranslatableException) {
         String tlMessage = sender.tl(((TranslatableException)exception).getTlKey(), ((TranslatableException)exception).getArgs());
         sender.sendTl("errorWithMessage", AdventureUtil.parsed(tlMessage));
      } else {
         sender.sendTl("errorWithMessage", exception.getMessage());
      }

      if (this.getSettings().isDebug()) {
         LOGGER.log(Level.INFO, this.getAdventureFacet().miniToLegacy(I18n.tlLiteral("errorCallingCommand", commandLabel)), exception);
      }
   }

   @Override
   public BukkitScheduler getScheduler() {
      return this.getServer().getScheduler();
   }

   @Override
   public List<Player> getJailedPlayers() {
      return this.getUsers()
         .getAllUserUUIDs()
         .stream()
         .map(this::getUser)
         .filter(Objects::nonNull)
         .filter(UserData::isJailed)
         .map(PlayerExtension::getBase)
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   public IJails getJails() {
      return this.jails;
   }

   public Warps getWarps() {
      return this.warps;
   }

   @Override
   public Worth getWorth() {
      return this.worth;
   }

   @Override
   public Backup getBackup() {
      return this.backup;
   }

   @Override
   public Kits getKits() {
      return this.kits;
   }

   @Override
   public RandomTeleport getRandomTeleport() {
      return this.randomTeleport;
   }

   @Override
   public UpdateChecker getUpdateChecker() {
      return this.updateChecker;
   }

   @Deprecated
   @Override
   public User getUser(final Object base) {
      if (base instanceof Player) {
         return this.getUser((Player)base);
      } else if (base instanceof OfflinePlayer) {
         return this.getUser(((OfflinePlayer)base).getUniqueId());
      } else if (base instanceof UUID) {
         return this.getUser((UUID)base);
      } else {
         return base instanceof String ? this.getOfflineUser((String)base) : null;
      }
   }

   @Override
   public User getUser(final String base) {
      return this.getOfflineUser(base);
   }

   @Override
   public User getUser(final UUID base) {
      return this.userMap.getUser(base);
   }

   @Override
   public User getOfflineUser(final String name) {
      return this.userMap.getUser(name);
   }

   @Override
   public User matchUser(final Server server, final User sourceUser, final String searchTerm, final Boolean getHidden, final boolean getOffline) throws PlayerNotFoundException {
      if (sourceUser == null || !searchTerm.equals("@p") && !searchTerm.equals("@s")) {
         Player exPlayer;
         try {
            exPlayer = server.getPlayer(UUID.fromString(searchTerm));
         } catch (IllegalArgumentException var13) {
            if (getOffline) {
               exPlayer = server.getPlayerExact(searchTerm);
            } else {
               exPlayer = server.getPlayerExact(searchTerm);
               if (exPlayer == null) {
                  exPlayer = server.getPlayer(searchTerm);
               }
            }
         }

         User user;
         if (exPlayer != null) {
            user = this.getUser(exPlayer);
         } else {
            user = this.getUser(searchTerm);
         }

         if (user == null) {
            List<Player> matches = server.matchPlayer(searchTerm);
            if (matches.isEmpty()) {
               String matchText = searchTerm.toLowerCase(Locale.ENGLISH);

               for (User userMatch : this.getOnlineUsers()) {
                  if (getHidden || this.canInteractWith(sourceUser, userMatch)) {
                     String displayName = FormatUtil.stripFormat(userMatch.getDisplayName()).toLowerCase(Locale.ENGLISH);
                     if (displayName.contains(matchText)) {
                        return userMatch;
                     }
                  }
               }
            } else {
               for (Player player : matches) {
                  if (player.getName().equalsIgnoreCase(searchTerm)) {
                     User userMatchx = this.getUser(player);
                     if (getHidden || this.canInteractWith(sourceUser, userMatchx)) {
                        return userMatchx;
                     }
                  }
               }

               for (Player playerx : matches) {
                  User userMatchx = this.getUser(playerx);
                  if (userMatchx.getDisplayName().startsWith(searchTerm) && (getHidden || this.canInteractWith(sourceUser, userMatchx))) {
                     return userMatchx;
                  }
               }

               User userMatchx = this.getUser(matches.get(0));
               if (getHidden || this.canInteractWith(sourceUser, userMatchx)) {
                  return userMatchx;
               }
            }

            throw new PlayerNotFoundException();
         } else if (!getOffline && !user.getBase().isOnline()) {
            throw new PlayerNotFoundException();
         } else if (getHidden || this.canInteractWith(sourceUser, user)) {
            return user;
         } else if (getOffline && user.getName().equalsIgnoreCase(searchTerm)) {
            return user;
         } else {
            throw new PlayerNotFoundException();
         }
      } else {
         return sourceUser;
      }
   }

   @Override
   public boolean canInteractWith(final CommandSource interactor, final User interactee) {
      if (interactor == null) {
         return !interactee.isHidden();
      } else {
         return interactor.isPlayer() ? this.canInteractWith(this.getUser(interactor.getPlayer()), interactee) : true;
      }
   }

   @Override
   public boolean canInteractWith(final User interactor, final User interactee) {
      if (interactor == null) {
         return !interactee.isHidden();
      } else {
         return interactor.equals(interactee) ? true : !interactee.isHiddenFrom(interactor.getBase());
      }
   }

   @Override
   public User getUser(final Player base) {
      if (base == null) {
         return null;
      } else if (this.userMap == null) {
         LOGGER.log(Level.WARNING, "Essentials userMap not initialized");
         return null;
      } else {
         User user = this.userMap.getUser(base);
         if (base.getClass() != UUIDPlayer.class || user.getBase() == null) {
            user.update(base);
         }

         return user;
      }
   }

   private void handleCrash(final Throwable exception) {
      PluginManager pm = this.getServer().getPluginManager();
      getWrappedLogger().log(Level.SEVERE, exception.toString(), exception);
      pm.registerEvents(new Listener() {
         @EventHandler(
            priority = EventPriority.LOW
         )
         public void onPlayerJoin(final PlayerJoinEvent event) {
            event.getPlayer().sendMessage("Essentials failed to load, read the log file.");
         }
      }, this);

      for (Player player : this.getOnlinePlayers()) {
         player.sendMessage("Essentials failed to load, read the log file.");
      }

      this.setEnabled(false);
   }

   @Override
   public World getWorld(final String name) {
      if (name.matches("[0-9]+")) {
         int worldId = Integer.parseInt(name);
         if (worldId < this.getServer().getWorlds().size()) {
            return (World)this.getServer().getWorlds().get(worldId);
         }
      }

      return this.getServer().getWorld(name);
   }

   @Override
   public void addReloadListener(final IConf listener) {
      this.confList.add(listener);
   }

   @Override
   public int broadcastMessage(final String message) {
      return this.broadcastMessage(null, null, message, true, null);
   }

   @Override
   public int broadcastMessage(final IUser sender, final String message) {
      return this.broadcastMessage(sender, null, message, false, null);
   }

   @Override
   public int broadcastMessage(final IUser sender, final String message, final Predicate<IUser> shouldExclude) {
      return this.broadcastMessage(sender, null, message, false, shouldExclude);
   }

   @Override
   public int broadcastMessage(final String permission, final String message) {
      return this.broadcastMessage(null, permission, message, false, null);
   }

   private int broadcastMessage(final IUser sender, final String permission, final String message, final boolean keywords, final Predicate<IUser> shouldExclude) {
      if (sender != null && sender.isHidden()) {
         return 0;
      } else {
         IText broadcast = new SimpleTextInput(message);
         Collection<Player> players = this.getOnlinePlayers();

         for (Player player : players) {
            User user = this.getUser(player);
            if ((permission == null && (sender == null || !user.isIgnoredPlayer(sender)) || permission != null && user.isAuthorized(permission))
               && (shouldExclude == null || !shouldExclude.test(user))) {
               if (keywords) {
                  broadcast = new KeywordReplacer(broadcast, new CommandSource(this, player), this, false);
               }

               for (String messageText : broadcast.getLines()) {
                  user.sendMessage(messageText);
               }
            }
         }

         return players.size();
      }
   }

   @Override
   public void broadcastTl(final String tlKey, final Object... args) {
      this.broadcastTl(null, null, false, tlKey, args);
   }

   @Override
   public void broadcastTl(final IUser sender, final String tlKey, final Object... args) {
      this.broadcastTl(sender, null, false, tlKey, args);
   }

   @Override
   public void broadcastTl(final IUser sender, final String permission, final String tlKey, final Object... args) {
      this.broadcastTl(sender, u -> !u.isAuthorized(permission), false, tlKey, args);
   }

   @Override
   public void broadcastTl(IUser sender, Predicate<IUser> shouldExclude, String tlKey, Object... args) {
      this.broadcastTl(sender, shouldExclude, false, tlKey, args);
   }

   @Override
   public void broadcastTl(final IUser sender, final Predicate<IUser> shouldExclude, final boolean parseKeywords, final String tlKey, final Object... args) {
      if (sender == null || !sender.isHidden()) {
         for (User user : this.getOnlineUsers()) {
            if ((sender == null || !user.isIgnoredPlayer(sender)) && (shouldExclude == null || !shouldExclude.test(user))) {
               Object[] processedArgs;
               if (parseKeywords) {
                  processedArgs = I18n.mutateArgs(
                     args, s -> new KeywordReplacer(new SimpleTextInput(s.toString()), new CommandSource(this, user.getBase()), this, false).getLines().get(0)
                  );
               } else {
                  processedArgs = args;
               }

               user.sendTl(tlKey, processedArgs);
            }
         }
      }
   }

   @Override
   public BukkitTask runTaskAsynchronously(final Runnable run) {
      return this.getScheduler().runTaskAsynchronously(this, run);
   }

   @Override
   public BukkitTask runTaskLaterAsynchronously(final Runnable run, final long delay) {
      return this.getScheduler().runTaskLaterAsynchronously(this, run, delay);
   }

   @Override
   public BukkitTask runTaskTimerAsynchronously(final Runnable run, final long delay, final long period) {
      return this.getScheduler().runTaskTimerAsynchronously(this, run, delay, period);
   }

   @Override
   public int scheduleSyncDelayedTask(final Runnable run) {
      return this.getScheduler().scheduleSyncDelayedTask(this, run);
   }

   @Override
   public int scheduleSyncDelayedTask(final Runnable run, final long delay) {
      return this.getScheduler().scheduleSyncDelayedTask(this, run, delay);
   }

   @Override
   public int scheduleSyncRepeatingTask(final Runnable run, final long delay, final long period) {
      return this.getScheduler().scheduleSyncRepeatingTask(this, run, delay, period);
   }

   @Override
   public PermissionsHandler getPermissionsHandler() {
      return this.permissionsHandler;
   }

   @Override
   public AlternativeCommandsHandler getAlternativeCommandsHandler() {
      return this.alternativeCommandsHandler;
   }

   public IItemDb getItemDb() {
      return this.itemDb;
   }

   @Deprecated
   @Override
   public UserMap getUserMap() {
      return this.legacyUserMap;
   }

   public ModernUserMap getUsers() {
      return this.userMap;
   }

   @Override
   public BalanceTop getBalanceTop() {
      return this.balanceTop;
   }

   @Override
   public I18n getI18n() {
      return this.i18n;
   }

   @Override
   public EssentialsTimer getTimer() {
      return this.timer;
   }

   @Override
   public MailService getMail() {
      return this.mail;
   }

   @Override
   public List<String> getVanishedPlayers() {
      return Collections.unmodifiableList(new ArrayList<>(this.vanishedPlayers));
   }

   @Override
   public Collection<String> getVanishedPlayersNew() {
      return this.vanishedPlayers;
   }

   @Override
   public Collection<Player> getOnlinePlayers() {
      return this.getServer().getOnlinePlayers();
   }

   @Override
   public Iterable<User> getOnlineUsers() {
      List<User> onlineUsers = new ArrayList<>();

      for (Player player : this.getOnlinePlayers()) {
         onlineUsers.add(this.getUser(player));
      }

      return onlineUsers;
   }

   @Override
   public CustomItemResolver getCustomItemResolver() {
      return this.customItemResolver;
   }

   @Override
   public PluginCommand getPluginCommand(final String cmd) {
      return this.getCommand(cmd);
   }

   @Override
   public AdventureFacet getAdventureFacet() {
      return this.adventureFacet;
   }

   private AbstractItemDb getItemDbFromConfig() {
      String setting = this.settings.getItemDbType();
      if (setting.equalsIgnoreCase("json")) {
         return new FlatItemDb(this);
      } else if (setting.equalsIgnoreCase("csv")) {
         return new LegacyItemDb(this);
      } else {
         VersionUtil.BukkitVersion version = VersionUtil.getServerBukkitVersion();
         return (AbstractItemDb)(version.isHigherThanOrEqualTo(VersionUtil.v1_13_0_R01) ? new FlatItemDb(this) : new LegacyItemDb(this));
      }
   }

   static {
      EconomyLayers.init();
   }

   private static class EssentialsWorldListener implements Listener, Runnable {
      private final transient net.ess3.api.IEssentials ess;

      EssentialsWorldListener(final net.ess3.api.IEssentials ess) {
         this.ess = ess;
      }

      @EventHandler(
         priority = EventPriority.LOW
      )
      public void onWorldLoad(final WorldLoadEvent event) {
         PermissionsDefaults.registerBackDefaultFor(event.getWorld());
      }

      @Override
      public void run() {
         this.ess.reload();
      }
   }
}
