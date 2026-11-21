package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EliteScrollMenuConfig extends MenusConfigFields {
   private static String menuName;
   private static String scrollNotOwnedMessage;
   private static int infoSlot;
   private static ItemStack infoButton;
   private static int cancelSlot;
   private static ItemStack cancelButton;
   private static int confirmSlot;
   private static ItemStack confirmButton;
   private static int nonEliteItemSlot;
   private static int nonEliteItemInfoSlot;
   private static ItemStack nonEliteItemInfoButton;
   private static int eliteScrollItemSlot;
   private static int eliteScrollItemInfoSlot;
   private static ItemStack eliteScrollItemInfoButton;
   private static ItemStack outputInfoButton;
   private static int outputInfoSlot;
   private static int outputSlot;

   public EliteScrollMenuConfig() {
      super("elite_scroll_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      scrollNotOwnedMessage = ConfigurationEngine.setString(
         List.of("Shown when a player tries to use another player's soulbound elite scroll."),
         this.file,
         this.fileConfiguration,
         "scrollNotOwnedMessage",
         "&cYou can't use a scroll soulbound to someone else.",
         true
      );
      menuName = ConfigurationEngine.setString(
         List.of("Sets the display name of the menu"), this.file, this.fileConfiguration, "menuName", "&6Elite Scroll Menu", true
      );
      infoSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the information button in the chest menu."), this.fileConfiguration, "infoSlot", 4
      );
      ItemStackSerializer.serialize(
         "infoButton",
         ItemStackGenerator.generateSkullItemStack(
            "magmaguy",
            "&2Elite Scroll info:",
            new ArrayList<>(
               List.of(
                  "&2This menu converts non-elite items into elite items!",
                  "&2Elite items have elite dps or elite defense which applies",
                  "&2to elite mobs.",
                  "&2To convert a non-elite item into an elite item, place",
                  "&2your non-elite item below, as well as a Elite Scroll!"
               )
            )
         ),
         this.fileConfiguration
      );
      infoButton = ItemStackSerializer.deserialize("infoButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(infoButton, CustomModelsConfig.goldenQuestionMark);
      cancelSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the cancel button in the chest menu."), this.fileConfiguration, "cancelSlot", 27
      );
      ItemStackSerializer.serialize(
         "cancelButton",
         ItemStackGenerator.generateItemStack(Material.BARRIER, "&4Cancel", List.of("&cCancel!"), MetadataHandler.signatureID),
         this.fileConfiguration
      );
      cancelButton = ItemStackSerializer.deserialize("cancelButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(cancelButton, CustomModelsConfig.redCross);
      confirmSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the confirm button in the chest menu."), this.fileConfiguration, "confirmSlot", 35
      );
      ItemStackSerializer.serialize(
         "confirmButton",
         ItemStackGenerator.generateItemStack(Material.EMERALD, "&2Apply!", new ArrayList<>(), "elitemobs:ui/anvilhammer"),
         this.fileConfiguration
      );
      confirmButton = ItemStackSerializer.deserialize("confirmButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.anvilHammer);
      nonEliteItemSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the non-elite item in the chest menu."), this.fileConfiguration, "itemSlot", 29
      );
      nonEliteItemInfoSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the non-elite item info button in the chest menu."), this.fileConfiguration, "itemInfoSlot", 20
      );
      ItemStackSerializer.serialize(
         "itemInfoButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER, "&2Non-Elite Item Input slot", List.of("&aPut your non-elite item here!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      nonEliteItemInfoButton = ItemStackSerializer.deserialize("itemInfoButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(nonEliteItemInfoButton, CustomModelsConfig.boxInput);
      eliteScrollItemInfoSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the elite scroll item info button in the chest menu."), this.fileConfiguration, "eliteScrollItemInfoSlot", 22
      );
      ItemStackSerializer.serialize(
         "eliteScrollItemInfoButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER, "&2Elite Scroll Input slot", List.of("&aPut your elite scroll here!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      eliteScrollItemInfoButton = ItemStackSerializer.deserialize("eliteScrollItemInfoButton", this.fileConfiguration, this.file);
      eliteScrollItemSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of elite scroll in the chest menu."), this.fileConfiguration, "eliteScrollItemSlot", 31
      );
      outputInfoButton = ConfigurationEngine.setItemStack(
         this.file, this.fileConfiguration, "outputInfoButton", ItemStackGenerator.generateItemStack(Material.RED_BANNER, "&2Result"), true
      );
      CustomModelAdder.addCustomModel(outputInfoButton, CustomModelsConfig.boxOutput);
      outputInfoSlot = ConfigurationEngine.setInt(this.fileConfiguration, "outputInfoSlot", 24);
      outputSlot = ConfigurationEngine.setInt(this.fileConfiguration, "outputSlot", 33);
      CustomModelAdder.addCustomModel(eliteScrollItemInfoButton, CustomModelsConfig.boxInput);
   }

   @Generated
   public static String getMenuName() {
      return menuName;
   }

   @Generated
   public static String getScrollNotOwnedMessage() {
      return scrollNotOwnedMessage;
   }

   @Generated
   public static int getInfoSlot() {
      return infoSlot;
   }

   @Generated
   public static ItemStack getInfoButton() {
      return infoButton;
   }

   @Generated
   public static int getCancelSlot() {
      return cancelSlot;
   }

   @Generated
   public static ItemStack getCancelButton() {
      return cancelButton;
   }

   @Generated
   public static int getConfirmSlot() {
      return confirmSlot;
   }

   @Generated
   public static ItemStack getConfirmButton() {
      return confirmButton;
   }

   @Generated
   public static int getNonEliteItemSlot() {
      return nonEliteItemSlot;
   }

   @Generated
   public static int getNonEliteItemInfoSlot() {
      return nonEliteItemInfoSlot;
   }

   @Generated
   public static ItemStack getNonEliteItemInfoButton() {
      return nonEliteItemInfoButton;
   }

   @Generated
   public static int getEliteScrollItemSlot() {
      return eliteScrollItemSlot;
   }

   @Generated
   public static int getEliteScrollItemInfoSlot() {
      return eliteScrollItemInfoSlot;
   }

   @Generated
   public static ItemStack getEliteScrollItemInfoButton() {
      return eliteScrollItemInfoButton;
   }

   @Generated
   public static ItemStack getOutputInfoButton() {
      return outputInfoButton;
   }

   @Generated
   public static int getOutputInfoSlot() {
      return outputInfoSlot;
   }

   @Generated
   public static int getOutputSlot() {
      return outputSlot;
   }
}
