package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import lombok.Generated;

public class ArrowShopMenuConfig extends MenusConfigFields {
   private static String shopName;
   private static String insufficientFundsMessage;
   private static String purchaseSuccessMessage;
   private static String inventoryFullMessage;
   private static int arrowSmallStackPrice;
   private static int arrowSmallStackAmount;
   private static int arrowLargeStackPrice;
   private static int arrowLargeStackAmount;
   private static int spectralArrowPrice;
   private static int spectralArrowAmount;
   private static int tippedArrowPoisonPrice;
   private static int tippedArrowPoisonAmount;
   private static int tippedArrowSlownessPrice;
   private static int tippedArrowSlownessAmount;
   private static int tippedArrowWeaknessPrice;
   private static int tippedArrowWeaknessAmount;
   private static int tippedArrowHarmingPrice;
   private static int tippedArrowHarmingAmount;
   private static int tippedArrowHealingPrice;
   private static int tippedArrowHealingAmount;
   private static String arrowDisplayName;
   private static String spectralArrowDisplayName;
   private static String arrowOfPoisonDisplayName;
   private static String arrowOfSlownessDisplayName;
   private static String arrowOfWeaknessDisplayName;
   private static String arrowOfHarmingDisplayName;
   private static String arrowOfHealingDisplayName;
   private static String shopItemAmountFormat;
   private static String shopItemPriceFormat;
   private static String shopItemClickToPurchase;

