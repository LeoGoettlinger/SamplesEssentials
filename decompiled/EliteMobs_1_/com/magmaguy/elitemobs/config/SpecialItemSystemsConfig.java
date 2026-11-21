package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import org.bukkit.configuration.file.FileConfiguration;

public class SpecialItemSystemsConfig extends ConfigurationFile {
   private static final HashMap<CustomItem, Double> specialValues = new HashMap<>();
   private static final List<SpecialItemSystemsConfig.PendingSpecialItem> pendingSpecialItems = new ArrayList<>();
   private static boolean specialValuesResolved = false;
   private static boolean dropSpecialLoot;
   private static double bossChanceToDrop;
   private static double nonEliteChanceToDrop;
   private static double luckyTicketMultiplier;
   private static double criticalFailureChance;
   private static double challengeChance;
   private static double criticalFailureChanceDuringChallengeChance;
   private static String insufficientFundsMessage;
   private static String newFundsMessage;
   private static boolean announceImportantEnchantments;
   private static String successAnnouncement;
   private static String criticalFailureAnnouncement;
   private static String challengeAnnouncement;

   public SpecialItemSystemsConfig() {
      super("SpecialItemSystems.yml");
   }

   public static HashMap<CustomItem, Double> getSpecialValues() {
      if (!specialValuesResolved) {
         resolveSpecialItems();
      }

      return specialValues;
   }

   private static void resolveSpecialItems() {
      specialValuesResolved = true;

      for (SpecialItemSystemsConfig.PendingSpecialItem pending : pendingSpecialItems) {
         CustomItem customItem = CustomItem.getCustomItem(pending.filename + ".yml");
         if (customItem == null) {
            Logger.warn("Failed to get custom item " + pending.filename + ".yml for the special loot list!");
         } else {
            specialValues.put(customItem, pending.weight);
         }
      }

      pendingSpecialItems.clear();
   }

   private static void addDefaultEnchantmentBook(FileConfiguration fileConfiguration, String configFilename, double chance) {
      String key = "enchantedBookWeightedDropChance." + configFilename;
      fileConfiguration.addDefault(key, chance);
      pendingSpecialItems.add(new SpecialItemSystemsConfig.PendingSpecialItem(configFilename, fileConfiguration.getDouble(key)));
   }

