package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class EconomySettingsConfig extends ConfigurationFile {
   private static boolean enableEconomy;
   private static double resaleValue;
   private static String currencyName;
   private static boolean useVault;
   private static boolean enableCurrencyShower;
   private static double currencyShowerMultiplier;
   private static String chatCurrencyShowerMessage;
   private static String actionBarCurrencyShowerMessage;
   private static String lootShowerMaterial1;
   private static String lootShowerMaterial5;
   private static String lootShowerMaterial10;
   private static String lootShowerMaterial20;
   private static String lootShowerMaterial50;
   private static String lootShowerMaterial100;
   private static String lootShowerMaterial500;
   private static String lootShowerMaterial1000;
   private static String adventurersGuildNotificationMessage;
   private static double defaultMaterialWorth;
   private static FileConfiguration thisConfiguration;
   private static double playerToPlayerTaxes;
   private static String economyPayMessage;
   private static String economyCurrencyLeftMessage;
   private static String economyPaymentReceivedMessage;
   private static String economyPaymentInsufficientCurrency;
   private static String economyWalletCommand;
   private static String shopBuyMessage;
   private static String shopSellMessage;
   private static String shopInsufficientFundsMessage;
   private static String shopSaleInstructions;
   private static String shopSaleOthersItems;
   private static String shopCurrentBalance;
   private static String shopItemPrice;
   private static String shopBatchSellMessage;
   private static String shopTransactionFailedMessage;

   public EconomySettingsConfig() {
      super("EconomySettings.yml");
   }

   private static void addMaterial(FileConfiguration fileConfiguration, Material material, double value) {
      ConfigurationEngine.setDouble(
         List.of("Currency value of this material when sold to shops."), fileConfiguration, "materialWorth." + material.name(), value
      );
   }

   public static double getMaterialWorth(Material material) {
      return thisConfiguration.getDouble("materialWorth." + material.name(), defaultMaterialWorth);
   }

   @Override
   public void initializeValues() {
      shopTransactionFailedMessage = ConfigurationEngine.setString(
         List.of("Shown when a shop transaction could not be confirmed"),
         this.file,
         this.fileConfiguration,
         "shopTransactionFailedMessage",
         "&cThe transaction could not be confirmed. Contact an administrator before trying again.",
         true
      );
      thisConfiguration = this.fileConfiguration;
      double netheriteLevel = 18.0;
      double tridentLevel = 17.0;
      double diamondLevel = 17.0;
      double ironLevel = 16.0;
      double stoneChainLevel = 15.0;
      double goldWoodLeatherLevel = 13.0;
      enableEconomy = ConfigurationEngine.setBoolean(
         List.of(
            "Whether the EliteMobs economy is enabled (elite coins, shops, guild ranks).", "If disabled, players will not be able to progress in the plugin!"
         ),
         this.fileConfiguration,
         "enableEconomy",
         true
      );
      resaleValue = ConfigurationEngine.setDouble(
         List.of("Percentage of the original price players get when reselling items. 5 = 5%."), this.fileConfiguration, "itemResaleValue", 5.0
      );
      currencyName = ConfigurationEngine.setString(
         List.of("The display name of the currency used throughout the plugin."), this.file, this.fileConfiguration, "currencyName", "Elite Coins", true
      );
      useVault = ConfigurationEngine.setBoolean(
         List.of("Whether to use Vault for the economy instead of the built-in system.", "NOT RECOMMENDED! See: https://wiki.nightbreak.io/EliteMobs/vault"),
         this.fileConfiguration,
         "useVault",
         false
      );
      enableCurrencyShower = ConfigurationEngine.setBoolean(
         List.of("Whether elites drop currency (coins) on death."), this.fileConfiguration, "enableCurrencyShower", true
      );
      currencyShowerMultiplier = ConfigurationEngine.setDouble(
         List.of("Multiplier for currency dropped by elites. 1.0 = normal, 2.0 = double drops."), this.fileConfiguration, "currencyShowerTierMultiplier", 1.0
      );
      chatCurrencyShowerMessage = ConfigurationEngine.setString(
         List.of("Chat message shown when a player picks up elite currency.", "Placeholders: $amount, $currency_name"),
         this.file,
         this.fileConfiguration,
         "chatCurrencyShowerMessage",
         "&7[EM] You've picked up &a$amount $currency_name!",
         true
      );
      actionBarCurrencyShowerMessage = ConfigurationEngine.setString(
         List.of("Action bar message shown when a player picks up elite currency.", "Placeholders: $amount, $currency_name"),
         this.file,
         this.fileConfiguration,
         "actionbarCurrencyShowerMessage",
         "&7[EM] You've picked up &a$amount $currency_name!",
         true
      );
      lootShowerMaterial1 = ConfigurationEngine.setString(
         List.of("Material used for dropped coins worth 1 currency."),
         this.file,
         this.fileConfiguration,
         "lootShowerMaterial.1",
         Material.GOLD_NUGGET.name(),
         false
      );
      ConfigurationEngine.setInt(
         List.of("Custom model data ID for dropped coins worth 1 currency. Used by the resource pack."), this.fileConfiguration, "lootShowerData.1", 1
      );
      ConfigurationEngine.setString(this.fileConfiguration, "lootShowerDataV2.1", "elitemobs:coins/coin1");
      lootShowerMaterial5 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lootShowerMaterial.5", Material.GOLD_INGOT.name(), false);
      ConfigurationEngine.setString(this.fileConfiguration, "lootShowerDataV2.5", "elitemobs:coins/coin1");
      lootShowerMaterial10 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lootShowerMaterial.10", Material.GOLD_BLOCK.name(), false);
      ConfigurationEngine.setString(this.fileConfiguration, "lootShowerDataV2.10", "elitemobs:coins/coin2");
      lootShowerMaterial20 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lootShowerMaterial.20", Material.EMERALD.name(), false);
      ConfigurationEngine.setString(this.fileConfiguration, "lootShowerDataV2.20", "elitemobs:coins/coin3");
      lootShowerMaterial50 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lootShowerMaterial.50", Material.EMERALD_BLOCK.name(), false);
      ConfigurationEngine.setString(this.fileConfiguration, "lootShowerDataV2.50", "elitemobs:coins/coin4");
      lootShowerMaterial100 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lootShowerMaterial.100", Material.DIAMOND.name(), false);
      ConfigurationEngine.setString(this.fileConfiguration, "lootShowerDataV2.100", "elitemobs:coins/coin4");
      lootShowerMaterial500 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lootShowerMaterial.500", Material.DIAMOND_BLOCK.name(), false);
      ConfigurationEngine.setString(this.fileConfiguration, "lootShowerDataV2.500", "elitemobs:coins/coin4");
      lootShowerMaterial1000 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "lootShowerMaterial.1000", Material.NETHER_STAR.name(), false);
      ConfigurationEngine.setString(this.fileConfiguration, "lootShowerDataV2.1000", "elitemobs:coins/coin4");
      adventurersGuildNotificationMessage = ConfigurationEngine.setString(
         List.of("Hint message shown to players after picking up currency, pointing them to the Adventurer's Guild."),
         this.file,
         this.fileConfiguration,
         "adventurersGuildNotificationMessages",
         "&7[EM] Extra spending money? Try &a/ag !",
         true
      );
      addMaterial(this.fileConfiguration, Material.DIAMOND_AXE, diamondLevel);
      addMaterial(this.fileConfiguration, Material.DIAMOND_BOOTS, diamondLevel);
      addMaterial(this.fileConfiguration, Material.DIAMOND_CHESTPLATE, diamondLevel);
      addMaterial(this.fileConfiguration, Material.DIAMOND_LEGGINGS, diamondLevel);
      addMaterial(this.fileConfiguration, Material.DIAMOND_HELMET, diamondLevel);
      addMaterial(this.fileConfiguration, Material.DIAMOND_PICKAXE, diamondLevel);
      addMaterial(this.fileConfiguration, Material.DIAMOND_SHOVEL, diamondLevel);
      addMaterial(this.fileConfiguration, Material.DIAMOND_SWORD, diamondLevel);
      addMaterial(this.fileConfiguration, Material.DIAMOND_HOE, diamondLevel);
      addMaterial(this.fileConfiguration, Material.IRON_AXE, ironLevel);
      addMaterial(this.fileConfiguration, Material.IRON_BOOTS, ironLevel);
      addMaterial(this.fileConfiguration, Material.IRON_LEGGINGS, ironLevel);
      addMaterial(this.fileConfiguration, Material.IRON_CHESTPLATE, ironLevel);
      addMaterial(this.fileConfiguration, Material.IRON_HELMET, ironLevel);
      addMaterial(this.fileConfiguration, Material.IRON_PICKAXE, ironLevel);
      addMaterial(this.fileConfiguration, Material.IRON_SHOVEL, ironLevel);
      addMaterial(this.fileConfiguration, Material.IRON_HOE, ironLevel);
      addMaterial(this.fileConfiguration, Material.IRON_SWORD, ironLevel);
      addMaterial(this.fileConfiguration, Material.SHIELD, ironLevel);
      addMaterial(this.fileConfiguration, Material.BOW, ironLevel);
      addMaterial(this.fileConfiguration, Material.CHAINMAIL_BOOTS, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.CHAINMAIL_LEGGINGS, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.CHAINMAIL_CHESTPLATE, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.CHAINMAIL_HELMET, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.STONE_SWORD, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.STONE_AXE, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.STONE_PICKAXE, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.STONE_SHOVEL, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.STONE_HOE, stoneChainLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_AXE, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_BOOTS, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_LEGGINGS, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_CHESTPLATE, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_HELMET, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_SWORD, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_SHOVEL, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_PICKAXE, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_HOE, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.GOLDEN_APPLE, diamondLevel);
      addMaterial(this.fileConfiguration, Material.ENCHANTED_GOLDEN_APPLE, diamondLevel);
      addMaterial(this.fileConfiguration, Material.LEATHER_BOOTS, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.LEATHER_LEGGINGS, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.LEATHER_CHESTPLATE, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.LEATHER_HELMET, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.WOODEN_SWORD, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.WOODEN_AXE, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.WOODEN_HOE, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.WOODEN_PICKAXE, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.TRIDENT, tridentLevel);
      addMaterial(this.fileConfiguration, Material.ELYTRA, diamondLevel);
      addMaterial(this.fileConfiguration, Material.TURTLE_HELMET, goldWoodLeatherLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_AXE, netheriteLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_PICKAXE, netheriteLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_SHOVEL, netheriteLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_HOE, netheriteLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_SWORD, netheriteLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_HELMET, netheriteLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_CHESTPLATE, netheriteLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_LEGGINGS, netheriteLevel);
      addMaterial(this.fileConfiguration, Material.NETHERITE_BOOTS, netheriteLevel);
      defaultMaterialWorth = ConfigurationEngine.setDouble(
         List.of("Default currency value for materials not listed above."), this.fileConfiguration, "materialWorth.defaultMaterialWorth", 1.0
      );
      playerToPlayerTaxes = ConfigurationEngine.setDouble(
         List.of(
            "Tax rate (0.0-1.0) on player-to-player currency transfers. 0.2 = 20% tax.",
            "Recommended to prevent high-level players from funneling currency through alts to avoid taxes."
         ),
         this.fileConfiguration,
         "playerToPlayerPaymentTaxes",
         0.2
      );
      economyPayMessage = ConfigurationEngine.setString(
         List.of("Message shown to the sender when paying another player.", "Placeholders: $amount_sent, $currency_name, $receiver, $amount_received"),
         this.file,
         this.fileConfiguration,
         "economyPayMessageV2",
         "&2You have paid &2$amount_sent $currency_name &2to $receiver&2, who got $amount_received after taxes!",
         true
      );
      economyCurrencyLeftMessage = ConfigurationEngine.setString(
         List.of("Message showing remaining balance after paying another player.", "Placeholders: $amount_left, $currency_name"),
         this.file,
         this.fileConfiguration,
         "economyCurrencyLeftMessage",
         "You now have &2$amount_left $currency_name",
         true
      );
      economyPaymentReceivedMessage = ConfigurationEngine.setString(
         List.of("Message shown to the recipient when receiving currency from another player.", "Placeholders: $amount_received, $currency_name, $sender"),
         this.file,
         this.fileConfiguration,
         "economyPaymentReceivedMessage",
         "You have received &2$amount_received $currency_name &ffrom $sender",
         true
      );
      economyPaymentInsufficientCurrency = ConfigurationEngine.setString(
         List.of("Message shown when a player tries to pay another player more currency than they have.", "Placeholder: $currency_name"),
         this.file,
         this.fileConfiguration,
         "economyPaymentInsufficientCurrency",
         "&cYou don't have enough $currency_name to do that!",
         true
      );
      economyWalletCommand = ConfigurationEngine.setString(
         List.of("Message shown when a player checks their balance with /em wallet.", "Placeholders: $balance, $currency_name"),
         this.file,
         this.fileConfiguration,
         "walletCommandMessage",
         "You have &2$balance $currency_name",
         true
      );
      shopBuyMessage = ConfigurationEngine.setString(
         List.of("Message shown when a player buys an item from the shop.", "Placeholders: $item_name, $item_value, $currency_name"),
         this.file,
         this.fileConfiguration,
         "shopBuyMessage",
         "&aYou have bought $item_name &afor $item_value $currency_name!",
         true
      );
      shopCurrentBalance = ConfigurationEngine.setString(
         List.of("Message showing the player's current balance when interacting with a shop.", "Placeholders: $currency_amount, $currency_name"),
         this.file,
         this.fileConfiguration,
         "shopCurrentBalanceMessage",
         "&aYou have $currency_amount $currency_name.",
         true
      );
      shopInsufficientFundsMessage = ConfigurationEngine.setString(
         List.of("Message shown when a player cannot afford to buy an item from the shop.", "Placeholder: $currency_name"),
         this.file,
         this.fileConfiguration,
         "shopInsufficientFundsMessage",
         "&cYou don't have enough $currency_name!",
         true
      );
      shopItemPrice = ConfigurationEngine.setString(
         List.of("Message showing the price of an item the player cannot afford.", "Placeholders: $item_value, $currency_name"),
         this.file,
         this.fileConfiguration,
         "shopItemCostMessage",
         "That item costs &c$item_value $currency_name.",
         true
      );
      shopSellMessage = ConfigurationEngine.setString(
         List.of("Message shown when a player sells an item to the shop.", "Placeholders: $item_name, $currency_amount, $currency_name"),
         this.file,
         this.fileConfiguration,
         "shopSellMessage",
         "&aYou have sold $item_name &afor $currency_amount $currency_name!",
         true
      );
      shopSaleOthersItems = ConfigurationEngine.setString(
         List.of("Message shown when a player tries to sell an item that is not soulbound to them."),
         this.file,
         this.fileConfiguration,
         "shopSalePlayerItemsWarning",
         "&cYou can't sell items that are not currently soulbound to you!",
         true
      );
      shopSaleInstructions = ConfigurationEngine.setString(
         List.of("Message shown when a player tries to sell a non-EliteMobs item to the shop."),
         this.file,
         this.fileConfiguration,
         "shopSaleInstructions",
         "&cYou can only sell EliteMobs loot here! (Armor / weapons dropped from elites showing a value on their lore)",
         true
      );
      shopBatchSellMessage = ConfigurationEngine.setString(
         List.of("Message sent upon selling a batch of elite items."),
         this.file,
         this.fileConfiguration,
         "shopBatchSellItem",
         "&aYou have sold your items &afor $currency_amount $currency_name!",
         true
      );
   }

   @Generated
   public static boolean isEnableEconomy() {
      return enableEconomy;
   }

   @Generated
   public static double getResaleValue() {
      return resaleValue;
   }

   @Generated
   public static String getCurrencyName() {
      return currencyName;
   }

   @Generated
   public static boolean isUseVault() {
      return useVault;
   }

   @Generated
   public static boolean isEnableCurrencyShower() {
      return enableCurrencyShower;
   }

   @Generated
   public static double getCurrencyShowerMultiplier() {
      return currencyShowerMultiplier;
   }

   @Generated
   public static String getChatCurrencyShowerMessage() {
      return chatCurrencyShowerMessage;
   }

   @Generated
   public static String getActionBarCurrencyShowerMessage() {
      return actionBarCurrencyShowerMessage;
   }

   @Generated
   public static String getLootShowerMaterial1() {
      return lootShowerMaterial1;
   }

   @Generated
   public static String getLootShowerMaterial5() {
      return lootShowerMaterial5;
   }

   @Generated
   public static String getLootShowerMaterial10() {
      return lootShowerMaterial10;
   }

   @Generated
   public static String getLootShowerMaterial20() {
      return lootShowerMaterial20;
   }

   @Generated
   public static String getLootShowerMaterial50() {
      return lootShowerMaterial50;
   }

   @Generated
   public static String getLootShowerMaterial100() {
      return lootShowerMaterial100;
   }

   @Generated
   public static String getLootShowerMaterial500() {
      return lootShowerMaterial500;
   }

   @Generated
   public static String getLootShowerMaterial1000() {
      return lootShowerMaterial1000;
   }

   @Generated
   public static String getAdventurersGuildNotificationMessage() {
      return adventurersGuildNotificationMessage;
   }

   @Generated
   public static double getDefaultMaterialWorth() {
      return defaultMaterialWorth;
   }

   @Generated
   public static FileConfiguration getThisConfiguration() {
      return thisConfiguration;
   }

   @Generated
   public static double getPlayerToPlayerTaxes() {
      return playerToPlayerTaxes;
   }

   @Generated
   public static String getEconomyPayMessage() {
      return economyPayMessage;
   }

   @Generated
   public static String getEconomyCurrencyLeftMessage() {
      return economyCurrencyLeftMessage;
   }

   @Generated
   public static String getEconomyPaymentReceivedMessage() {
      return economyPaymentReceivedMessage;
   }

   @Generated
   public static String getEconomyPaymentInsufficientCurrency() {
      return economyPaymentInsufficientCurrency;
   }

   @Generated
   public static String getEconomyWalletCommand() {
      return economyWalletCommand;
   }

   @Generated
   public static String getShopBuyMessage() {
      return shopBuyMessage;
   }

   @Generated
   public static String getShopSellMessage() {
      return shopSellMessage;
   }

   @Generated
   public static String getShopInsufficientFundsMessage() {
      return shopInsufficientFundsMessage;
   }

   @Generated
   public static String getShopSaleInstructions() {
      return shopSaleInstructions;
   }

   @Generated
   public static String getShopSaleOthersItems() {
      return shopSaleOthersItems;
   }

   @Generated
   public static String getShopCurrentBalance() {
      return shopCurrentBalance;
   }

   @Generated
   public static String getShopItemPrice() {
      return shopItemPrice;
   }

   @Generated
   public static String getShopBatchSellMessage() {
      return shopBatchSellMessage;
   }

   @Generated
   public static String getShopTransactionFailedMessage() {
      return shopTransactionFailedMessage;
   }
}