   public ArrowShopMenuConfig() {
      super("arrow_shop_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      shopName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "shopName", "&2&lFletcher's Arrow Shop", true);
      insufficientFundsMessage = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "insufficientFundsMessage", "&c[EliteMobs] &7You don't have enough Elite Coins! You need &f%price% &7coins.", true
      );
      purchaseSuccessMessage = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "purchaseSuccessMessage", "&a[EliteMobs] &7Purchased &f%amount%x %item% &7for &f%price% &7Elite Coins!", true
      );
      inventoryFullMessage = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "inventoryFullMessage", "&c[EliteMobs] &7Your inventory is full!", true
      );
      arrowSmallStackPrice = ConfigurationEngine.setInt(this.fileConfiguration, "arrowSmallStackPrice", 10);
      arrowSmallStackAmount = ConfigurationEngine.setInt(this.fileConfiguration, "arrowSmallStackAmount", 16);
      arrowLargeStackPrice = ConfigurationEngine.setInt(this.fileConfiguration, "arrowLargeStackPrice", 35);
      arrowLargeStackAmount = ConfigurationEngine.setInt(this.fileConfiguration, "arrowLargeStackAmount", 64);
      spectralArrowPrice = ConfigurationEngine.setInt(this.fileConfiguration, "spectralArrowPrice", 40);
      spectralArrowAmount = ConfigurationEngine.setInt(this.fileConfiguration, "spectralArrowAmount", 16);
      tippedArrowPoisonPrice = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowPoisonPrice", 50);
      tippedArrowPoisonAmount = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowPoisonAmount", 8);
      tippedArrowSlownessPrice = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowSlownessPrice", 50);
      tippedArrowSlownessAmount = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowSlownessAmount", 8);
      tippedArrowWeaknessPrice = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowWeaknessPrice", 50);
      tippedArrowWeaknessAmount = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowWeaknessAmount", 8);
      tippedArrowHarmingPrice = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowHarmingPrice", 75);
      tippedArrowHarmingAmount = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowHarmingAmount", 8);
      tippedArrowHealingPrice = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowHealingPrice", 75);
      tippedArrowHealingAmount = ConfigurationEngine.setInt(this.fileConfiguration, "tippedArrowHealingAmount", 8);
      arrowDisplayName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "arrowDisplayName", "&fArrows", true);
      spectralArrowDisplayName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "spectralArrowDisplayName", "&eSpectral Arrows", true);
      arrowOfPoisonDisplayName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "arrowOfPoisonDisplayName", "&2Arrow of Poison", true);
      arrowOfSlownessDisplayName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "arrowOfSlownessDisplayName", "&8Arrow of Slowness", true);
      arrowOfWeaknessDisplayName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "arrowOfWeaknessDisplayName", "&7Arrow of Weakness", true);
      arrowOfHarmingDisplayName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "arrowOfHarmingDisplayName", "&4Arrow of Harming", true);
      arrowOfHealingDisplayName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "arrowOfHealingDisplayName", "&dArrow of Healing", true);
      shopItemAmountFormat = ConfigurationEngine.setString(this.file, this.fileConfiguration, "shopItemAmountFormat", "&7Amount: &f$amount", true);
      shopItemPriceFormat = ConfigurationEngine.setString(this.file, this.fileConfiguration, "shopItemPriceFormat", "&7Price: &6$price &7Elite Coins", true);
      shopItemClickToPurchase = ConfigurationEngine.setString(this.file, this.fileConfiguration, "shopItemClickToPurchase", "&eClick to purchase!", true);
   }

   @Generated
   public static String getShopName() {
      return shopName;
   }

   @Generated
   public static String getInsufficientFundsMessage() {
      return insufficientFundsMessage;
   }

   @Generated
   public static String getPurchaseSuccessMessage() {
      return purchaseSuccessMessage;
   }

   @Generated
   public static String getInventoryFullMessage() {
      return inventoryFullMessage;
   }

   @Generated
   public static int getArrowSmallStackPrice() {
      return arrowSmallStackPrice;
   }

   @Generated
   public static int getArrowSmallStackAmount() {
      return arrowSmallStackAmount;
   }

   @Generated
   public static int getArrowLargeStackPrice() {
      return arrowLargeStackPrice;
   }

   @Generated
   public static int getArrowLargeStackAmount() {
      return arrowLargeStackAmount;
   }

   @Generated
   public static int getSpectralArrowPrice() {
      return spectralArrowPrice;
   }

   @Generated
   public static int getSpectralArrowAmount() {
      return spectralArrowAmount;
   }

   @Generated
   public static int getTippedArrowPoisonPrice() {
      return tippedArrowPoisonPrice;
   }

   @Generated
   public static int getTippedArrowPoisonAmount() {
      return tippedArrowPoisonAmount;
   }

   @Generated
   public static int getTippedArrowSlownessPrice() {
      return tippedArrowSlownessPrice;
   }

   @Generated
   public static int getTippedArrowSlownessAmount() {
      return tippedArrowSlownessAmount;
   }

   @Generated
   public static int getTippedArrowWeaknessPrice() {
      return tippedArrowWeaknessPrice;
   }

   @Generated
   public static int getTippedArrowWeaknessAmount() {
      return tippedArrowWeaknessAmount;
   }

   @Generated
   public static int getTippedArrowHarmingPrice() {
      return tippedArrowHarmingPrice;
   }

   @Generated
   public static int getTippedArrowHarmingAmount() {
      return tippedArrowHarmingAmount;
   }

   @Generated
   public static int getTippedArrowHealingPrice() {
      return tippedArrowHealingPrice;
   }

   @Generated
   public static int getTippedArrowHealingAmount() {
      return tippedArrowHealingAmount;
   }

   @Generated
   public static String getArrowDisplayName() {
      return arrowDisplayName;
   }

   @Generated
   public static String getSpectralArrowDisplayName() {
      return spectralArrowDisplayName;
   }

   @Generated
   public static String getArrowOfPoisonDisplayName() {
      return arrowOfPoisonDisplayName;
   }

   @Generated
   public static String getArrowOfSlownessDisplayName() {
      return arrowOfSlownessDisplayName;
   }

   @Generated
   public static String getArrowOfWeaknessDisplayName() {
      return arrowOfWeaknessDisplayName;
   }

   @Generated
   public static String getArrowOfHarmingDisplayName() {
      return arrowOfHarmingDisplayName;
   }

   @Generated
   public static String getArrowOfHealingDisplayName() {
      return arrowOfHealingDisplayName;
   }

   @Generated
   public static String getShopItemAmountFormat() {
      return shopItemAmountFormat;
   }

   @Generated
   public static String getShopItemPriceFormat() {
      return shopItemPriceFormat;
   }

   @Generated
   public static String getShopItemClickToPurchase() {
      return shopItemClickToPurchase;
   }
}
