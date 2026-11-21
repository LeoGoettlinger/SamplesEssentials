package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatRuntime;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatStateRecovery;
import com.magmaguy.elitemobs.advancedcombat.menu.ClassSelectionMenu;
import com.magmaguy.elitemobs.advancedcombat.weapons.AdvancedMagicWeaponItems;
import com.magmaguy.elitemobs.antiexploit.FarmingProtection;
import com.magmaguy.elitemobs.api.EMDungeonLocator;
import com.magmaguy.elitemobs.api.EliteMobsInitializedEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.KeepNeutralsAngry;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.combatsystem.PlayerAttackCooldownTracker;
import com.magmaguy.elitemobs.combatsystem.antiexploit.AutoclickerThrottle;
import com.magmaguy.elitemobs.combatsystem.combattag.CombatTag;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.combatsystem.displays.BossHealthDisplay;
import com.magmaguy.elitemobs.commands.CommandHandler;
import com.magmaguy.elitemobs.commands.LanguageCommand;
import com.magmaguy.elitemobs.commands.admin.RemoveCommand;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.AntiExploitConfig;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.ChangelogsConfig;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.ConfigurationExporter;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.DatabaseConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.DiscordSRVConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.EliteMobPowersConfig;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.config.StaticItemNamesConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.config.commands.CommandsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfig;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusesConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldPackage;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.economy.GamblingEconomyHandler;
import com.magmaguy.elitemobs.economy.VaultCompatibility;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.ActionEvent;
import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.gambling.DebtCollectorManager;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.EliteEnchantmentCatalog;
import com.magmaguy.elitemobs.items.GearRestrictionHandler;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import com.magmaguy.elitemobs.items.potioneffects.custom.Harm;
import com.magmaguy.elitemobs.items.potioneffects.custom.Heal;
import com.magmaguy.elitemobs.items.potioneffects.custom.Saturation;
import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.dlc.ConfigurationImporter;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentAnvil;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationConfig;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationContext;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationManager;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationState;
import com.magmaguy.elitemobs.magmacore.instance.InstanceProtector;
import com.magmaguy.elitemobs.magmacore.location.LocationQueryRegistry;
import com.magmaguy.elitemobs.magmacore.location.RegionProtectionProvider;
import com.magmaguy.elitemobs.magmacore.location.api.LocationOwnership;
import com.magmaguy.elitemobs.magmacore.menus.NightbreakSetupIcons;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginSpec;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginUpdater;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.menus.ArenaMenu;
import com.magmaguy.elitemobs.menus.BuyOrSellMenu;
import com.magmaguy.elitemobs.menus.CustomShopMenu;
import com.magmaguy.elitemobs.menus.DynamicDungeonBrowser;
import com.magmaguy.elitemobs.menus.EliteScrollMenu;
import com.magmaguy.elitemobs.menus.GetLootMenu;
import com.magmaguy.elitemobs.menus.InstancedDungeonBrowser;
import com.magmaguy.elitemobs.menus.ItemEnchantmentMenu;
import com.magmaguy.elitemobs.menus.LootMenu;
import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import com.magmaguy.elitemobs.menus.RepairMenu;
import com.magmaguy.elitemobs.menus.ScrapperMenu;
import com.magmaguy.elitemobs.menus.SellMenu;
import com.magmaguy.elitemobs.menus.UnbindMenu;
import com.magmaguy.elitemobs.menus.gambling.BettingMenu;
import com.magmaguy.elitemobs.menus.gambling.BlackjackGame;
import com.magmaguy.elitemobs.menus.gambling.CoinFlipGame;
import com.magmaguy.elitemobs.menus.gambling.HigherLowerGame;
import com.magmaguy.elitemobs.menus.gambling.SlotMachineGame;
import com.magmaguy.elitemobs.mobconstructor.EliteLuaPowerServiceModule;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.elitemobs.mobconstructor.mobdata.PluginMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.chatter.NPCProximitySensor;
import com.magmaguy.elitemobs.npcs.scripts.NPCScriptManager;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolEditor;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerItem;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerManager;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.playerdata.statusscreen.BossTrackingPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.CommandsPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.CoverPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.GearPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.SkillsPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.StatsPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.StatusInventorySafety;
import com.magmaguy.elitemobs.playerdata.statusscreen.TeleportsPage;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.ScriptAction;
import com.magmaguy.elitemobs.powers.scripts.ScriptListener;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import com.magmaguy.elitemobs.powers.specialpowers.SpiritWalkSupport;
import com.magmaguy.elitemobs.powers.specialpowers.TrackingFireballSupport;
import com.magmaguy.elitemobs.powers.specialpowers.ZombieNecronomiconSupport;
import com.magmaguy.elitemobs.powerstances.MajorPowerStanceMath;
import com.magmaguy.elitemobs.powerstances.MinorPowerStanceMath;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.quests.menus.QuestInventoryMenu;
import com.magmaguy.elitemobs.quests.menus.QuestScreenSession;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.scripting.LuaEntityEnricher;
import com.magmaguy.elitemobs.scripting.LuaWorldEnricher;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.HealthDisplayCoordinator;
import com.magmaguy.elitemobs.skills.SkillSystemMigration;
import com.magmaguy.elitemobs.skills.SkillXPBar;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusInitializer;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.testing.SkillSystemTest;
import com.magmaguy.elitemobs.thirdparty.bstats.CustomCharts;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.custommodels.modelengine.ModelEngineReservedAddresses;
import com.magmaguy.elitemobs.thirdparty.placeholderapi.Placeholders;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.transport.TransportModule;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import com.magmaguy.elitemobs.utils.BossBarUtil;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.elitemobs.utils.MessageThrottler;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.elitemobs.wormhole.Wormhole;
import com.magmaguy.elitemobs.wormhole.WormholeManager;
import com.magmaguy.freeminecraftmodels.api.LocationAPI;
import com.magmaguy.shaded.bstats.bukkit.Metrics;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class EliteMobs extends JavaPlugin {
   private int permissionNodes;
   private boolean missingModelsWarningSent;
   public static final NightbreakPluginSpec NIGHTBREAK_PLUGIN_SPEC = new NightbreakPluginSpec(
      "EliteMobs",
      "em",
      "elitemobs.downloadall",
      "elitemobs.setup",
      "elitemobs.setup",
      "https://nightbreak.io/plugin/elitemobs/",
      "Reloaded EliteMobs.",
      ChangelogsConfig::getMessages,
      ChangelogsConfig::getLogoutMessages
   );
   public static List<World> validWorldList = new ArrayList<>();
   public static boolean worldGuardIsEnabled = false;
   public static Metrics metrics;
   public Object placeholders = null;
   private EnchantmentAnvil.Registration enchantmentAnvil;
   private static final String MODELS_PLUGIN = "FreeMinecraftModels";
   private static final long MODEL_REGISTRY_TIMEOUT_MS = 300000L;
   private static final long MODEL_REGISTRY_POLL_MS = 100L;

   public static void initializeConfigs() {
      new CustomModelsConfig();
      new DefaultConfig();
      new ItemSettingsConfig();
      new ProceduralItemGenerationSettingsConfig();
      new ClassLootSettingsConfig();
      new StaticItemNamesConfig();
      PotionEffectsConfig.initializeConfigs();
      new EconomySettingsConfig();
      new EventsConfig();
      new SkillsConfig();
      EliteEnchantmentCatalog.prepare();
      new EnchantmentsConfig();
      new AntiExploitConfig();
      new CombatTagConfig();
      new AntiExploitConfig();
      new AdventurersGuildConfig();
      new ValidWorldsConfig();
      new MenusConfig();
      MobPropertiesConfig.initializeConfigs();
      new MobCombatSettingsConfig();
      CommandsConfig.initializeConfigs();
      new DiscordSRVConfig();
      new PeaceBannerConfig();
      new CustomEventsConfig();
      new QuestsConfig();
      new PartyConfig();
      new WormholesConfig();
      new ArenasConfig();
      new DungeonsConfig();
      new AdvancedCombatSystemConfig();
      new CommandMessagesConfig();
      new ChangelogsConfig();
      new InitializeConfig();
      new SoundsConfig();
      new GamblingConfig();
   }

   public static void worldScanner() {
      for (World world : Bukkit.getWorlds()) {
         if (ValidWorldsConfig.getInstance().getFileConfiguration().getBoolean("validWorlds." + world.getName())) {
            validWorldList.add(world);
         }
      }
   }

   public void onEnable() {
      this.permissionNodes = this.loadPermissionNodes();
      MetadataHandler.pluginState = PluginState.INITIALIZING;
      MetadataHandler.shutdownRequested = false;
      Bukkit.getLogger().info(" _____ _     _____ _____ ________  ______________  _____");
      Bukkit.getLogger().info("|  ___| |   |_   _|_   _|  ___|  \\/  |  _  | ___ \\/  ___|");
      Bukkit.getLogger().info("| |__ | |     | |   | | | |__ | .  . | | | | |_/ /\\ `--.");
      Bukkit.getLogger().info("|  __|| |     | |   | | |  __|| |\\/| | | | | ___ \\ `--. \\");
      Bukkit.getLogger().info("| |___| |_____| |_  | | | |___| |  | \\ \\_/ / |_/ //\\__/ /");
      Bukkit.getLogger().info("\\____/\\_____/\\___/  \\_/ \\____/\\_|  |_/\\___/\\____/ \\____/");
      Bukkit.getLogger().info("By MagmaGuy - v. " + MetadataHandler.PLUGIN.getDescription().getVersion());
      MagmaCore.onEnable(this);
      if (!this.missingModelsWarningSent && !Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         this.missingModelsWarningSent = true;
         Logger.warn(
            "Wands, staves, their enchantments, and the mage class require FreeMinecraftModels. These optional features are unavailable. Download: https://nightbreak.io/plugin/freeminecraftmodels/"
         );
      }

      this.enchantmentAnvil = EnchantmentAnvil.register(
         this,
         item -> null,
         item -> {
            if (ItemTagger.isEliteItem(item)) {
               return "Use the EliteMobs enchanter for elite items.";
            } else {
               if (item.getType() == Material.ENCHANTED_BOOK) {
                  for (Enchantment enchantment : Enchantment.values()) {
                     if (enchantment.getKey().getNamespace().equals("minecraft")
                        && ItemTagger.getEnchantment(item.getItemMeta(), enchantment.getKey()) > enchantment.getMaxLevel()) {
                        return "Use the EliteMobs enchanter for extended native enchantment books.";
                     }
                  }
               }

               return null;
            }
         }
      );
      NightbreakSetupIcons.setAdditionalResourcePackAvailableSupplier(DefaultConfig::useResourcePackModels);
      MagmaCore.exportSharedAssets(this);
      MagmaCore.enableWorldProtections(this);
      InstanceProtector.setContainerAllowlist(block -> TreasureChest.getTreasureChest(block.getLocation()) != null);
      if (VersionChecker.serverVersionOlderThan(21, 0)) {
         Logger.warn(
            "You are running a Minecraft version older than 1.21.0! EliteMobs 9.0 and later are only compatible with Minecraft 1.21.0 or later, if you are running an older Minecraft version you will need to use a pre-9.0 version of EliteMobs."
         );
         MetadataHandler.pluginState = PluginState.UNINITIALIZED;
         Bukkit.getPluginManager().disablePlugin(this);
      } else {
         NMSManager.initializeAdapter(this);
         double configuredMinecraftMaxHealth = Bukkit.getServer().spigot().getConfig().getDouble("settings.attribute.maxHealth.max");
         if (!Double.isFinite(configuredMinecraftMaxHealth) || configuredMinecraftMaxHealth < 2048.0) {
            Bukkit.getServer().spigot().getConfig().set("settings.attribute.maxHealth.max", 2048.0);

            try {
               File spigotConfigContainer = new File(
                  Paths.get(MetadataHandler.PLUGIN.getDataFolder().getParentFile().getCanonicalFile().getParentFile().toString() + "/spigot.yml").toString()
               );
               Bukkit.getServer().spigot().getConfig().save(spigotConfigContainer);
               Logger.info("New default max health set correctly!");
            } catch (IOException var4) {
               Logger.warn(
                  "Failed to save max health value! For the plugin to work correctly, you should increase your max health on the spigot.yml config file to 2048.0"
               );
            }
         }

         CrashFix.startupCheck();
         MagmaCore.startInitialization(
            this, new PluginInitializationConfig("EliteMobs", "elitemobs.*", 34), this::asyncInitialization, this::syncInitialization, () -> {
               if (MetadataHandler.shutdownRequested) {
                  Logger.warn("EliteMobs init cancelled -- server shutting down.");
               } else {
                  MetadataHandler.pluginState = PluginState.INITIALIZED;
                  EventsRegistrer.registerPostInitializationEvents();
                  TransportModule.initialize();
                  Bukkit.getScheduler().runTaskLater(this, () -> {
                     if (!MetadataHandler.shutdownRequested && this.isEnabled()) {
                        AdvancedMagicWeaponItems.register();
                     }
                  }, 40L);
                  Bukkit.getPluginManager().callEvent(new EliteMobsInitializedEvent());
                  Logger.info("EliteMobs fully initialized!");
                  NightbreakPluginUpdater.autoDownloadPluginUpdateIfEnabled(this, NIGHTBREAK_PLUGIN_SPEC);
                  if (MetadataHandler.pendingReloadSender != null) {
                     Logger.sendMessage(MetadataHandler.pendingReloadSender, CommandMessagesConfig.getReloadSuccessMessage());
                     MetadataHandler.pendingReloadSender = null;
                  }
               }
            }, throwable -> {
               EliteEnchantmentCatalog.close();
               MetadataHandler.pluginState = PluginState.UNINITIALIZED;
               MetadataHandler.pendingReloadSender = null;
               EliteMindServiceModule.shutdown();
               EliteLuaPowerServiceModule.shutdown();
               AdvancedCombatModule.shutdownIfInitialized();
               AdvancedCombatRuntime.shutdownIfInitialized();
               AdvancedCombatStateRecovery.clearAllOnlinePlayers();
               throwable.printStackTrace();
            }
         );
      }
   }

   private void asyncInitialization(PluginInitializationContext initializationContext) {
      initializationContext.step("Configs");
      initializeConfigs();

      try {
         Bukkit.getScheduler().callSyncMethod(this, () -> {
            EliteEnchantmentCatalog.publish();
            return null;
         }).get();
      } catch (InterruptedException var3) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("Interrupted publishing enchantments", var3);
      } catch (ExecutionException var4) {
         throw new IllegalStateException("Cannot publish enchantments", var4.getCause());
      }

      initializationContext.step("Database");
      new DatabaseConfig();
      initializationContext.step("Skill Bonuses Config");
      new SkillBonusesConfig();
      initializationContext.step("Events");
      ActionEvent.initializeBlueprintEvents();
      TimedEvent.initializeBlueprintEvents();
      initializationContext.step("Power Stances");
      MinorPowerStanceMath.initializeVectorCache();
      MajorPowerStanceMath.initializeVectorCache();
      initializationContext.step("Configuration Export");
      ConfigurationExporter.initializeConfigs();
      initializationContext.step("Content Importer");
      this.waitForModelRegistryRebuild(MagmaCore.initializeImporter(this));
      initializationContext.step("Powers Config");
      new LuaPowersConfig();
      new PowersConfig();
      new EliteMobPowersConfig();
      initializationContext.step("Mob Properties");
      PluginMobProperties.initializePluginMobValues();
      initializationContext.step("Custom Items");
      new CustomItemsConfig();
      CustomItem.initializeCustomItems(initializationContext::isShutdownRequested);
      initializationContext.step("Loot Tables");
      LootTables.initialize();
      initializationContext.step("Content Packages Config");
      new ContentPackagesConfig();
      initializationContext.step("Custom Bosses Config");
      new CustomBossesConfig();
      initializationContext.step("NPC Scripts");
      NPCScriptManager.initialize();
      initializationContext.step("NPCs Config");
      new NPCsConfig();
      initializationContext.step("Wormholes Config");
      new WormholeConfig();
      initializationContext.step("Arenas Config");
      new CustomArenasConfig();
      initializationContext.step("Quests Config");
      new CustomQuestsConfig();
      initializationContext.step("Special Items Config");
      new SpecialItemSystemsConfig();
   }

   private void waitForModelRegistryRebuild(ConfigurationImporter importer) {
      if (importer != null && importer.isModelsInstalled()) {
         Plugin models = Bukkit.getPluginManager().getPlugin("FreeMinecraftModels");
         if (models != null) {
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(300000L);
            boolean announced = false;

            while (!MagmaCore.isShutdownRequested(this)) {
               PluginInitializationState state = PluginInitializationManager.getState("FreeMinecraftModels");
               if (!models.isEnabled() || state == PluginInitializationState.FAILED) {
                  throw new IllegalStateException("FreeMinecraftModels failed to reload imported content; EliteMobs initialization stopped.");
               }

               if (state == PluginInitializationState.INITIALIZED) {
                  return;
               }

               if (!announced) {
                  Logger.info("Imported new models; waiting for FreeMinecraftModels to rebuild its model registry...");
                  announced = true;
               }

               if (System.nanoTime() >= deadline) {
                  throw new IllegalStateException(
                     "Timed out waiting for FreeMinecraftModels to reload imported content; EliteMobs initialization stopped before loading incomplete models and items."
                  );
               }

               try {
                  Thread.sleep(100L);
               } catch (InterruptedException var8) {
                  Thread.currentThread().interrupt();
                  throw new IllegalStateException("Interrupted while waiting for imported model reload", var8);
               }
            }
         }
      }
   }

   private static void unregisterModelsDungeonLocator() {
      Plugin models = Bukkit.getPluginManager().getPlugin("FreeMinecraftModels");
      if (models != null && models.isEnabled()) {
         try {
            Class.forName("com.magmaguy.freeminecraftmodels.api.LocationAPI", false, models.getClass().getClassLoader())
               .getMethod("unregisterDungeonLocator", String.class)
               .invoke(null, "EliteMobs");
         } catch (NoSuchMethodException | ClassNotFoundException var2) {
         } catch (LinkageError | ReflectiveOperationException var3) {
            Logger.warn("Failed to unregister the EliteMobs dungeon locator from FreeMinecraftModels: " + var3);
         }
      }
   }

   private void syncInitialization(PluginInitializationContext initializationContext) {
      initializationContext.step("Custom Models");
      CustomModel.initialize();
      ModelEngineReservedAddresses.reserve();
      if (worldGuardIsEnabled) {
         Logger.info("WorldGuard compatibility is enabled!");
      } else {
         Logger.info("WorldGuard compatibility is not enabled!");
      }

      initializationContext.step("Economy");

      try {
         VaultCompatibility.vaultSetup();
      } catch (Exception var12) {
         Logger.warn(
            "Something went wrong with the vault configuration - your Vault version is probably not compatible with this EliteMobs version. Please contact the dev about this error."
         );
         VaultCompatibility.VAULT_ENABLED = false;
      }

      initializationContext.step("Event Listeners");
      EventsRegistrer.registerEvents();
      SkillSystemTest.registerRecoveryListeners();
      EliteLuaPowerServiceModule.initialize();
      EliteMindServiceModule.initialize();
      PatrolService.initialize();
      PatrolEditor.initialize();
      initializationContext.step("Player Database");
      PlayerData.initializeDatabaseConnection();
      ElitePlayerInventory.initialize();
      initializationContext.step("Skills System");
      SkillSystemMigration.initialize();
      SkillBonusInitializer.initialize();

      for (Player p : Bukkit.getOnlinePlayers()) {
         if (PlayerData.isDataLoaded(p.getUniqueId())) {
            SkillBonusRegistry.applyAllBonuses(p);
            ArmorSkillHealthBonus.applyHealthBonus(p);
         }
      }

      CombatLevelDisplay.initialize();
      initializationContext.step("Party System");
      PartyManager.initialize();
      initializationContext.step("Gambling System");
      if (GamblingConfig.isGamblingEnabled()) {
         GamblingEconomyHandler.initialize();
         DebtCollectorManager.initialize();
      }

      initializationContext.step("World Scanner");
      worldScanner();
      initializationContext.step("Scheduled Tasks");
      this.launchRunnables();
      MessageThrottler.start();
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         Placeholders placeholders = new Placeholders();
         placeholders.register();
         this.placeholders = placeholders;
      }

      metrics = new Metrics(this, 1081);
      new CustomCharts();
      initializationContext.step("Content Packages");
      ContentPackagesConfig.initializePackages();
      EMDungeonLocator emDungeonLocator = new EMDungeonLocator();
      LocationQueryRegistry.registerDungeonLocator(emDungeonLocator);
      final Predicate<Location> emProtectionFn = loc -> loc != null
         && loc.getWorld() != null
         && EliteMobsWorld.isEliteMobsWorld(loc.getWorld().getUID())
         && InstanceProtector.isProtectedWorld(loc.getWorld());
      Function<Location, Set<String>> emKindsFn = loc -> {
         if (loc != null && loc.getWorld() != null) {
            EMPackage pkg = EMPackage.getContent(loc.getWorld().getName());
            if (!(pkg instanceof WorldPackage)) {
               return Collections.emptySet();
            } else {
               Set<String> kinds = new HashSet<>();
               kinds.add("elitemobs");
               kinds.add("dungeon");
               kinds.add("world_package");
               if (pkg instanceof WorldDungeonPackage) {
                  kinds.add("open_world_dungeon");
               }

               ContentPackagesConfigFields.DungeonSizeCategory size = pkg.getContentPackagesConfigFields().getDungeonSizeCategory();
               if (size != null) {
                  kinds.add(size.name().toLowerCase(Locale.ROOT));
               }

               return kinds;
            }
         } else {
            return Collections.emptySet();
         }
      };
      LocationOwnership.registerTyped(this, "EliteMobs", emKindsFn, emProtectionFn);
      LocationQueryRegistry.registerProtectionProvider(new RegionProtectionProvider() {
         @Override
         public boolean isProtected(Location loc) {
            return emProtectionFn.test(loc);
         }

         @Override
         public String providerName() {
            return "EliteMobs";
         }
      });
      if (Bukkit.getPluginManager().getPlugin("FreeMinecraftModels") != null) {
         try {
            LocationAPI.registerDungeonLocator("EliteMobs", emDungeonLocator::contains);
         } catch (NoClassDefFoundError var11) {
            Logger.warn(
               "Failed to bridge dungeon locator into FreeMinecraftModels — the installed FMM jar is older than 2.4.0 (missing LocationAPI). is_in_dungeon in FMM scripts will not see EliteMobs dungeons."
            );
         }
      }

      LuaEntityEnricher.register();
      LuaWorldEnricher.register();
      initializationContext.step("Custom Bosses");
      CustomBossesConfig.initializeBosses();
      initializationContext.step("Treasure Chests");
      new CustomTreasureChestsConfig();
      initializationContext.step("NPCs");
      NPCsConfig.initializeNPCs();
      initializationContext.step("EM Packages");

      for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
         try {
            if (emPackage.isInstalled()) {
               emPackage.initializeContent();
            }
         } catch (Exception var10) {
            Logger.warn("Failed to load EliteMobs Package " + emPackage.getContentPackagesConfigFields().getFilename() + " !");
            var10.printStackTrace();
         }
      }

      for (EMPackage emPackage : EMPackage.getEmPackages().values()) {
         if (emPackage.isInstalled()) {
            for (Wormhole wormhole : Wormhole.getWormholes()) {
               wormhole.onDungeonInstall(emPackage.getContentPackagesConfigFields().getFilename());
            }
         }
      }

      initializationContext.step("Custom Spawns");

      try {
         new CustomSpawnConfig();
      } catch (Exception var9) {
         Logger.warn(
            "You are using a version of Spigot or a branch thereof (Paper, Purpur, so on) that is (probably) HORRIBLY outdated! This issue will probably be fixed if you update your server version to the latest patch of the version you are running."
         );
         Logger.warn(
            " This does not mean that you have to update your Minecraft version, but it does mean you must update your server version to the latest patch available for that Minecraft version. Download from trustworthy sources, as if you download Spigot from some random website other than Spigot, you are probably not getting the latest version (and also there's a high chance you'll get a virus)."
         );
      }

      initializationContext.step("Commands");
      CommandHandler.registerCommands();
      PeaceBannerManager.loadData();
      PeaceBannerItem.registerRecipe();
      VersionChecker.check();
      DynamicQuest.startRandomizingQuests();
      CustomBossEntity.startUpdatingDynamicLevels();
      EntityTracker.managedEntityWatchdog();
      WormholeManager.getInstance(false);
      initializationContext.step("Item Cache");
      CustomItem.regenerateCachedItemStacks();
   }

   public void onLoad() {
      MetadataHandler.PLUGIN = this;
      MagmaCore.createInstance(this, NIGHTBREAK_PLUGIN_SPEC);
      worldGuardIsEnabled = false;
      if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
         try {
            worldGuardIsEnabled = WorldGuardCompatibility.initialize();
         } catch (IllegalStateException | NoClassDefFoundError var2) {
            Logger.warn("Error loading WorldGuard. EliteMobs-specific flags are unavailable: " + var2);
         }
      }
   }

   public void onDisable() {
      EliteEnchantmentCatalog.close();
      MetadataHandler.shutdownRequested = true;
      if (metrics != null) {
         metrics.shutdown();
         metrics = null;
      }

      unregisterModelsDungeonLocator();
      if (this.enchantmentAnvil != null) {
         this.enchantmentAnvil.close();
         this.enchantmentAnvil = null;
      }

      CustomItem.shutdownCacheRegeneration();
      LanguageCommand.shutdown();
      ClassSelectionMenu.shutdown();
      MagmaCore.requestInitializationShutdown(this);
      PatrolEditor.shutdown();
      TransportModule.shutdown();
      PatrolService.shutdown();
      AdvancedCombatModule.shutdownIfInitialized();
      EliteMindServiceModule.shutdown();
      EliteLuaPowerServiceModule.shutdown();
      AutoclickerThrottle.shutdown();
      SpiritWalkSupport.shutdown();
      CombatTag.shutdown();
      ZombieNecronomiconSupport.shutdown();
      if (MetadataHandler.pluginState == PluginState.INITIALIZING) {
         Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
         MetadataHandler.pluginState = PluginState.UNINITIALIZED;
         HealthDisplayCoordinator.shutdown();
         MagmaCore.shutdown(this);
         Logger.info("EliteMobs shutdown during init -- cancelled async loading.");
      } else if (MetadataHandler.pluginState == PluginState.UNINITIALIZED) {
         HealthDisplayCoordinator.shutdown();
         MagmaCore.shutdown(this);
      } else {
         Logger.info("Starting EliteMobs shutdown sequence...");
         Explosion.shutdown();
         PeaceBannerManager.shutdown();
         PartyManager.shutdown();
         Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
         Wormhole.shutdown();
         RegionalBossEntity.flushOnShutdown();
         InstancedBossEntity.shutdown();
         NPCEntity.shutdown();
         PersistentObjectHandler.shutdown();
         EntityTracker.wipeShutdown();
         TimedEvent.shutdown();
         ActionEvent.shutdown();
         PeaceBannerItem.unregisterRecipe();
         validWorldList.clear();
         CustomBossesConfigFields.getRegionalElites().clear();
         CustomItem.getCustomItems().clear();
         CustomItem.getCustomItemStackList().clear();
         CustomItem.getCustomItemStackShopList().clear();
         CustomItem.getLimitedItems().clear();
         CustomItem.getScalableItems().clear();
         CustomItem.getFixedItems().clear();
         CustomItem.getTieredLoot().clear();
         CustomItem.getWeighedFixedItems().clear();
         EMPackage.shutdown();
         RegionalBossEntity.regionalBossesShutdown();
         if (this.placeholders != null) {
            ((Placeholders)this.placeholders).unregister();
         }

         PlayerPotionEffects.shutdown();
         HandlerList.unregisterAll(MetadataHandler.PLUGIN);
         TreasureChest.shutdown();
         WorldOperationQueue.shutdown();
         MatchInstance.shutdown();
         DynamicQuest.shutdown();
         ProceduralShopMenu.shutdown();
         EliteMobsWorld.shutdown();
         Navigation.shutdown();
         QuestDialogueBossBarManager.shutdown();
         BossBarUtil.shutdown();
         SkillSystemTest.shutdown();
         ScriptAction.shutdown();
         CustomMusic.shutdown();
         CustomBossEntity.shutdown();
         BossHealthDisplay.shutdown();
         ItemLootShower.shutdown();
         MessageThrottler.shutdown();
         TrackingFireballSupport.shutdown();
         VersionChecker.shutdown();
         KeepNeutralsAngry.shutdown();
         SharedLootTable.shutdown();
         LootMenu.shutdown();
         TeleportsPage.TeleportsPageEvents.shutdown();
         BossTrackingPage.BossTrackingPageEvents.shutdown();
         PlayerDeathMessageByEliteMob.shutdown();
         NPCInteractions.shutdown();
         GearRestrictionHandler.shutdown();
         FarmingProtection.shutdown();
         NPCProximitySensor.shutdown();
         PlayerQuestCooldowns.shutdown();
         GetLootMenu.shutdown();
         TransitiveBlockCommand.shutdown();
         Saturation.shutdown();
         Heal.shutdown();
         Harm.shutdown();
         ScriptListener.shutdown();
         CombatEnterScanPower.shutdown();
         QuestTracking.shutdown();
         CustomShopMenu.shutdown();
         SellMenu.shutdown();
         RepairMenu.shutdown();
         ScrapperMenu.shutdown();
         UnbindMenu.shutdown();
         BettingMenu.shutdown();
         BlackjackGame.shutdown();
         CoinFlipGame.shutdown();
         HigherLowerGame.shutdown();
         SlotMachineGame.shutdown();
         DebtCollectorManager.shutdown();
         GamblingEconomyHandler.shutdown();
         DynamicDungeonBrowser.shutdown();
         InstancedDungeonBrowser.shutdown();
         BuyOrSellMenu.BuyOrSellMenuEvents.shutdown();
         Quest.shutdown();
         QuestInventoryMenu.shutdown();
         SimpleScoreboard.shutdown();
         QuestScreenSession.shutdown();
         StatsPage.StatsPageEvents.shutdown();
         GearPage.GearPageEvents.shutdown();
         CommandsPage.CommandsPageEvents.shutdown();
         CoverPage.CoverPageEvents.shutdown();
         SkillsPage.SkillsPageEvents.shutdown();
         StatusInventorySafety.shutdown();
         SkillSystemMigration.shutdown();
         SkillBonusInitializer.shutdown();
         SkillXPBar.shutdown();
         CombatLevelDisplay.shutdown();
         AdvancedCombatRuntime.shutdownIfInitialized();
         HealthDisplayCoordinator.shutdown();
         ActionBarCompositor.shutdown();
         DungeonCombatRuntime.shutdownIfInitialized();
         BossBarOrderManager.shutdown();
         ArenaMenu.ArenaMenuEvents.shutdown();
         ItemEnchantmentMenu.ItemEnchantMenuEvents.shutdown();
         EliteScrollMenu.EliteScrollMenuEvents.shutdown();
         CrashFix.shutdown();
         RemoveCommand.shutdown();
         InstanceProtector.shutdown();
         ConfigurationLocation.shutdown();
         ElitePlayerInventory.shutdown();
         KeepNeutralsAngry.shutdown();
         Navigation.shutdown();
         PlayerAttackCooldownTracker.shutdown();
         GameClock.shutdown();
         DynamicQuest.shutdown();
         DungeonInstance.shutdown();
         ArenaInstance.shutdown();
         LuaPowerManager.shutdown();
         NPCScriptManager.shutdown();
         ElitePower.shutdown();
         EliteScriptBlueprint.shutdown();
         EliteMobProperties.shutdown();
         Logger.info("Saving EliteMobs databases...");
         PlayerData.closeConnection();
         MagmaCore.shutdown(this);
         MetadataHandler.pluginState = PluginState.UNINITIALIZED;
         Logger.info("All done! Good night.");
      }
   }

   private void launchRunnables() {
      GameClock.initialize();
      RegionalBossEntity.regionalDataSaver();
   }
}