   @Override
   public void initializeValues() {
      specialValues.clear();
      pendingSpecialItems.clear();
      specialValuesResolved = false;
      dropSpecialLoot = ConfigurationEngine.setBoolean(List.of("Sets if special loot will drop."), this.fileConfiguration, "dropSpecialLoot", true);
      bossChanceToDrop = ConfigurationEngine.setDouble(
         List.of(
            "Sets the chance of special loot dropping from any custom boss that can drop elite loot with a health multiplier over 1.0.",
            "This means all bosses, minibosses and marginally hard bosses."
         ),
         this.fileConfiguration,
         "bossChanceToDrop",
         0.1
      );
      nonEliteChanceToDrop = ConfigurationEngine.setDouble(
         List.of("Sets the chance of special loot dropping custom boss that can drop elite loot.", "Normal elites are excluded to avoid encouraging mob farms."),
         this.fileConfiguration,
         "normalChanceToDrop",
         0.01
      );
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_arrow_damage", 200.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_arrow_fire", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_arrow_infinite", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_arrow_knockback", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_channeling", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_critical_strikes", 1.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_damage_all", 200.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_damage_arthropods", 100.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_damage_undead", 100.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_depth_strider", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_dig_speed", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_drilling", 1.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_durability", 100.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_earthquake", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_flamethrower", 0.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_frost_walker", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_hunter", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_ice_breaker", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_impaling", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_knockback", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_lightning", 0.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_loot_bonus_blocks", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_loot_bonus_mobs", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_loud_strikes", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_loyalty", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_luck", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_lure", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_mending", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_meteor_shower", 0.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_multishot", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_oxygen", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_piercing", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_plasma_boots", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_protection_environmental", 600.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_protection_explosions", 100.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_protection_fall", 100.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_protection_fire", 100.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_protection_projectile", 100.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_quick_charge", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_riptide", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_silk_touch", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_soul_speed", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_sweeping_edge", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_thorns", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "enchanted_book_water_worker", 10.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "elite_lucky_ticket", 100.0);
      addDefaultEnchantmentBook(this.fileConfiguration, "elite_scrap_huge", 100.0);
      this.fileConfiguration
         .setComments(
            "enchantedBookWeightedDropChance",
            List.of(
               "Sets the chance of a special item dropping over another special item.",
               "The higher the value, the higher the chance of that item getting picked over other items.",
               "Keep in mind that if values get too high, things with low values will become almost impossible to obtain."
            )
         );
      luckyTicketMultiplier = ConfigurationEngine.setDouble(
         List.of("Multiplier for the lucky ticket success chance in enchantments. 2.0 = 2x"), this.fileConfiguration, "luckyTicketMultiplier", 2.0
      );
      criticalFailureChance = ConfigurationEngine.setDouble(
         List.of("Chance of an item being lost if an item enchantment fails. This is a percentage of the base failure chance."),
         this.fileConfiguration,
         "criticalFailureChanceV2",
         0.01
      );
      challengeChance = ConfigurationEngine.setDouble(
         List.of(
            "Chance of a player being teleported to a challenge arena if an item enchantment fails. This is a percentage of the base failure chance.",
            "If the player defeats the arena, their item is upgraded correctly. If the lose, either it is not enchanted or, if they're unlucky, they may lose the item."
         ),
         this.fileConfiguration,
         "challengeChance",
         0.3
      );
      criticalFailureChanceDuringChallengeChance = ConfigurationEngine.setDouble(
         List.of("Chance of an item being lost if a player loses the enchantment challenge. This is a percentage of the base failure chance."),
         this.fileConfiguration,
         "criticalFailureChanceDuringChallengeChance",
         0.1
      );
      insufficientFundsMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when players do not have enough elite coins to enchant an item"),
         this.file,
         this.fileConfiguration,
         "insufficientFundsMessage",
         "&8[EliteMobs] &c$price $currencyName is required to enchant $itemName, but you only have $currentAmount!",
         true
      );
      newFundsMessage = ConfigurationEngine.setString(
         List.of("Sets the message sent when players after deducting the funds"),
         this.file,
         this.fileConfiguration,
         "newFundsMessage",
         "&8[EliteMobs] &fYou just spent $price $currencyName! You now have $currentAmount $currencyName.",
         true
      );
      announceImportantEnchantments = ConfigurationEngine.setBoolean(
         List.of("Sets if enchantments of items with more than 10 enchantments in them will be announced"), this.fileConfiguration, "successAnnouncement", true
      );
      successAnnouncement = ConfigurationEngine.setString(
         List.of("Sets the message announced to all players when successfully enchanting an item with more than 10 enchantments"),
         this.file,
         this.fileConfiguration,
         "successAnnouncement",
         "&8[EliteMobs] $player &2has successfully enchanted $itemName&2!",
         true
      );
      challengeAnnouncement = ConfigurationEngine.setString(
         List.of("Sets the message announced to all players when they get a boss fight while trying to enchant an item with more than 10 enchantments"),
         this.file,
         this.fileConfiguration,
         "challengeAnnouncement",
         "&8[EliteMobs] $player &6is challenging a boss to enchant $itemName&6!",
         true
      );
      criticalFailureAnnouncement = ConfigurationEngine.setString(
         List.of("Sets the message announced to all players when losing an item while trying to enchant it if the item has more than 10 enchantments"),
         this.file,
         this.fileConfiguration,
         "criticalFailureAnnouncement",
         "&8[EliteMobs] $player &clost $itemName &cwhile trying to enchant it!",
         true
      );
   }

   @Generated
   public static boolean isDropSpecialLoot() {
      return dropSpecialLoot;
   }

   @Generated
   public static double getBossChanceToDrop() {
      return bossChanceToDrop;
   }

   @Generated
   public static double getNonEliteChanceToDrop() {
      return nonEliteChanceToDrop;
   }

   @Generated
   public static double getLuckyTicketMultiplier() {
      return luckyTicketMultiplier;
   }

   @Generated
   public static double getCriticalFailureChance() {
      return criticalFailureChance;
   }

   @Generated
   public static double getChallengeChance() {
      return challengeChance;
   }

   @Generated
   public static double getCriticalFailureChanceDuringChallengeChance() {
      return criticalFailureChanceDuringChallengeChance;
   }

   @Generated
   public static String getInsufficientFundsMessage() {
      return insufficientFundsMessage;
   }

   @Generated
   public static String getNewFundsMessage() {
      return newFundsMessage;
   }

   @Generated
   public static boolean isAnnounceImportantEnchantments() {
      return announceImportantEnchantments;
   }

   @Generated
   public static String getSuccessAnnouncement() {
      return successAnnouncement;
   }

   @Generated
   public static String getCriticalFailureAnnouncement() {
      return criticalFailureAnnouncement;
   }

   @Generated
   public static String getChallengeAnnouncement() {
      return challengeAnnouncement;
   }

   private record PendingSpecialItem(String filename, double weight) {
   }
}
