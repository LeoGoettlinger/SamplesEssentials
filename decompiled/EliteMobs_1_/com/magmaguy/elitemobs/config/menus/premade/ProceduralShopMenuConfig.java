package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class ProceduralShopMenuConfig extends MenusConfigFields {
   public static String shopName;
   public static ItemStack rerollItem;
   public static int rerollSlot;
   public static List<Integer> storeSlots;
   public static String messageFullInventory;

   public ProceduralShopMenuConfig() {
      super("procedural_shop_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      shopName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "shopName", "[EM] Procedural Item Shop", true);
      ItemStackSerializer.serialize(
         "rerollButton",
         ItemStackGenerator.generateSkullItemStack("magmaguy", "&4&lEliteMobs &r&cby &4&lMagmaGuy", List.of("&8Support the plugins you enjoy!")),
         this.fileConfiguration
      );
      rerollItem = ItemStackSerializer.deserialize("rerollButton", this.fileConfiguration, this.file);
      rerollSlot = ConfigurationEngine.setInt(this.fileConfiguration, "rerollButtonSlot", 4);
      storeSlots = ConfigurationEngine.setList(
         this.file,
         this.fileConfiguration,
         "storeItemSlots",
         new ArrayList<>(
            List.of(
               9,
               10,
               11,
               12,
               13,
               14,
               15,
               16,
               17,
               18,
               19,
               20,
               21,
               22,
               23,
               24,
               25,
               26,
               27,
               28,
               29,
               30,
               31,
               32,
               33,
               34,
               35,
               36,
               37,
               38,
               39,
               40,
               41,
               42,
               43,
               44,
               45,
               46,
               47,
               48,
               49,
               50,
               51,
               52,
               53
            )
         ),
         false
      );
      messageFullInventory = ConfigurationEngine.setString(
         this.file,
         this.fileConfiguration,
         "fullInventoryMessage",
         "&7[EliteMobs] &4Your inventory is full! You can't buy items until you get some free space.",
         true
      );
   }
}
