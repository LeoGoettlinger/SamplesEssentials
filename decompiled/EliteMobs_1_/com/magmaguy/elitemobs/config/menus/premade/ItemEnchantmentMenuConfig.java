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

public class ItemEnchantmentMenuConfig extends MenusConfigFields {
   private static String menuName;
   private static int infoSlot;
   private static ItemStack infoButton;
   private static int cancelSlot;
   private static ItemStack cancelButton;
   private static int confirmSlot;
   private static ItemStack confirmButton;
   private static int itemSlot;
   private static int itemInfoSlot;
   private static ItemStack itemInfoButton;
   private static int enchantedBookSlot;
   private static int enchantedBookInfoSlot;
   private static ItemStack enchantedBookInfoButton;
   private static int luckyTicketSlot;
   private static int luckyTicketInfoSlot;
   private static ItemStack luckyTicketInfoButton;
   private static String enchantmentLimitMessage;
   private static String incompatibleEnchantmentMessage;
   private static String missingItemsMessage;

   public ItemEnchantmentMenuConfig() {
      super("item_enchantment_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      menuName = ConfigurationEngine.setString(
         List.of("Sets the display name of the menu"), this.file, this.fileConfiguration, "menuName", "&6Enchantment Menu", true
      );
      infoSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the information button in the chest menu."), this.fileConfiguration, "infoSlot", 4
      );
      ItemStackSerializer.serialize(
         "infoButton",
         ItemStackGenerator.generateSkullItemStack(
            "magmaguy",
            "&2Enchantment info:",
            new ArrayList<>(
               List.of(
                  "&2This menu can add enchants to your elite items!",
                  "&aIn order to enchant your elite item, add your",
                  "&aelite item and your elite enchanted books!",
                  "&6Items that already have a lot of enchantments",
                  "&6(high quality items) have a high chance of failing!",
                  "&8Normal failure makes you fail to enchant the item.",
                  "&8Challenge makes you fight a boss for the enchant.",
                  "&8Critical failures make you lose the item!"
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
         ItemStackGenerator.generateItemStack(Material.BARRIER, "&4Cancel", List.of("&cCancel repair!"), MetadataHandler.signatureID),
         this.fileConfiguration
      );
      cancelButton = ItemStackSerializer.deserialize("cancelButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(cancelButton, CustomModelsConfig.redCross);
      confirmSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the confirm button in the chest menu."), this.fileConfiguration, "confirmSlot", 35
      );
      ItemStackSerializer.serialize(
         "confirmButton",
         ItemStackGenerator.generateItemStack(
            Material.EMERALD,
            "&2Enchant!",
            List.of(
               "&aPrice: $price $currencyName",
               "&2Chance of success: $successChance%",
               "&cChance of failure: $failureChance%",
               "&6Challenge chance: $challengeChance%",
               "&4Chance of critical failure: $criticalFailureChance%"
            ),
            "elitemobs:ui/anvilhammer"
         ),
         this.fileConfiguration
      );
      confirmButton = ItemStackSerializer.deserialize("confirmButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.anvilHammer);
      itemSlot = ConfigurationEngine.setInt(List.of("Sets the item slot number of the elite item in the chest menu."), this.fileConfiguration, "itemSlot", 29);
      itemInfoSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the elite item info button in the chest menu."), this.fileConfiguration, "itemInfoSlot", 20
      );
      ItemStackSerializer.serialize(
         "itemInfoButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER, "&2Elite Item Input slot", List.of("&aPut your elite item here!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      itemInfoButton = ItemStackSerializer.deserialize("itemInfoButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(itemInfoButton, CustomModelsConfig.boxInput);
      enchantedBookInfoSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the enchanted book info button in the chest menu."), this.fileConfiguration, "enchantedBookInfoSlot", 22
      );
      ItemStackSerializer.serialize(
         "enchantedBookInfoButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER, "&2Enchanted Book Input slot", List.of("&aPut your elite enchanted books here!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      enchantedBookInfoButton = ItemStackSerializer.deserialize("enchantedBookInfoButton", this.fileConfiguration, this.file);
      enchantedBookSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the enchanted book in the chest menu."), this.fileConfiguration, "enchantedBookSlot", 31
      );
      CustomModelAdder.addCustomModel(enchantedBookInfoButton, CustomModelsConfig.boxInput);
      luckyTicketSlot = ConfigurationEngine.setInt(this.fileConfiguration, "luckyTicketSlot", 33);
      luckyTicketInfoSlot = ConfigurationEngine.setInt(
         List.of("Sets the item slot number of the lucky ticket info button in the chest menu."), this.fileConfiguration, "luckyTicketInfoSlot", 24
      );
      ItemStackSerializer.serialize(
         "luckyTicketInfoButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER,
            "&2Elite Lucky Ticket Input slot",
            List.of(
               "&a(Optional) Put your elite lucky tickets here!", "&2Increase the odds of the enchantment succeeding", "&2by adding an elite lucky ticket!"
            ),
            MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      luckyTicketInfoButton = ItemStackSerializer.deserialize("luckyTicketInfoButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(luckyTicketInfoButton, CustomModelsConfig.boxInput);
      incompatibleEnchantmentMessage = ConfigurationEngine.setString(
         this.file, this.fileConfiguration, "incompatibleEnchantmentMessage", "&cThat enchantment cannot be used on this item.", true
      );
      enchantmentLimitMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when a player attempts to enchant an item beyond the configured maximum enchantment level of any enchantment."),
         this.file,
         this.fileConfiguration,
         "enchantmentLimitMessage",
         "&c[EliteMobs] Can't enchant item beyond enchantment limit!",
         true
      );
      missingItemsMessage = ConfigurationEngine.setString(
         List.of("Sets the message that appears when a player tries to enchant without adding both an elite item and an enchanted book."),
         this.file,
         this.fileConfiguration,
         "missingItemsMessage",
         "&8[EliteMobs] &cYou must add an elite item and an enchanted book to enchant an item!",
         true
      );
   }

   @Generated
   public static String getMenuName() {
      return menuName;
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
   public static int getItemSlot() {
      return itemSlot;
   }

   @Generated
   public static int getItemInfoSlot() {
      return itemInfoSlot;
   }

   @Generated
   public static ItemStack getItemInfoButton() {
      return itemInfoButton;
   }

   @Generated
   public static int getEnchantedBookSlot() {
      return enchantedBookSlot;
   }

   @Generated
   public static int getEnchantedBookInfoSlot() {
      return enchantedBookInfoSlot;
   }

   @Generated
   public static ItemStack getEnchantedBookInfoButton() {
      return enchantedBookInfoButton;
   }

   @Generated
   public static int getLuckyTicketSlot() {
      return luckyTicketSlot;
   }

   @Generated
   public static int getLuckyTicketInfoSlot() {
      return luckyTicketInfoSlot;
   }

   @Generated
   public static ItemStack getLuckyTicketInfoButton() {
      return luckyTicketInfoButton;
   }

   @Generated
   public static String getEnchantmentLimitMessage() {
      return enchantmentLimitMessage;
   }

   @Generated
   public static String getIncompatibleEnchantmentMessage() {
      return incompatibleEnchantmentMessage;
   }

   @Generated
   public static String getMissingItemsMessage() {
      return missingItemsMessage;
   }
}
