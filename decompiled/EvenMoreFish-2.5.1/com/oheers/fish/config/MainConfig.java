package com.oheers.fish.config;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.serializer.BossBarOverlaySerializer;
import com.oheers.fish.api.economy.EconomyType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.boostedyaml.settings.updater.UpdaterSettings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MainConfig extends ConfigBase {
   private static MainConfig instance = null;
   private final boolean adminShortcutEnabled;
   private final String adminShortcutName;
   private final String mainCommandName;
   private final List<String> mainCommandAliases;
   private final String adminSubCommandName;
   private final String nextSubCommandName;
   private final String toggleSubCommandName;
   private final String guiSubCommandName;
   private final String helpSubCommandName;
   private final String topSubCommandName;
   private final String shopSubCommandName;
   private final String sellAllSubCommandName;
   private final String applyBaitsSubCommandName;
   private final String journalSubCommandName;
   private Map<String, List<Biome>> biomeSets;
   private Map<String, Map<String, Double>> regionBoosts;

   public MainConfig() {
      super("config.yml", "config.yml", EvenMoreFish.getInstance(), true);
      instance = this;
      this.mainCommandName = this.getConfig().getString("command.main", "emf");
      this.mainCommandAliases = this.getConfig().getStringList("command.aliases");
      this.adminShortcutEnabled = this.getConfig().getBoolean("command.admin-shortcut.enabled", true);
      this.adminShortcutName = this.getConfig().getString("command.admin-shortcut.name", "emfa");
      this.adminSubCommandName = this.getConfig().getString("command.subcommands.admin", "admin");
      this.nextSubCommandName = this.getConfig().getString("command.subcommands.next", "next");
      this.toggleSubCommandName = this.getConfig().getString("command.subcommands.toggle", "toggle");
      this.guiSubCommandName = this.getConfig().getString("command.subcommands.gui", "gui");
      this.helpSubCommandName = this.getConfig().getString("command.subcommands.help", "help");
      this.topSubCommandName = this.getConfig().getString("command.subcommands.top", "top");
      this.shopSubCommandName = this.getConfig().getString("command.subcommands.shop", "shop");
      this.sellAllSubCommandName = this.getConfig().getString("command.subcommands.sellall", "sellall");
      this.applyBaitsSubCommandName = this.getConfig().getString("command.subcommands.applybaits", "applybaits");
      this.journalSubCommandName = this.getConfig().getString("command.subcommands.journal", "journal");
      this.biomeSets = this.loadBiomeSets();
      this.regionBoosts = this.loadRegionBoosts();
   }

   @Override
   public void reload() {
      super.reload();
      DimensionFishingConfig.getInstance().reload();
      this.biomeSets = this.loadBiomeSets();
      this.regionBoosts = this.loadRegionBoosts();
   }

   public static MainConfig getInstance() {
      return instance;
   }

   public String getLocale() {
      return this.getConfig().getString("locale", "en");
   }

   public boolean doingRandomDurability() {
      return this.getConfig().getBoolean("random-durability", true);
   }

   @Deprecated
   public boolean isDatabaseOnline() {
      return this.databaseEnabled() && !EvenMoreFish.getInstance().getPluginDataManager().getDatabase().isLegacyDatabase()
         ? EvenMoreFish.getInstance().getPluginDataManager().getDatabase() != null
         : false;
   }

   public boolean isCatchEnabled() {
      return this.getConfig().getBoolean("fishing.catch-enabled", true);
   }

   public boolean isFishCatchOnlyInCompetition() {
      return this.getConfig().getBoolean("fishing.catch-only-in-competition", false);
   }

   public boolean isGiveStraightToInventory() {
      return this.getConfig().getBoolean("fishing.give-straight-to-inventory", false);
   }

   public boolean isFishCatchOverrideOnlyFish() {
      return this.getConfig().getBoolean("fishing.only-fish", false);
   }

   public boolean isHuntEnabled() {
      return this.getConfig().getBoolean("fishing.hunt-enabled", false);
   }

   public boolean isFishHuntOnlyInCompetition() {
      return this.getConfig().getBoolean("fishing.hunt-only-in-competition", true);
   }

   public boolean isFishHuntIgnoreSpawnerFish() {
      return this.getConfig().getBoolean("fishing.hunt-ignore-spawner-fish", true);
   }

   public boolean getEnabled() {
      return this.getConfig().getBoolean("enabled", true);
   }

   public List<String> getAllowedRegions() {
      return this.getConfig().getStringList("allowed-regions");
   }

   public List<String> getAllowedWorlds() {
      return this.getConfig().getStringList("allowed-worlds");
   }

   public boolean shouldRespectVanish() {
      return this.getConfig().getBoolean("respect-vanished", true);
   }

   public boolean shouldProtectBaitedRods() {
      return this.getConfig().getBoolean("protect-baited-rods", true);
   }

   public Overlay getBarStyle() {
      String styleString = this.getConfig().getString("barstyle");
      return BossBarOverlaySerializer.get().deserialize(styleString);
   }

   public boolean sellOverDrop() {
      return this.getConfig().getBoolean("sell-gui.sell-over-drop", false);
   }

   public boolean disableMcMMOTreasure() {
      return this.getConfig().getBoolean("disable-mcmmo-loot", true);
   }

   public boolean disableAuraSkills() {
      return this.getConfig().getBoolean("disable-auraskills-loot", true);
   }

   public boolean doDBVerbose() {
      return !this.getConfig().getBoolean("database.disable-verbose", false);
   }

   public boolean requireCustomRod() {
      return this.getConfig().getBoolean("fishing.require-custom-rod", false);
   }

   public boolean requireFishingPermission() {
      return this.getConfig().getBoolean("requires-fishing-permission", false);
   }

   public boolean preventCrafting() {
      return this.getConfig().getBoolean("item-protection.prevent-crafting", true);
   }

   public boolean preventConsume() {
      return this.getConfig().getBoolean("item-protection.prevent-consume", true);
   }

   public boolean preventFurnaceBurn() {
      return this.getConfig().getBoolean("item-protection.prevent-furnace-burn", true);
   }

   public boolean preventCooking() {
      return this.getConfig().getBoolean("item-protection.prevent-cooking", true);
   }

   public boolean preventPlacing() {
      return this.getConfig().getBoolean("item-protection.prevent-placing", true);
   }

   public boolean shouldDebug() {
      return this.getConfig().getBoolean("debug", false);
   }

   public boolean databaseEnabled() {
      return this.getConfig().getBoolean("database.enabled", false);
   }

   public String getAddress() {
      return this.getConfig().getString("database.address", "localhost");
   }

   public String getDatabase() {
      return this.getConfig().getString("database.database", "database");
   }

   public String getUsername() {
      return this.getConfig().getString("database.username", "root");
   }

   public String getPassword() {
      return this.getConfig().getString("database.password", "");
   }

   public String getPrefix() {
      return this.getConfig().getString("database.prefix", "emf_");
   }

   public String getDatabaseType() {
      return this.getConfig().getString("database.type", "sqlite");
   }

   public boolean isCompetitionBackupEnabled() {
      return this.getConfig().getBoolean("competition-backup.enabled", false);
   }

   public int getCompetitionBackupInterval() {
      return this.getConfig().getInt("competition-backup.interval", 5);
   }

   public String getSaveIntervalUnit() {
      return this.getConfig().getString("database.advanced.save-interval.units", "SECONDS");
   }

   public int getCompetitionSaveInterval() {
      return this.getSaveInterval("competition");
   }

   public int getUserFishStatsSaveInterval() {
      return this.getSaveInterval("user-fish-stats");
   }

   private int getSaveInterval(final String path) {
      return this.getConfig().getInt("database.advanced.save-interval." + path, 5);
   }

   public boolean isDisableJooqStartupCommments() {
      return this.getConfig().getBoolean("database.advanced.disable-jooq-startup-comments", true);
   }

   public boolean isJooqExecuteLoggingEnabled() {
      return this.getConfig().getBoolean("database.advanced.jooq-execute-logging", false);
   }

   public boolean isJooqRenderFormattedEnabled() {
      return this.getConfig().getBoolean("database.advanced.jooq-render-formatted", false);
   }

   public boolean useAdditionalAddons() {
      return this.getConfig().getBoolean("addons.additional-addons", true);
   }

   public int getNearbyPlayersRequirementRange() {
      return this.getConfig().getInt("requirements.nearby-players.range", 20);
   }

   public boolean isAdminShortcutCommandEnabled() {
      return this.adminShortcutEnabled;
   }

   @NonNull
   public String getAdminShortcutCommandName() {
      return this.adminShortcutName;
   }

   public String getMainCommandName() {
      return this.mainCommandName;
   }

   public List<String> getMainCommandAliases() {
      return this.mainCommandAliases;
   }

   public String getAdminSubCommandName() {
      return this.adminSubCommandName;
   }

   public String getNextSubCommandName() {
      return this.nextSubCommandName;
   }

   public String getToggleSubCommandName() {
      return this.toggleSubCommandName;
   }

   public String getGuiSubCommandName() {
      return this.guiSubCommandName;
   }

   public String getHelpSubCommandName() {
      return this.helpSubCommandName;
   }

   public String getTopSubCommandName() {
      return this.topSubCommandName;
   }

   public String getShopSubCommandName() {
      return this.shopSubCommandName;
   }

   public String getSellAllSubCommandName() {
      return this.sellAllSubCommandName;
   }

   public String getApplyBaitsSubCommandName() {
      return this.applyBaitsSubCommandName;
   }

   public String getJournalSubCommandName() {
      return this.journalSubCommandName;
   }

   public boolean useOldBaseCommandBehavior() {
      return this.getConfig().getBoolean("command.old-base-command-behavior", false);
   }

   private Map<String, List<Biome>> loadBiomeSets() {
      Map<String, List<Biome>> biomeSetMap = new HashMap<>();
      Section section = this.getConfig().getSection("biome-sets");
      if (section == null) {
         return Map.of();
      } else {
         section.getRoutesAsStrings(false).forEach(key -> {
            List<Biome> biomes = new ArrayList<>();
            section.getStringList(key).forEach(biomeString -> {
               Biome biome = FishUtils.getBiome(biomeString);
               if (biome == null) {
                  EvenMoreFish.getInstance().getLogger().severe(biomeString + " is not a valid biome, found when loading in biome set " + key + ".");
               }

               biomes.add(biome);
            });
            biomeSetMap.put(key, biomes);
         });
         return biomeSetMap;
      }
   }

   private Map<String, Map<String, Double>> loadRegionBoosts() {
      Section regionBoostsSection = this.getConfig().getSection("region-boosts");
      if (regionBoostsSection == null) {
         return Map.of();
      } else {
         Map<String, Map<String, Double>> boosts = new HashMap<>();
         regionBoostsSection.getRoutesAsStrings(false).forEach(regionKey -> {
            Section regionSection = regionBoostsSection.getSection(regionKey);
            if (regionSection != null) {
               Map<String, Double> rarityBoosts = new HashMap<>();
               regionSection.getRoutesAsStrings(false).forEach(rarityKey -> rarityBoosts.put(rarityKey, regionSection.getDouble(rarityKey, 1.0)));
               boosts.put(regionKey, rarityBoosts);
            }
         });
         return boosts;
      }
   }

   public Map<String, List<Biome>> getBiomeSets() {
      return this.biomeSets;
   }

   public double getRegionBoost(String region, String rarity) {
      double defaultBoostRate = 1.0;
      if (region == null || rarity == null) {
         return defaultBoostRate;
      } else if (this.regionBoosts != null && !this.regionBoosts.isEmpty()) {
         Map<String, Double> regionMap = this.regionBoosts.get(region);
         return regionMap == null ? defaultBoostRate : regionMap.getOrDefault(rarity, defaultBoostRate);
      } else {
         return defaultBoostRate;
      }
   }

   public boolean isRegionBoostsEnabled() {
      return this.regionBoosts != null && !this.regionBoosts.isEmpty();
   }

   public boolean isEconomyEnabled(@NonNull EconomyType type) {
      return this.getConfig().getBoolean("economy." + type.getIdentifier().toLowerCase() + ".enabled");
   }

   public double getEconomyMultiplier(@NonNull EconomyType type) {
      return this.getConfig().getDouble("economy." + type.getIdentifier().toLowerCase() + ".multiplier");
   }

   @Nullable
   public String getEconomyDisplay(@NonNull EconomyType type) {
      return this.getConfig().getString("economy." + type.getIdentifier().toLowerCase() + ".display");
   }

   @Nullable
   public String getExcellentEconomyCurrency() {
      return this.getConfig().getString("economy.excellenteconomy.currency");
   }

   public boolean shouldCompetitionResume() {
      return this.getConfig().getBoolean("fishing.resume-competition-on-restart", false);
   }

   public boolean shouldCompetitionHold() {
      return this.getConfig().getBoolean("fishing.hold-competition-if-active", true);
   }

   @Override
   public UpdaterSettings getUpdaterSettings() {
      return UpdaterSettings.builder(super.getUpdaterSettings())
         .addCustomLogic("1", document -> {
            String economyType = document.getString("economy-type");
            document.remove("enable-economy");
            document.remove("economy-type");
            if (economyType != null && !economyType.equalsIgnoreCase("NONE")) {
               String path = "economy." + economyType.toLowerCase();
               document.set(path + ".enabled", true);
            }
         })
         .addRelocation("1", "block-crafting", "item-protection.block-crafting", '.')
         .addRelocation("1", "fish-only-in-competition", "fishing.catch-only-in-competition", '.')
         .addCustomLogic("1", document -> {
            if (document.contains("vanilla-fishing")) {
               boolean vanillaFishing = document.getBoolean("vanilla-fishing");
               document.set("fishing.catch-enabled", !vanillaFishing);
               document.remove("vanilla-fishing");
            }
         })
         .addRelocation("2", "require-nbt-rod", "fishing.require-custom-rod", '.')
         .addRelocations(
            "4",
            Map.of(
               "item-protection.block-crafting",
               "item-protection.prevent-crafting",
               "item-protection.block-consume",
               "item-protection.prevent-consume",
               "item-protection.block-furnace-burn",
               "item-protection.prevent-furnace-burn",
               "item-protection.block-cooking",
               "item-protection.prevent-cooking",
               "item-protection.block-placing",
               "item-protection.prevent-placing",
               "disable-db-verbose",
               "database.disable-verbose",
               "give-straight-to-inventory",
               "fishing.give-straight-to-inventory"
            ),
            '.'
         )
         .addRelocation("5", "disable-aurelium-skills", "disable-aura-skills", '.')
         .build();
   }

   public boolean hasCredentials() {
      return getInstance().getUsername() != null
         && getInstance().getPassword() != null
         && getInstance().getAddress() != null
         && getInstance().getDatabase() != null;
   }

   @Nullable
   public String getDisplayNameAddonBehavior() {
      return this.getConfig().getString("items.addons.display-behavior");
   }

   @Nullable
   public String getLoreAddonBehavior() {
      return this.getConfig().getString("items.addons.lore-behavior");
   }

   public double getBaitBoostRate() {
      return this.getConfig().getDouble("bait.boost", 1.5);
   }

   public boolean getBaitCompetitionDisable() {
      return this.getConfig().getBoolean("bait.competition-disable", true);
   }

   public boolean getBaitAddToLore() {
      return this.getConfig().getBoolean("bait.add-to-lore", true);
   }

   public double getBaitCatchPercentage() {
      return this.getConfig().getDouble("bait.catch-percentage", 2.5);
   }

   public int getBaitsPerRod() {
      return this.getConfig().getInt("bait.baits-per-rod", 7);
   }

   public boolean getBaitShowUnusedSlots() {
      return this.getConfig().getBoolean("bait.show-unused-slots", true);
   }

   public boolean isAFKProtectionEnabled() {
      boolean enabled = this.getConfig().getBoolean("exploits.afk-fishing.enabled", false);
      if (enabled && Bukkit.getPluginManager().isPluginEnabled("mcMMO") && ExperienceConfig.getInstance().isFishingExploitingPrevented()) {
         Logging.warn("mcMMO Overfishing is enabled. EMF's Anti-AFK features will not function.");
         return false;
      } else {
         return enabled;
      }
   }

   public int getAFKMaxFishCaught() {
      return this.getConfig().getInt("exploits.afk-fishing.max-caught", 10) + 1;
   }

   public int getAFKCheckRange() {
      return this.getConfig().getInt("exploits.afk-fishing.range", 3);
   }
}
