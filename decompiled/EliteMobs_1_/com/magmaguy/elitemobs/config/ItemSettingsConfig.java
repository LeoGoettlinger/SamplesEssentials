package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class ItemSettingsConfig extends ConfigurationFile {
   private static String shopItemSource;
   private static boolean doEliteMobsLoot;
   private static boolean doMmorpgColors;
   private static boolean preventCustomItemPlacement;
   private static List<String> loreStructure;
   private static String mobItemSource;
   private static String loreWorth;
   private static String loreResale;
   private static double flatDropRate;
   private static double regionalBossNonUniqueDropRate;
   private static boolean regionalBossesDropVanillaLoot;
   private static double levelIncreaseDropRate;
   private static double proceduralItemWeight;
   private static double weighedItemWeight;
   private static double fixedItemWeight;
   private static double limitedItemWeight;
   private static double scalableItemWeight;
   private static double defaultLootMultiplier;
   private static double defaultExperienceMultiplier;
   private static String potionEffectOnHitTargetLore;
   private static int maxLevelForDefaultLootMultiplier;
   private static int maximumItemLevel;
   private static boolean useEliteEnchantments;
   private static String eliteEnchantLoreString;
   private static boolean useHoesAsWeapons;
   private static boolean enableRareItemParticleEffects;
   private static String potionEffectOnHitSelfLore;
   private static String potionEffectContinuousLore;
   private static String scrapSucceededMessage;
   private static String eliteEnchantmentColor;
   private static String vanillaEnchantmentColor;
   private static String customEnchantmentColor;
   private static String potionEffectColor;
   private static String noSoulbindLore;
   private static boolean preventEliteItemEnchantment;
   private static boolean preventEliteItemDisenchantment;
   private static String preventEliteItemDisenchantmentMessage;
   private static boolean preventEliteItemDiamondToNetheriteUpgrade;
   private static boolean eliteDurability;
   private static double eliteDurabilityMultiplier;
   private static String scrapFailedMessage;
   private static String scrapNotConfiguredMessage;
   private static boolean putLootDirectlyIntoPlayerInventory;
   private static int lootLevelDifferenceLockout;
   private static boolean preventEliteItemsFromBreaking;
   private static String noItemDurabilityMessage;
   private static String brokenItemSubtitleWarning;
   private static int minimumProcedurallyGeneratedDiamondLootLevelPlusSeven;
   private static String simlootMessageSuccess;
   private static String simlootMessageFailure;
   private static String directDropCustomLootMessage;
   private static String directDropMinecraftLootMessage;
   private static String directDropCoinMessage;
   private static boolean hideAttributes;
   private static boolean hideEnchants;
   private static String weaponEntry;
   private static String armorEntry;
   private static String levelRangeTooDifferent;
   private static boolean useEliteItemScrolls;
   private static double eliteItemScrollChance;
   private static String eliteItemScrollName;
   private static List<String> eliteItemScrollLore;
   private static String eliteItemModel;
   private static ItemSettingsConfig instance;

   public ItemSettingsConfig() {
      super("ItemSettings.yml");
   }

   public static void toggleEliteMobsLoot(boolean enabled) {
      doEliteMobsLoot = enabled;
      instance.fileConfiguration.set("doEliteMobsLoot", enabled);
      ConfigurationEngine.fileSaverCustomValues(instance.fileConfiguration, instance.file);
   }

   @Override
   public void initializeValues() {
      instance = this;
      doEliteMobsLoot = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if any EliteMobs loot will drop.",
            "Includes Elite Coins, Custom Items, Procedurally Generate Items - everything!",
            "Not recommended, makes mmorpg progression impossible."
         ),
         this.fileConfiguration,
         "doEliteMobsLoot",
         true
      );
      doMmorpgColors = ConfigurationEngine.setBoolean(
         List.of("Sets if procedurally generated loot will have different colors based on the quality of the item."),
         this.fileConfiguration,
         "doMMORPGColorsForItems",
         true
      );
      preventCustomItemPlacement = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if the placement of custom items, like banners or blocks, is prevented.",
            "This is recommended - custom items break when they are placed and can not be recovered!"
         ),
         this.fileConfiguration,
         "preventCustomItemPlacement",
         true
      );
      loreStructure = ConfigurationEngine.setList(
         List.of(
            "Sets the format for the lore of custom all EliteMobs items!",
            "The following are valid placeholders:",
            "$itemLevel - shows the item level",
            "$prestigeLevel - shows the prestige level",
            "$itemMaterial - shows the base material of the item, like Diamond Sword. Useful when resource pack skins hide the material. Uses the server-side English name",
            "$itemMaxDurability - shows the maximum durability of the item. Lines with this placeholder are skipped for items without durability and unbreakable items",
            "$weaponOrArmorStats - shows the elite DPS or elite armor stats, depending on the item",
            "$soulbindInfo - shows who, if anyone, the item is soulbound to",
            "$itemSource - shows where the item came from, like a mob or a shop",
            "$ifLore - makes a line only appear if the item has custom lore. Only applies for custom items",
            "$customLore - shows the entirety of the custom lore. Only applies for custom items",
            "$ifEnchantments - makes a line only appear if the item has any enchantments",
            "$enchantments - shows the enchantments on the item",
            "$eliteEnchantments - shows the elite enchantments on the item",
            "$ifCustomEnchantments - shows the custom enchantments on the item",
            "$customEnchantments - shows the custom enchantments on the item",
            "$ifPotionEffects - only shows the line if the item has potion effects",
            "$potionEffects - shows the potion effects on the item",
            "$loreResaleValue - shows the value of the item. Might show the purchase or sell price depending on where it is viewed",
            "Important: Several of the placeholders can be further customized by the configuration settings further below"
         ),
         this.file,
         this.fileConfiguration,
         "itemLoreStructureV2",
         new ArrayList<>(
            List.of(
               ChatColorConverter.convert("&7&m&l---------&7<&lEquip Info&7>&m&l---------"),
               ChatColorConverter.convert("&7Item level: &f$itemLevel &7Prestige &6$prestigeLevel"),
               ChatColorConverter.convert("$weaponOrArmorStats"),
               ChatColorConverter.convert("$soulbindInfo"),
               ChatColorConverter.convert("$itemSource"),
               ChatColorConverter.convert("$ifLore&7&m&l-----------&7< &f&lLore&7 >&m&l-----------"),
               ChatColorConverter.convert("$customLore"),
               ChatColorConverter.convert("$ifEnchantments&7&m&l--------&7<&9&lEnchantments&7>&m&l--------"),
               ChatColorConverter.convert("$enchantments"),
               ChatColorConverter.convert("$eliteEnchantments"),
               ChatColorConverter.convert("$ifCustomEnchantments&7&m&l------&7< &3&lCustom Enchants&7 >&m&l------"),
               ChatColorConverter.convert("$customEnchantments"),
               ChatColorConverter.convert("$ifPotionEffects&7&m&l----------&7< &5&lEffects&7 >&m&l----------"),
               ChatColorConverter.convert("$potionEffect"),
               ChatColorConverter.convert("&7&l&m-----------------------------"),
               ChatColorConverter.convert("$loreResaleValue")
            )
         ),
         true
      );
      shopItemSource = ConfigurationEngine.setString(
         List.of("Sets the shop source lore for store purchased"), this.file, this.fileConfiguration, "shopSourceItemLores", "&7Purchased from a store", true
      );
      mobItemSource = ConfigurationEngine.setString(
         List.of("Sets the shop source lore for items looted from bosses"), this.file, this.fileConfiguration, "mobSourceItemLores", "&7Looted from $mob", true
      );
      loreWorth = ConfigurationEngine.setString(
         List.of("Sets the item worth lore"), this.file, this.fileConfiguration, "loreWorths", "&7Worth $worth $currencyName", true
      );
      loreResale = ConfigurationEngine.setString(
         List.of("Sets the item resale value lore"), this.file, this.fileConfiguration, "loreResaleValues", "&7Sells for $resale $currencyName", true
      );
      flatDropRate = ConfigurationEngine.setDouble(
         List.of("Sets the base chance for any elite item to drop from elite mobs"), this.fileConfiguration, "flatDropRateV3", 0.2
      );
      regionalBossNonUniqueDropRate = ConfigurationEngine.setDouble(
         List.of("Sets the base chance for any elite item to drop from regional bosses"), this.fileConfiguration, "regionalBossNonUniqueDropRate", 0.05
      );
      regionalBossesDropVanillaLoot = ConfigurationEngine.setBoolean(
         List.of("Sets if the regional bosses can drop vanilla loot"), this.fileConfiguration, "regionalBossesDropVanillaLoot", false
      );
      levelIncreaseDropRate = ConfigurationEngine.setDouble(
         List.of(
            "Sets how much the chance of an elite item dropping increases based on the level of the boss.",
            "The level of the boss is multiplied by this value and added to the base chance.",
            "It is no longer recommended to have this above 0.0!"
         ),
         this.fileConfiguration,
         "levelIncreaseDropRateV2",
         0.0
      );
      proceduralItemWeight = ConfigurationEngine.setDouble(
         List.of(
            "Sets the weighed chance of a procedurally generated item dropping.",
            "This system uses weighted probabilities! Look that up on google if you don't know what that is."
         ),
         this.fileConfiguration,
         "proceduralItemDropWeight",
         90.0
      );
      weighedItemWeight = ConfigurationEngine.setDouble(
         List.of("Sets the relative chance of a weighed item dropping.", "Weighed items are custom items that don't have a dynamic weight, like charms."),
         this.fileConfiguration,
         "weighedItemDropWeight",
         1.0
      );
      fixedItemWeight = ConfigurationEngine.setDouble(
         List.of("Sets the relative chance of a fixed item dropping. These are custom items that do not scale."),
         this.fileConfiguration,
         "fixedItemDropWeight",
         10.0
      );
      limitedItemWeight = ConfigurationEngine.setDouble(
         List.of("Sets the relative chance of a limited item dropping. These are custom items that scale up to a specific level"),
         this.fileConfiguration,
         "limitedItemDropWeight",
         3.0
      );
      scalableItemWeight = ConfigurationEngine.setDouble(
         List.of(
            "Sets the relative chance of a scalable item dropping. These are custom items that can scale to any level, and are the most common in the plugin."
         ),
         this.fileConfiguration,
         "scalableItemDropWeight",
         6.0
      );
      defaultLootMultiplier = ConfigurationEngine.setDouble(
         List.of("Sets the multiplier for the vanilla loot of the mob, based on the level of the mob."), this.fileConfiguration, "defaultLootMultiplier", 0.0
      );
      maxLevelForDefaultLootMultiplier = ConfigurationEngine.setInt(
         List.of("Sets the maximum level for the default loot multiplier."), this.fileConfiguration, "levelCapForDefaultLootMultiplier", 100
      );
      defaultExperienceMultiplier = ConfigurationEngine.setDouble(
         List.of("Sets the vanilla Minecraft experience dropped multiplier for the boss, based on the level of the boss."),
         this.fileConfiguration,
         "defaultExperienceMultiplier",
         1.0
      );
      maximumItemLevel = ConfigurationEngine.setInt(
         List.of("Sets the level of the maximum loot that will be dropped by EliteMobs. Strongly recommended to leave it at 100."),
         this.fileConfiguration,
         "maximumItemLevel",
         100
      );
      useEliteEnchantments = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if elite enchantments will be used. ",
            "Elite enchantments replace vanilla enchantments when elite items get enchantment levels that go beyond vanilla limits.",
            "Example: if an elite sword is supposed to have sharpness 10, since the Minecraft limit is level 5, it will have sharpness 5 and elite sharpness 5.",
            "Elite sharpness only affects mobs spawned by EliteMobs. This is done so PVP and vanilla combat don't become unbalanced."
         ),
         this.fileConfiguration,
         "useEliteEnchantments",
         true
      );
      eliteEnchantLoreString = ConfigurationEngine.setString(
         List.of("Sets the display name that will be used for the elite enchantments on item lore."),
         this.file,
         this.fileConfiguration,
         "eliteEnchantmentLoreStrings",
         "Elite",
         true
      );
      useHoesAsWeapons = ConfigurationEngine.setBoolean(
         List.of("Sets if EliteMobs will see hoes as valid weapons for the damage calculations."), this.fileConfiguration, "useHoesAsWeapons", false
      );
      enableRareItemParticleEffects = ConfigurationEngine.setBoolean(
         List.of("Sets if EliteMobs will spawn special particles over dropped items of high quality."),
         this.fileConfiguration,
         "enableRareItemParticleEffects",
         true
      );
      potionEffectOnHitTargetLore = ConfigurationEngine.setString(
         List.of("Sets the symbols that will be used in item lore to show that a potion effect applies on hit to the entity that gets hit."),
         this.file,
         this.fileConfiguration,
         "potionEffectOnHitTargetLore",
         "&4⚔☠",
         false
      );
      potionEffectOnHitSelfLore = ConfigurationEngine.setString(
         List.of("Sets the symbols that will be used in item lore to show that a potion effect applies on hit to the player doing the hitting."),
         this.file,
         this.fileConfiguration,
         "potionEffectOnHitSelfLore",
         "&9⚔\ud83d\udee1",
         false
      );
      potionEffectContinuousLore = ConfigurationEngine.setString(
         List.of("Sets the symbols that will be used in item lore to show that a potion effect will keep reapplying for as long as the player wields it."),
         this.file,
         this.fileConfiguration,
         "potionEffectContinuousLore",
         "&6⟲",
         false
      );
      eliteEnchantmentColor = ConfigurationEngine.setString(
         List.of("Sets the characters prefixed to elite enchantments in item lore."),
         this.file,
         this.fileConfiguration,
         "eliteEnchantmentLoreColor",
         "&9◇",
         false
      );
      vanillaEnchantmentColor = ConfigurationEngine.setString(
         List.of("Sets the characters prefixed to vanilla enchantments in item lore."),
         this.file,
         this.fileConfiguration,
         "vanillaEnchantmentLoreColor",
         "&7◇",
         false
      );
      customEnchantmentColor = ConfigurationEngine.setString(
         List.of("Sets the characters prefixed to custom enchantments in item lore."),
         this.file,
         this.fileConfiguration,
         "customEnchantmentColor",
         "&3◇",
         false
      );
      potionEffectColor = ConfigurationEngine.setString(
         List.of("Sets the characters prefixed to potion effects in item lore."), this.file, this.fileConfiguration, "potionEffectLoreColor", "&5◇", false
      );
      noSoulbindLore = ConfigurationEngine.setString(
         List.of("Sets the text that will appear on the item if the item is not soulbound."),
         this.file,
         this.fileConfiguration,
         "noSoulbindLore",
         "&7Not Soulbound!",
         true
      );
      preventEliteItemEnchantment = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if elite item can be enchanted by vanilla means. This is not recommended as EliteMobs has its own custom enchantments system  with its own balance!"
         ),
         this.fileConfiguration,
         "preventEliteItemEnchantment",
         true
      );
      preventEliteItemDisenchantment = ConfigurationEngine.setBoolean(
         List.of("Sets if elite items can be disenchanted by vanilla means."), this.fileConfiguration, "preventEliteItemDisenchantment", true
      );
      preventEliteItemDisenchantmentMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears for players when they attempt to disenchant an item and that is not allowed."),
         this.file,
         this.fileConfiguration,
         "preventEliteItemDisenchantmentMessage",
         "&c[EliteMobs] Can't disenchant Elite Items!",
         true
      );
      preventEliteItemDiamondToNetheriteUpgrade = ConfigurationEngine.setBoolean(
         List.of("Sets whether elite items can be upgraded from diamond to netherite by vanilla means. Not recommended!"),
         this.fileConfiguration,
         "preventEliteItemDiamondToNetheriteUpgrade",
         true
      );
      eliteDurability = ConfigurationEngine.setBoolean(
         List.of(
            "Sets whether elite items will only lose durability on death.",
            "This is an important system for EliteMobs, and it is strongly recommended as high level fights are nearly impossible without it!"
         ),
         this.fileConfiguration,
         "eliteItemsDurabilityLossOnlyOnDeath",
         true
      );
      eliteDurabilityMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Sets the durability loss multiplier for elite items if it is set to lose durability on death.",
            "Values between 0.0 and 1.0 lower the durability loss and values above 1.0 increase it.",
            "Example: 0.5 deals 50% of the durability loss, 2.0 deals 200% of the durability loss."
         ),
         this.fileConfiguration,
         "eliteItemsDurabilityLossMultiplier",
         1.0
      );
      scrapSucceededMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when item scrapping is successful."),
         this.file,
         this.fileConfiguration,
         "scrapSucceededMessageV2",
         "&8[EliteMobs] &2Scrapping succeeded $amount times!",
         true
      );
      scrapFailedMessage = ConfigurationEngine.setString(
         List.of("Sets message that appears when item scrapping fails."),
         this.file,
         this.fileConfiguration,
         "scrapFailedMessageV2",
         "&8[EliteMobs] &cScrapping failed $amount times!",
         true
      );
      scrapNotConfiguredMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when the scrapper has no scrap item configured to produce."),
         this.file,
         this.fileConfiguration,
         "scrapNotConfiguredMessage",
         "&cThe scrapper has no scrap item configured — contact an admin.",
         true
      );
      putLootDirectlyIntoPlayerInventory = ConfigurationEngine.setBoolean(
         List.of("Sets if elite loot should be placed directly into players' inventories."),
         this.fileConfiguration,
         "putLootDirectlyIntoPlayerInventory",
         false
      );
      lootLevelDifferenceLockout = ConfigurationEngine.setInt(
         List.of(
            "Sets maximum level difference players can have before they can no longer loot items that are too low level.",
            "This is calculated based on the average level of the loot the player is wearing.",
            "As an example, if it is set to 10 and a player has level 50 gear, they will not be able to farm level 39 bosses."
         ),
         this.fileConfiguration,
         "lootLevelDifferenceLockout",
         10
      );
      preventEliteItemsFromBreaking = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if EliteMobs will prevent Elite Items from breaking when using the durability loss on death system.",
            "Players will not be able to use items with no durability left anyway, this is simply to prevent the accidental loss of high level but low durability items."
         ),
         this.fileConfiguration,
         "preventEliteItemsFromBreaking",
         true
      );
      noItemDurabilityMessage = ConfigurationEngine.setString(
         List.of("Sets the characters prefixed to vanilla enchantments in item lore."),
         this.file,
         this.fileConfiguration,
         "noItemDurabilityMessage",
         "&8[EliteMobs] $item &4is broken! It won't work until repaired!",
         true
      );
      brokenItemSubtitleWarning = ConfigurationEngine.setString(
         List.of(
            "Subtitle shown to a player when their attack is cancelled because their held elite weapon is broken.",
            "Set to an empty string to disable the subtitle."
         ),
         this.file,
         this.fileConfiguration,
         "brokenItemSubtitleWarning",
         "&cYour weapon is broken!",
         true
      );
      minimumProcedurallyGeneratedDiamondLootLevelPlusSeven = ConfigurationEngine.setInt(
         List.of(
            "Sets the minimum level, +7, of bosses that can procedurally generated drop diamond gear in EliteMobs.",
            "There is no procedurally generated netherite gear in EliteMobs, only custom loot."
         ),
         this.fileConfiguration,
         "minimumProcedurallyGeneratedDiamondLootLevelPlusSeven",
         10
      );
      simlootMessageSuccess = ConfigurationEngine.setString(
         List.of("Sets the message show in chat when successfully rolling for loot through the /em simloot <level> <times> command."),
         this.file,
         this.fileConfiguration,
         "simlootMessageSuccess",
         "&8[EliteMobs] &2Rolled for loot and got $itemName &2!",
         true
      );
      simlootMessageFailure = ConfigurationEngine.setString(
         List.of("Sets the message show in chat when failing to roll for loot through the /em simloot <level> <times> command."),
         this.file,
         this.fileConfiguration,
         "simlootMessageFailure",
         "&8[EliteMobs] &cRolled for loot and got nothing!",
         true
      );
      directDropCustomLootMessage = ConfigurationEngine.setString(
         List.of("Sets the message that players get when elite loot is deposited directly into their inventories."),
         this.file,
         this.fileConfiguration,
         "directDropCustomLootMessage",
         "&8[EliteMobs] &2Obtained $itemName &2!",
         true
      );
      directDropMinecraftLootMessage = ConfigurationEngine.setString(
         List.of("Sets the message that players get when vanilla loot is deposited directly into their inventories."),
         this.file,
         this.fileConfiguration,
         "directDropMinecraftLootMessage",
         "&8[EliteMobs] &aObtained $itemName &a!",
         true
      );
      directDropCoinMessage = ConfigurationEngine.setString(
         List.of("Sets the message that players get when elite coins are deposited directly into their inventories."),
         this.file,
         this.fileConfiguration,
         "directDropCoinMessage",
         "&8[EliteMobs] &aObtained &2$amount $currencyName &a!",
         true
      );
      hideAttributes = ConfigurationEngine.setBoolean(
         List.of("Sets if EliteMobs will hide vanilla Minecraft attributes."), this.fileConfiguration, "hideItemAttributes", true
      );
      hideEnchants = ConfigurationEngine.setBoolean(
         List.of("Sets if EliteMobs will hide enchantment tooltips on EliteMobs items."), this.fileConfiguration, "hideEnchants", true
      );
      weaponEntry = ConfigurationEngine.setString(
         List.of(
            "Sets the weapon-specific lore entry on an elite item.",
            "$itemLevel - shows the weapon's item level (recommended; reflects actual combat power)",
            "$EDPS - shows the elite DPS (legacy, kept for compatibility; misleading under scaled/normalized combat since the formula replaces vanilla damage)"
         ),
         this.file,
         this.fileConfiguration,
         "weaponEntry",
         "&7Weapon Level: &2$itemLevel",
         true
      );
      armorEntry = ConfigurationEngine.setString(
         List.of(
            "Sets the armor-specific lore entry on an elite item.",
            "$itemLevel - shows the armor's item level (recommended; reflects actual combat power)",
            "$EDEF - shows the elite armor value (legacy, kept for compatibility; misleading under scaled/normalized combat)"
         ),
         this.file,
         this.fileConfiguration,
         "armorEntry",
         "&7Armor Level: &2$itemLevel",
         true
      );
      levelRangeTooDifferent = ConfigurationEngine.setString(
         List.of("Sets the message sent when a played kills a boss but the gear level is too different from the boss level to get coins"),
         this.file,
         this.fileConfiguration,
         "levelRangeTooDifferent",
         "&8EM] &4Your gear is level $playerLevel and the boss is level $bossLevel, level difference is too high to get coins!",
         true
      );
      useEliteItemScrolls = ConfigurationEngine.setBoolean(
         List.of(
            "Sets whether the plugin will use Elite Item Scrolls",
            "Elite Item Scroll are recommended for servers that want to use other item plugins like ItemAdder",
            "Elite item scrolls can be used at anvils to give non-elite items elite levels",
            "Elite item scrolls drop from elites of the same level to guarantee that the loot is balanced",
            "Note for item balance that elite damage does not affect damage outside of EliteMobs, so in most scenarios this should not result in broken (overpowered) items"
         ),
         this.fileConfiguration,
         "useEliteItemScrolls",
         false
      );
      eliteItemScrollChance = ConfigurationEngine.setDouble(
         List.of("Sets the chance of an Elite Item Scroll dropping from a boss, where 0 is 0% and 1 is 100%, and 0.5 is 50%."),
         this.fileConfiguration,
         "eliteItemScrollChance",
         0.1
      );
      eliteItemScrollName = ConfigurationEngine.setString(
         List.of("Sets the name of the elite item scroll item"), this.file, this.fileConfiguration, "eliteItemScrollName", "&6Elite Item Scroll", true
      );
      eliteItemScrollLore = ConfigurationEngine.setList(
         List.of("Sets the name of the elite item scroll item"),
         this.file,
         this.fileConfiguration,
         "eliteItemScrollLore",
         List.of("&fUse at the Scroll Applier NPC to enchant", "&fa non-elite item into a level $level", "&felite item!"),
         true
      );
      eliteItemModel = ConfigurationEngine.setString(List.of(), this.file, this.fileConfiguration, "eliteItemModel", "elitemobs:elitescroll/elitescroll", false);
   }

   @Generated
   public static String getShopItemSource() {
      return shopItemSource;
   }

   @Generated
   public static boolean isDoEliteMobsLoot() {
      return doEliteMobsLoot;
   }

   @Generated
   public static boolean isDoMmorpgColors() {
      return doMmorpgColors;
   }

   @Generated
   public static boolean isPreventCustomItemPlacement() {
      return preventCustomItemPlacement;
   }

   @Generated
   public static List<String> getLoreStructure() {
      return loreStructure;
   }

   @Generated
   public static String getMobItemSource() {
      return mobItemSource;
   }

   @Generated
   public static String getLoreWorth() {
      return loreWorth;
   }

   @Generated
   public static String getLoreResale() {
      return loreResale;
   }

   @Generated
   public static double getFlatDropRate() {
      return flatDropRate;
   }

   @Generated
   public static double getRegionalBossNonUniqueDropRate() {
      return regionalBossNonUniqueDropRate;
   }

   @Generated
   public static boolean isRegionalBossesDropVanillaLoot() {
      return regionalBossesDropVanillaLoot;
   }

   @Generated
   public static double getLevelIncreaseDropRate() {
      return levelIncreaseDropRate;
   }

   @Generated
   public static double getProceduralItemWeight() {
      return proceduralItemWeight;
   }

   @Generated
   public static double getWeighedItemWeight() {
      return weighedItemWeight;
   }

   @Generated
   public static double getFixedItemWeight() {
      return fixedItemWeight;
   }

   @Generated
   public static double getLimitedItemWeight() {
      return limitedItemWeight;
   }

   @Generated
   public static double getScalableItemWeight() {
      return scalableItemWeight;
   }

   @Generated
   public static double getDefaultLootMultiplier() {
      return defaultLootMultiplier;
   }

   @Generated
   public static double getDefaultExperienceMultiplier() {
      return defaultExperienceMultiplier;
   }

   @Generated
   public static String getPotionEffectOnHitTargetLore() {
      return potionEffectOnHitTargetLore;
   }

   @Generated
   public static int getMaxLevelForDefaultLootMultiplier() {
      return maxLevelForDefaultLootMultiplier;
   }

   @Generated
   public static int getMaximumItemLevel() {
      return maximumItemLevel;
   }

   @Generated
   public static boolean isUseEliteEnchantments() {
      return useEliteEnchantments;
   }

   @Generated
   public static String getEliteEnchantLoreString() {
      return eliteEnchantLoreString;
   }

   @Generated
   public static boolean isUseHoesAsWeapons() {
      return useHoesAsWeapons;
   }

   @Generated
   public static boolean isEnableRareItemParticleEffects() {
      return enableRareItemParticleEffects;
   }

   @Generated
   public static String getPotionEffectOnHitSelfLore() {
      return potionEffectOnHitSelfLore;
   }

   @Generated
   public static String getPotionEffectContinuousLore() {
      return potionEffectContinuousLore;
   }

   @Generated
   public static String getScrapSucceededMessage() {
      return scrapSucceededMessage;
   }

   @Generated
   public static String getEliteEnchantmentColor() {
      return eliteEnchantmentColor;
   }

   @Generated
   public static String getVanillaEnchantmentColor() {
      return vanillaEnchantmentColor;
   }

   @Generated
   public static String getCustomEnchantmentColor() {
      return customEnchantmentColor;
   }

   @Generated
   public static String getPotionEffectColor() {
      return potionEffectColor;
   }

   @Generated
   public static String getNoSoulbindLore() {
      return noSoulbindLore;
   }

   @Generated
   public static boolean isPreventEliteItemEnchantment() {
      return preventEliteItemEnchantment;
   }

   @Generated
   public static boolean isPreventEliteItemDisenchantment() {
      return preventEliteItemDisenchantment;
   }

   @Generated
   public static String getPreventEliteItemDisenchantmentMessage() {
      return preventEliteItemDisenchantmentMessage;
   }

   @Generated
   public static boolean isPreventEliteItemDiamondToNetheriteUpgrade() {
      return preventEliteItemDiamondToNetheriteUpgrade;
   }

   @Generated
   public static boolean isEliteDurability() {
      return eliteDurability;
   }

   @Generated
   public static double getEliteDurabilityMultiplier() {
      return eliteDurabilityMultiplier;
   }

   @Generated
   public static String getScrapFailedMessage() {
      return scrapFailedMessage;
   }

   @Generated
   public static String getScrapNotConfiguredMessage() {
      return scrapNotConfiguredMessage;
   }

   @Generated
   public static boolean isPutLootDirectlyIntoPlayerInventory() {
      return putLootDirectlyIntoPlayerInventory;
   }

   @Generated
   public static int getLootLevelDifferenceLockout() {
      return lootLevelDifferenceLockout;
   }

   @Generated
   public static boolean isPreventEliteItemsFromBreaking() {
      return preventEliteItemsFromBreaking;
   }

   @Generated
   public static String getNoItemDurabilityMessage() {
      return noItemDurabilityMessage;
   }

   @Generated
   public static String getBrokenItemSubtitleWarning() {
      return brokenItemSubtitleWarning;
   }

   @Generated
   public static int getMinimumProcedurallyGeneratedDiamondLootLevelPlusSeven() {
      return minimumProcedurallyGeneratedDiamondLootLevelPlusSeven;
   }

   @Generated
   public static String getSimlootMessageSuccess() {
      return simlootMessageSuccess;
   }

   @Generated
   public static String getSimlootMessageFailure() {
      return simlootMessageFailure;
   }

   @Generated
   public static String getDirectDropCustomLootMessage() {
      return directDropCustomLootMessage;
   }

   @Generated
   public static String getDirectDropMinecraftLootMessage() {
      return directDropMinecraftLootMessage;
   }

   @Generated
   public static String getDirectDropCoinMessage() {
      return directDropCoinMessage;
   }

   @Generated
   public static boolean isHideAttributes() {
      return hideAttributes;
   }

   @Generated
   public static boolean isHideEnchants() {
      return hideEnchants;
   }

   @Generated
   public static String getWeaponEntry() {
      return weaponEntry;
   }

   @Generated
   public static String getArmorEntry() {
      return armorEntry;
   }

   @Generated
   public static String getLevelRangeTooDifferent() {
      return levelRangeTooDifferent;
   }

   @Generated
   public static boolean isUseEliteItemScrolls() {
      return useEliteItemScrolls;
   }

   @Generated
   public static double getEliteItemScrollChance() {
      return eliteItemScrollChance;
   }

   @Generated
   public static String getEliteItemScrollName() {
      return eliteItemScrollName;
   }

   @Generated
   public static List<String> getEliteItemScrollLore() {
      return eliteItemScrollLore;
   }

   @Generated
   public static String getEliteItemModel() {
      return eliteItemModel;
   }
}
