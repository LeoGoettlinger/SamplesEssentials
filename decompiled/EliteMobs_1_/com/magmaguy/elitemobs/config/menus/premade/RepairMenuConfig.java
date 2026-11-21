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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RepairMenuConfig extends MenusConfigFields {
   public static String shopName;
   public static ItemStack infoButton;
   public static ItemStack eliteItemInputInfoButton;
   public static ItemStack eliteScrapInputInfoButton;
   public static ItemStack outputInfoButton;
   public static int infoSlot;
   public static int eliteItemInputInformationSlot;
   public static int eliteScrapInputInformationSlot;
   public static int outputInformationSlot;
   public static int eliteItemInputSlot;
   public static int eliteScrapInputSlot;
   public static int outputSlot;
   public static ItemStack cancelButton;
   public static int cancelSlot;
   public static ItemStack confirmButton;
   public static int confirmSlot;

   public RepairMenuConfig() {
      super("repair_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      shopName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "shopName", "[EM] Repair menu!", true);
      ItemStackSerializer.serialize(
         "infoButton",
         ItemStackGenerator.generateSkullItemStack(
            "magmaguy", "&4&lEliteMobs &r&cby &4&lMagmaGuy", new ArrayList<>(List.of("&8Support the plugins you enjoy!", "&aUse scrap to repair elite items!"))
         ),
         this.fileConfiguration
      );
      infoButton = ItemStackSerializer.deserialize("infoButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(infoButton, CustomModelsConfig.goldenQuestionMark);
      infoSlot = ConfigurationEngine.setInt(this.fileConfiguration, "infoButtonSlot", 4);
      eliteItemInputInformationSlot = ConfigurationEngine.setInt(this.fileConfiguration, "eliteItemInputInformationSlot", 20);
      eliteItemInputSlot = ConfigurationEngine.setInt(this.fileConfiguration, "eliteItemInputSlot", 29);
      ItemStackSerializer.serialize(
         "eliteItemInputInformationButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER, "&2 Elite Item Input slots", List.of("&aThis slot is for your elite item!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      eliteItemInputInfoButton = ItemStackSerializer.deserialize("eliteItemInputInformationButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(eliteItemInputInfoButton, CustomModelsConfig.boxInput);
      eliteScrapInputInformationSlot = ConfigurationEngine.setInt(this.fileConfiguration, "eliteScrapInputInformationSlot", 22);
      eliteScrapInputSlot = ConfigurationEngine.setInt(this.fileConfiguration, "eliteScrapInputSlot", 31);
      ItemStackSerializer.serialize(
         "eliteScrapInputInformationButton",
         ItemStackGenerator.generateItemStack(
            Material.GREEN_BANNER, "&2Elite Scrap Input slots", List.of("&aThis slot is for your elite scrap!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      eliteScrapInputInfoButton = ItemStackSerializer.deserialize("eliteScrapInputInformationButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(eliteScrapInputInfoButton, CustomModelsConfig.boxInput);
      outputInformationSlot = ConfigurationEngine.setInt(this.fileConfiguration, "informationOutputButtonSlot", 24);
      outputSlot = ConfigurationEngine.setInt(this.fileConfiguration, "outputSlot", 33);
      ItemStackSerializer.serialize(
         "cancelButton",
         ItemStackGenerator.generateItemStack(Material.BARRIER, "&4Cancel", List.of("&cCancel repair!"), MetadataHandler.signatureID),
         this.fileConfiguration
      );
      ItemStackSerializer.serialize(
         "outputInformationButton",
         ItemStackGenerator.generateItemStack(
            Material.RED_BANNER, "&2Output slots", List.of("&aThis slot previews the result of your repair!"), MetadataHandler.signatureID
         ),
         this.fileConfiguration
      );
      outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(outputInfoButton, CustomModelsConfig.boxOutput);
      cancelButton = ItemStackSerializer.deserialize("cancelButton", this.fileConfiguration, this.file);
      cancelSlot = ConfigurationEngine.setInt(this.fileConfiguration, "cancelButtonSlot", 27);
      ItemStackSerializer.serialize(
         "confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD, "&2Confirm!", List.of("&aRepair item!")), this.fileConfiguration
      );
      confirmButton = ItemStackSerializer.deserialize("confirmButton", this.fileConfiguration, this.file);
      CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.anvilHammer);
      confirmSlot = ConfigurationEngine.setInt(this.fileConfiguration, "confirmRepairSlot", 35);
   }
}
