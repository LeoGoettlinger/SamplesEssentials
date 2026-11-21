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

public class UnbinderMenuConfig extends MenusConfigFields {
   private static String shopName;
   private static ItemStack infoButton;
   private static ItemStack eliteItemInputInfoButton;
   private static ItemStack eliteUnbindInputInfoButton;
   private static ItemStack outputInfoButton;
   private static int infoSlot;
   private static int eliteItemInputInformationSlot;
   private static int eliteScrapInputInformationSlot;
   private static int outputInformationSlot;
   private static int eliteItemInputSlot;
   private static int eliteUnbindInputSlot;
   private static int outputSlot;
   private static ItemStack cancelButton;
   private static int cancelSlot;
   private static ItemStack confirmButton;
   private static int confirmSlot;

   public UnbinderMenuConfig() {
      super("unbind_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      shopName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "shopName", "[EM] Unbind menu!", true);
      ItemStackSerializer.serialize(
         "infoButton",
         ItemStackGenerator.generateSkullItemStack(
            "magmaguy",
            "&4&lEliteMobs &r&cby &4&lMagmaGuy",
            new ArrayList<>(List.of("&8Support the plugins you enjoy!", "&aUse an &5Unbind Scroll &ato remove Soulbind from an item!"))
         ),
         this.fileConfiguration
      );
      infoButton = ItemStackSerializer.deserialize("infoButton", this.fileConfiguration, this.file);
      infoSlot = ConfigurationEngine.setInt(this.fileConfiguration, "infoButtonSlot", 4);
      eliteItemInputInformationSlot = ConfigurationEngine.setInt(this.fileConfiguration, "eliteItemInputInformationSlot", 20);
      eliteItemInputSlot = ConfigurationEngine.setInt(this.fileConfiguration, "eliteItemInputSlot", 29);
      ItemStackSerializer.serialize(
         "eliteItemInputInformationButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER, "&2Elite Item Input slots", List.of("&aThis slot is for your elite item!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      eliteItemInputInfoButton = ItemStackSerializer.deserialize("eliteItemInputInformationButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(eliteItemInputInfoButton, CustomModelsConfig.boxInput);
      eliteScrapInputInformationSlot = ConfigurationEngine.setInt(this.fileConfiguration, "unbindScrollInputInformationSlot", 22);
      eliteUnbindInputSlot = ConfigurationEngine.setInt(this.fileConfiguration, "unbindScrollInputSlot", 31);
      ItemStackSerializer.serialize(
         "unbindScrollInputInformationButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER, "&5Unbind Scroll &aInput slots", List.of("&aThis slot is for your &9Unbind Scroll&a!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      eliteUnbindInputInfoButton = ItemStackSerializer.deserialize("unbindScrollInputInformationButton", this.fileConfiguration, this.file);
      outputInformationSlot = ConfigurationEngine.setInt(this.fileConfiguration, "informationOutputButtonSlot", 24);
      outputSlot = ConfigurationEngine.setInt(this.fileConfiguration, "outputSlot", 33);
      ItemStackSerializer.serialize(
         "cancelButton",
         ItemStackGenerator.generateItemStack(Material.BARRIER, "&4Cancel", List.of("&cCancel unbind!"), MetadataHandler.signatureID),
         this.fileConfiguration
      );
      ItemStackSerializer.serialize(
         "outputInformationButton",
         ItemStackGenerator.generateItemStack(
            Material.RED_BANNER, "&2Unbound Item Output slot", List.of("&aThis slot previews the result of your unbind!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(eliteItemInputInfoButton, CustomModelsConfig.boxOutput);
      cancelButton = ItemStackSerializer.deserialize("cancelButton", this.fileConfiguration, this.file);
      cancelSlot = ConfigurationEngine.setInt(this.fileConfiguration, "cancelButtonSlot", 27);
      ItemStackSerializer.serialize(
         "confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD, "&2Confirm!", List.of("&aUnbind item!"), 31175), this.fileConfiguration
      );
      confirmButton = ItemStackSerializer.deserialize("confirmButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.whiteAnvil);
      confirmSlot = ConfigurationEngine.setInt(this.fileConfiguration, "confirmUnbindSlot", 35);
   }

   @Generated
   public static String getShopName() {
      return shopName;
   }

   @Generated
   public static ItemStack getInfoButton() {
      return infoButton;
   }

   @Generated
   public static ItemStack getEliteItemInputInfoButton() {
      return eliteItemInputInfoButton;
   }

   @Generated
   public static ItemStack getEliteUnbindInputInfoButton() {
      return eliteUnbindInputInfoButton;
   }

   @Generated
   public static ItemStack getOutputInfoButton() {
      return outputInfoButton;
   }

   @Generated
   public static int getInfoSlot() {
      return infoSlot;
   }

   @Generated
   public static int getEliteItemInputInformationSlot() {
      return eliteItemInputInformationSlot;
   }

   @Generated
   public static int getEliteScrapInputInformationSlot() {
      return eliteScrapInputInformationSlot;
   }

   @Generated
   public static int getOutputInformationSlot() {
      return outputInformationSlot;
   }

   @Generated
   public static int getEliteItemInputSlot() {
      return eliteItemInputSlot;
   }

   @Generated
   public static int getEliteUnbindInputSlot() {
      return eliteUnbindInputSlot;
   }

   @Generated
   public static int getOutputSlot() {
      return outputSlot;
   }

   @Generated
   public static ItemStack getCancelButton() {
      return cancelButton;
   }

   @Generated
   public static int getCancelSlot() {
      return cancelSlot;
   }

   @Generated
   public static ItemStack getConfirmButton() {
      return confirmButton;
   }

   @Generated
   public static int getConfirmSlot() {
      return confirmSlot;
   }
}
