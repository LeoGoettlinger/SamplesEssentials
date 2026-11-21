package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import lombok.Generated;

public class LootMenuConfig extends MenusConfigFields {
   private static String greedListTitle;
   private static String greedListLore1;
   private static String greedListLore2;
   private static String greedListLore3;
   private static String needListTitle;
   private static String needListLore1;
   private static String needListLore2;
   private static String needListLore3;
   private static String noGroupLootMessage;
   private static String previousPageTitle;
   private static String nextPageTitle;
   private static String pageIndicatorTitle;

   public LootMenuConfig() {
      super("loot_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      greedListTitle = ConfigurationEngine.setString(this.file, this.fileConfiguration, "greedListTitle", "&2Greed Item List", true);
      greedListLore1 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "greedListLore1", "Click to move to the Need item list!", true);
      greedListLore2 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "greedListLore2", "Items in the greed list will only", true);
      greedListLore3 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "greedListLore3", "be rolled for if no one needs them!", true);
      needListTitle = ConfigurationEngine.setString(this.file, this.fileConfiguration, "needListTitle", "&2Need Item List", true);
      needListLore1 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "needListLore1", "Click to move to the Greed item list!", true);
      needListLore2 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "needListLore2", "Items in the need list will only", true);
      needListLore3 = ConfigurationEngine.setString(this.file, this.fileConfiguration, "needListLore3", "be rolled for people who needed them!", true);
      noGroupLootMessage = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "noGroupLootMessage", "&4[EliteMobs] &6You don't currently have any group loot to vote on!", true
      );
      previousPageTitle = ConfigurationEngine.setString(this.file, this.fileConfiguration, "previousPageTitle", "&6Previous loot page", true);
      nextPageTitle = ConfigurationEngine.setString(this.file, this.fileConfiguration, "nextPageTitle", "&6Next loot page", true);
      pageIndicatorTitle = ConfigurationEngine.setString(this.file, this.fileConfiguration, "pageIndicatorTitle", "&8Loot page &f$current&8/&f$total", true);
   }

   @Generated
   public static String getGreedListTitle() {
      return greedListTitle;
   }

   @Generated
   public static String getGreedListLore1() {
      return greedListLore1;
   }

   @Generated
   public static String getGreedListLore2() {
      return greedListLore2;
   }

   @Generated
   public static String getGreedListLore3() {
      return greedListLore3;
   }

   @Generated
   public static String getNeedListTitle() {
      return needListTitle;
   }

   @Generated
   public static String getNeedListLore1() {
      return needListLore1;
   }

   @Generated
   public static String getNeedListLore2() {
      return needListLore2;
   }

   @Generated
   public static String getNeedListLore3() {
      return needListLore3;
   }

   @Generated
   public static String getNoGroupLootMessage() {
      return noGroupLootMessage;
   }

   @Generated
   public static String getPreviousPageTitle() {
      return previousPageTitle;
   }

   @Generated
   public static String getNextPageTitle() {
      return nextPageTitle;
   }

   @Generated
   public static String getPageIndicatorTitle() {
      return pageIndicatorTitle;
   }
}
