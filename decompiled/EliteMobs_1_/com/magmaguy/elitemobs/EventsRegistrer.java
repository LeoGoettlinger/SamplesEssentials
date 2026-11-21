package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatRuntime;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatStateRecovery;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatSuggestion;
import com.magmaguy.elitemobs.advancedcombat.menu.ClassSelectionMenu;
import com.magmaguy.elitemobs.api.ArenaCompleteEvent;
import com.magmaguy.elitemobs.api.EliteExplosionEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEventFilter;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestLeaveEvent;
import com.magmaguy.elitemobs.api.QuestObjectivesCompletedEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.AlternativeDurabilityLoss;
import com.magmaguy.elitemobs.collateralminecraftchanges.CombustionPrevention;
import com.magmaguy.elitemobs.collateralminecraftchanges.EliteBlazeWaterDamagePrevention;
import com.magmaguy.elitemobs.collateralminecraftchanges.EliteSlimeDeathSplit;
import com.magmaguy.elitemobs.collateralminecraftchanges.EnderCrystalDamageProtectionBypass;
import com.magmaguy.elitemobs.collateralminecraftchanges.EnderDragonUnstuck;
import com.magmaguy.elitemobs.collateralminecraftchanges.EntityTransformHandler;
import com.magmaguy.elitemobs.collateralminecraftchanges.FindNewWorlds;
import com.magmaguy.elitemobs.collateralminecraftchanges.FixPlayerOnLoginOrRespawn;
import com.magmaguy.elitemobs.collateralminecraftchanges.ItemDisenchantPrevention;
import com.magmaguy.elitemobs.collateralminecraftchanges.ItemEnchantmentPrevention;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningImmunity;
import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.collateralminecraftchanges.NPCsBecomeWitches;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerQuitCleanup;
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventEliteBeeHiveEnter;
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventEliteEquipmentDrop;
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventEliteSilverfishBlockEnter;
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventItemPickupByMobs;
import com.magmaguy.elitemobs.collateralminecraftchanges.PreventUpgradeDiamondToNetherite;
import com.magmaguy.elitemobs.collateralminecraftchanges.TamePrevention;
import com.magmaguy.elitemobs.collateralminecraftchanges.VanillaReinforcementsCanceller;
import com.magmaguy.elitemobs.combatsystem.EliteMobDamagedByEliteMobHandler;
import com.magmaguy.elitemobs.combatsystem.EliteMobGenericDamagedHandler;
import com.magmaguy.elitemobs.combatsystem.antiexploit.AmbientDamageExploit;
import com.magmaguy.elitemobs.combatsystem.antiexploit.EliteMobDamagedByPlayerAntiExploitListener;
import com.magmaguy.elitemobs.combatsystem.antiexploit.HoneyBlockJumpExploit;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventDarkroomExploit;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventEndermanHeightExploit;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventLargeDarkroomExploit;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventPathfindingExploit;
import com.magmaguy.elitemobs.combatsystem.combattag.CombatTag;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.combatsystem.displays.BossHealthDisplay;
import com.magmaguy.elitemobs.commands.admin.RemoveCommand;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.config.AntiExploitConfig;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.dungeons.DungeonBossLockoutHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.ActionEvent;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.gambling.DebtCollectorManager;
import com.magmaguy.elitemobs.initialsetup.FirstTimeSetup;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonKillTargetObjective;
import com.magmaguy.elitemobs.items.DefaultDropsHandler;
import com.magmaguy.elitemobs.items.ItemLootShower;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.PlaceEventPrevent;
import com.magmaguy.elitemobs.items.RareDropEffect;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.potioneffects.PlayerPotionEffects;
import com.magmaguy.elitemobs.menus.ArenaMenu;
import com.magmaguy.elitemobs.menus.ArrowShopMenu;
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
import com.magmaguy.elitemobs.menus.SkillBonusMenu;
import com.magmaguy.elitemobs.menus.UnbindMenu;
import com.magmaguy.elitemobs.menus.gambling.BettingMenu;
import com.magmaguy.elitemobs.menus.gambling.BlackjackGame;
import com.magmaguy.elitemobs.menus.gambling.CoinFlipGame;
import com.magmaguy.elitemobs.menus.gambling.HigherLowerGame;
import com.magmaguy.elitemobs.menus.gambling.SlotMachineGame;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossDeath;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossTaunts;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.mobconstructor.custombosses.FrozenCustomBossKnockbackHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBossBlock;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.npcs.NPCDamageEvent;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.npcs.chatter.NPCProximitySensor;
import com.magmaguy.elitemobs.ondeathcommands.OnDeathCommands;
import com.magmaguy.elitemobs.parties.PartyInteractionHint;
import com.magmaguy.elitemobs.parties.PartyInventoryMenu;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerListener;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.PlayerStatsTracker;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.playerdata.statusscreen.BossTrackingPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.CommandsPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.CoverPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.GearPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.SkillsPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.StatsPage;
import com.magmaguy.elitemobs.playerdata.statusscreen.StatusInventorySafety;
import com.magmaguy.elitemobs.playerdata.statusscreen.TeleportsPage;
import com.magmaguy.elitemobs.powers.BonusCoins;
import com.magmaguy.elitemobs.powers.ProjectileDamage;
import com.magmaguy.elitemobs.powers.lua.LuaPowerEvents;
import com.magmaguy.elitemobs.powers.meta.CombatEnterScanPower;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.elitemobs.powers.scripts.ScriptListener;
import com.magmaguy.elitemobs.powers.specialpowers.EnderCrystalLightningRod;
import com.magmaguy.elitemobs.powers.specialpowers.SpiritWalkSupport;
import com.magmaguy.elitemobs.powers.specialpowers.TrackingFireballSupport;
import com.magmaguy.elitemobs.powers.specialpowers.ZombieNecronomiconSupport;
import com.magmaguy.elitemobs.powerstances.EffectEventHandlers;
import com.magmaguy.elitemobs.powerstances.VisualEffectObfuscator;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.QuestLockoutHandler;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.quests.menus.QuestInventoryMenu;
import com.magmaguy.elitemobs.quests.menus.QuestScreenSession;
import com.magmaguy.elitemobs.quests.objectives.ArenaObjective;
import com.magmaguy.elitemobs.quests.objectives.ClassUnlockObjective;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldownsLogout;
import com.magmaguy.elitemobs.skills.CombatLevelDisplay;
import com.magmaguy.elitemobs.skills.SkillSystemMigration;
import com.magmaguy.elitemobs.skills.SkillXPBar;
import com.magmaguy.elitemobs.skills.SkillXPHandler;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusEventHandler;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardDungeonFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardEliteMobOnlySpawnFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardExplosionBlockDamageFlag;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import com.magmaguy.elitemobs.wormhole.WormholePlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class EventsRegistrer {
   private static PluginManager pluginManager;
   private static Plugin plugin;

   private EventsRegistrer() {
   }

   public static void registerEvents() {
      pluginManager = Bukkit.getPluginManager();
      plugin = MetadataHandler.PLUGIN;
      register(new FirstTimeSetup());
      register(new AdvancedCombatSuggestion());
      register(new Navigation());
      register(new DungeonKillTargetObjective.DungeonKillTargetObjectiveListener());
      register(new VersionChecker.VersionCheckerEvents());
      register(new PlayerData.PlayerDataEvents());
      register(new ElitePlayerInventory.ElitePlayerInventoryEvents());
      register(new PlayerStatsTracker());
      register(new SkillXPHandler());
      register(new SkillXPBar());
      register(new SkillSystemMigration.MigrationEvents());
      register(new CombatLevelDisplay());
      register(new SkillBonusEventHandler());
      register(new AdvancedCombatStateRecovery());
      register(new SkillBonusMenu.SkillBonusMenuEvents());
      register(new PlayerQuestCooldownsLogout());
      if (ItemSettingsConfig.isPreventEliteItemEnchantment()) {
         register(new ItemEnchantmentPrevention());
      }

      if (ItemSettingsConfig.isPreventEliteItemDisenchantment()) {
         register(new ItemDisenchantPrevention());
      }

      if (ItemSettingsConfig.isPreventEliteItemDiamondToNetheriteUpgrade()) {
         register(new PreventUpgradeDiamondToNetherite());
      }

      if (ItemSettingsConfig.isUseEliteItemScrolls()) {
         register(new EliteScrollMenu.EliteScrollMenuEvents());
      }

      register(new FixPlayerOnLoginOrRespawn());
      register(new PlayerQuitCleanup());
      if (PartyConfig.isEnabled()) {
         register(new PartyManager());
         register(new PartyInventoryMenu());
         if (PartyConfig.isPlayerInteractionHintsEnabled()) {
            register(new PartyInteractionHint());
         }
      }

      register(new WormholePlayerListener());
      register(new EliteMobGenericDamagedHandler());
      register(new EliteMobDamagedByEliteMobHandler());
      if (MobCombatSettingsConfig.isEnableDeathMessages()) {
         register(new PlayerDeathMessageByEliteMob());
      }

      if (DefaultConfig.isDoExplosionRegen()) {
         register(new Explosion.ExplosionEvent());
      }

      register(new DefaultDropsHandler());
      register(new ItemLootShower.ItemLootShowerEvents());
      register(new PlayerPotionEffects());
      register(new GetLootMenu.GetLootMenuListener());
      register(new EliteMobDeathEvent.EliteMobDeathEventFilter());
      register(new EliteMobTargetPlayerEvent.EliteMobTargetPlayerEventFilter());
      register(new EliteMobDamagedEvent.EliteMobDamageEventFilter());
      register(new PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter());
      register(new EliteMobDamagedByEliteMobEvent.EliteMobDamagedByEliteMobFilter());
      register(new EliteMobEnterCombatEvent.EliteMobEnterCombatEventFilter());
      register(new PlayerPreTeleportEvent.PlayerPreTeleportEventEvents());
      register(new PlayerTeleportEvent.PlayerTeleportEventExecutor());
      register(new EliteMobDamagedByPlayerEventFilter());
      register(new EliteExplosionEvent.EliteExplosionEvents());
      register(new ScriptListener());
      register(new LuaPowerEvents());
      registerPower(new TrackingFireballSupport.Events(), "tracking_fireball.yml");
      if (EnchantmentsConfig.getEnchantment(SoulbindEnchantment.key + ".yml").isEnabled()) {
         register(new SoulbindEnchantment.SoulbindEnchantmentEvents());
      }

      register(new CustomSummonPower.CustomSummonPowerEvent());
      register(new CombatEnterScanPower.MajorCombatEnterScanningPowerEvents());
      registerPower(new BonusCoins.BonusCoinsEvents(), "bonus_coins.yml");
      register(new EnderCrystalLightningRod.EnderCrystalLightningRodEvents());
      register(new SpiritWalkSupport());
      register(new ZombieNecronomiconSupport());
      register(new EliteScript.EliteScriptEvents());
      register(new CustomBossEntity.CustomBossEntityEvents());
      register(new FrozenCustomBossKnockbackHandler());
      register(new CustomBossDeath());
      register(new PersistentObjectHandler.PersistentObjectHandlerEvents());
      register(new CustomBossTaunts());
      register(new PhaseBossEntity.PhaseBossEntityListener());
      register(new TransitiveBossBlock());
      register(new TransitiveBlockCommand.TemporaryBossBlockCommandEvents());
      register(new CustomModel.ModelEntityEvents());
      register(new MetadataHandler());
      register(new EntityTracker());
      register(new CrashFix());
      register(new NaturalMobSpawnEventHandler());
      if (PeaceBannerConfig.isEnabled()) {
         register(new PeaceBannerListener());
      }

      register(new EffectEventHandlers());
      if (MobCombatSettingsConfig.isObfuscateMobPowers()) {
         register(new VisualEffectObfuscator());
      }

      if (ItemSettingsConfig.isDoEliteMobsLoot()) {
         register(new LootTables());
         register(new PlaceEventPrevent());
      }

      register(new StatusInventorySafety());
      register(new CoverPage.CoverPageEvents());
      if (AdvancedCombatSystemConfig.isEnabled()) {
         register(ClassSelectionMenu.listener());
      }

      register(new StatsPage.StatsPageEvents());
      register(new SkillsPage.SkillsPageEvents());
      register(new GearPage.GearPageEvents());
      register(new TeleportsPage.TeleportsPageEvents());
      register(new CommandsPage.CommandsPageEvents());
      register(new BossTrackingPage.BossTrackingPageEvents());
      register(new InstancedDungeonBrowser.InstancedDungeonBrowserEvents());
      register(new DynamicDungeonBrowser.DynamicDungeonBrowserEvents());
      register(new ProceduralShopMenu.ProceduralShopMenuEvents());
      register(new CustomShopMenu.CustomShopMenuEvents());
      register(new BuyOrSellMenu.BuyOrSellMenuEvents());
      register(new SellMenu());
      register(new ScrapperMenu.ScrapperMenuEvents());
      register(new RepairMenu.RepairMenuEvents());
      register(new UnbindMenu.UnbinderMenuEvents());
      register(new ItemEnchantmentMenu.ItemEnchantMenuEvents());
      register(new ArrowShopMenu.ArrowShopMenuEvents());
      register(new BettingMenu.BettingMenuEvents());
      register(new BlackjackGame.BlackjackMenuEvents());
      register(new CoinFlipGame.CoinFlipMenuEvents());
      register(new HigherLowerGame.HigherLowerMenuEvents());
      register(new SlotMachineGame.SlotMachineMenuEvents());
      register(new DebtCollectorManager());
      register(new LootMenu.LootMenuEvents());
      register(new PreventEliteBeeHiveEnter());
      register(new EnderDragonUnstuck());
      if (DefaultConfig.isPreventVanillaReinforcementsForEliteEntities()) {
         register(new VanillaReinforcementsCanceller());
      }

      register(new LightningSpawnBypass());
      register(new ProjectileDamage.ViewerEvents());
      if (ItemSettingsConfig.isEliteDurability()) {
         register(new AlternativeDurabilityLoss());
      }

      register(new EnderCrystalDamageProtectionBypass());
      register(new NPCsBecomeWitches());
      register(new PreventEliteSilverfishBlockEnter());
      register(new PreventMountExploit());
      register(new PreventDarkroomExploit());
      register(new PreventLargeDarkroomExploit());
      register(new PreventEndermanHeightExploit());
      if (AntiExploitConfig.isNoItemPickup()) {
         register(new PreventItemPickupByMobs());
      }

      if (AntiExploitConfig.isAmbientDamageExploit()) {
         register(new AmbientDamageExploit());
      }

      register(new HoneyBlockJumpExploit());
      register(new EliteMobDamagedByPlayerAntiExploitListener());
      if (AntiExploitConfig.isNoPathExploit()) {
         register(new PreventPathfindingExploit());
      }

      register(new ActionEvent.ActionEventEvents());
      BossHealthDisplay bossHealthDisplay = new BossHealthDisplay();
      register(bossHealthDisplay);
      BossHealthDisplay.startMasterUpdateTask();
      register(new BossBarOrderManager.BossBarOrderManagerEvents());
      register(new KillObjective.KillObjectiveEvents());
      register(new CustomFetchObjective.CustomFetchObjectiveEvents());
      register(new ArenaObjective.ArenaObjectiveEvents());
      register(new QuestAcceptEvent.QuestAcceptEventHandler());
      register(new ClassUnlockObjective.Events());
      register(new QuestCompleteEvent.QuestCompleteEventHandler());
      register(new QuestObjectivesCompletedEvent.QuestObjectivesCompletedEventHandler());
      register(new QuestLeaveEvent.QuestLeaveEventHandler());
      register(new QuestProgressionEvent.QuestProgressionEventHandler());
      register(new QuestTracking.QuestTrackingEvents());
      register(new CustomQuest.CustomQuestEvents());
      register(new QuestDialogueBossBarManager.QuestDialogueBossBarEvents());
      register(new QuestInventoryMenu.QuestInventoryMenuEvents());
      register(new SimpleScoreboard.Events());
      register(new QuestScreenSession());
      register(new ArenaCompleteEvent.ArenaCompleteEventHandler());
      register(new CustomMusic.CustomMusicEvents());
      register(new ArenaMenu.ArenaMenuEvents());
      register(new ArenaInstance.ArenaInstanceEvents());
      register(new ArenaInstance.ArenaInstanceLoader());
      register(new MatchInstance.MatchInstanceEvents());
      if (CombatTagConfig.isEnableCombatTag()) {
         register(new CombatTag());
      }

      register(new EnderDragonUnstuck.AggroPrevention());
      register(new TamePrevention());
      register(new RareDropEffect());
      register(new NPCDamageEvent());
      register(new NPCInteractions());
      register(new NPCProximitySensor());
      register(new FindNewWorlds());
      register(new WorldGuardSpawnEventBypasser());
      if (EliteMobs.worldGuardIsEnabled) {
         register(new WorldGuardEliteMobOnlySpawnFlag());
         register(new WorldGuardDungeonFlag());
         register(new WorldGuardExplosionBlockDamageFlag());
      }

      register(new DungeonBossLockoutHandler());
      register(new QuestLockoutHandler());
      register(new EntityTransformHandler());
      register(new EliteBlazeWaterDamagePrevention());
      register(new PreventEliteEquipmentDrop());
      register(new CombustionPrevention());
      register(new EliteSlimeDeathSplit());
      register(new LightningImmunity());
      register(new TreasureChest.TreasureChestEvents());
      register(new OnDeathCommands());
      register(new RemoveCommand.RemoveCommandEvents());
   }

   private static void registerPower(Listener listener, String config) {
      if (PowersConfig.getPower(config).isEnabled()) {
         register(listener);
      }
   }

   public static void registerPostInitializationEvents() {
      ActionBarCompositor.initialize();

      for (Player player : Bukkit.getOnlinePlayers()) {
         AdvancedCombatStateRecovery.reconcile(player);
      }

      if (DungeonsConfig.isEnableDungeonFoodRegeneration() || AdvancedCombatSystemConfig.isEnabled()) {
         DungeonCombatRuntime dungeonCombatRuntime = new DungeonCombatRuntime();
         CombatLevelDisplay.setCombatState(dungeonCombatRuntime);
         register(dungeonCombatRuntime);
         dungeonCombatRuntime.start();
         if (AdvancedCombatSystemConfig.isEnabled()) {
            AdvancedCombatRuntime advancedCombatRuntime = new AdvancedCombatRuntime(dungeonCombatRuntime);
            register(advancedCombatRuntime);
            advancedCombatRuntime.start();
            AdvancedCombatModule.initialize(dungeonCombatRuntime);
         }
      }
   }

   private static void register(Listener listener) {
      pluginManager.registerEvents(listener, plugin);
   }
}
