package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class GetLootMenuConfig extends MenusConfigFields {
   public static String menuName;
   public static ItemStack infoItem;
   public static ItemStack leftArrowItem;
   public static ItemStack rightArrowItem;
   public static ItemStack previousLootItem;
   public static ItemStack nextLootItem;
   public static String tierTranslation;
   public static String itemFilterTranslation;

   public GetLootMenuConfig() {
      super("get_loot_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      menuName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "menuName", "[EM] Getloot menu", true);
      ItemStackSerializer.serialize(
         "infoButton",
         ItemStackGenerator.generateSkullItemStack("magmaguy", "&4&lEliteMobs &r&cby &4&lMagmaGuy", List.of("&8Support the plugins you enjoy!")),
         this.fileConfiguration
      );
      infoItem = ItemStackSerializer.deserialize("infoButton", this.fileConfiguration, this.file);
      ItemStackSerializer.serialize(
         "leftButton", ItemStackGenerator.generateSkullItemStack("MHF_ArrowLeft", "Previous Item Ranks", List.of("")), this.fileConfiguration
      );
      leftArrowItem = ItemStackSerializer.deserialize("leftButton", this.fileConfiguration, this.file);
      ItemStackSerializer.serialize(
         "rightButton", ItemStackGenerator.generateSkullItemStack("MHF_ArrowRight", "Next Item Ranks", List.of("")), this.fileConfiguration
      );
      rightArrowItem = ItemStackSerializer.deserialize("rightButton", this.fileConfiguration, this.file);
      tierTranslation = ConfigurationEngine.setString(this.file, this.fileConfiguration, "tierTranslation", "Level", true);
      itemFilterTranslation = ConfigurationEngine.setString(this.file, this.fileConfiguration, "itemFilterTranslation", "Filter by items of this level.", true);
      ItemStackSerializer.serialize(
         "previousLoot", ItemStackGenerator.generateSkullItemStack("MHF_ArrowLeft", "Previous Loot Page", List.of("")), this.fileConfiguration
      );
      previousLootItem = ItemStackSerializer.deserialize("previousLoot", this.fileConfiguration, this.file);
      ItemStackSerializer.serialize(
         "nextLoot", ItemStackGenerator.generateSkullItemStack("MHF_ArrowRight", "Next Loot Page", List.of("")), this.fileConfiguration
      );
      nextLootItem = ItemStackSerializer.deserialize("nextLoot", this.fileConfiguration, this.file);
   }
}
