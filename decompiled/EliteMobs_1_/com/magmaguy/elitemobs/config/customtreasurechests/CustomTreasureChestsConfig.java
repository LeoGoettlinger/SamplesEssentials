package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.config.CustomConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import java.util.HashMap;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CustomTreasureChestsConfig extends CustomConfig {
   private static HashMap<String, CustomTreasureChestConfigFields> customTreasureChestConfigFields = new HashMap<>();

   public CustomTreasureChestsConfig() {
      super("customtreasurechests", "com.magmaguy.elitemobs.config.customtreasurechests.premade", CustomTreasureChestConfigFields.class);
      customTreasureChestConfigFields = new HashMap<>();

      for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
         if (super.getCustomConfigFieldsHashMap().get(key).isEnabled()) {
            customTreasureChestConfigFields.put(key, (CustomTreasureChestConfigFields)super.getCustomConfigFieldsHashMap().get(key));
         }
      }
   }

   public static void addTreasureChestEntry(Player player, String customChestFileName) {
      CustomTreasureChestConfigFields customTreasureChestConfigFields = getCustomTreasureChestConfigFields().get(customChestFileName);
      if (customTreasureChestConfigFields == null) {
         player.sendMessage(CommandMessagesConfig.getInvalidTreasureChestFilename());
      } else {
         addTreasureChestEntry(
            new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()), customChestFileName
         );
      }
   }

   public static TreasureChest addTreasureChestEntry(Location location, String customChestFileName) {
      CustomTreasureChestConfigFields customTreasureChestConfigFields = getCustomTreasureChestConfigFields().get(customChestFileName);
      if (location == null) {
         Logger.warn("Failed to commit a location for a treasure chest!");
         return null;
      } else {
         return customTreasureChestConfigFields.addTreasureChest(location, 0L);
      }
   }

   public static boolean removeTreasureChestEntry(Location location, String customChestFileName) {
      CustomTreasureChestConfigFields customTreasureChestConfigFields = getCustomTreasureChestConfigFields().get(customChestFileName);
      if (location != null && customTreasureChestConfigFields != null) {
         return customTreasureChestConfigFields.removeLocation(location);
      } else {
         Logger.warn("Failed to remove a location for a treasure chest!");
         return false;
      }
   }

   @Generated
   public static HashMap<String, CustomTreasureChestConfigFields> getCustomTreasureChestConfigFields() {
      return customTreasureChestConfigFields;
   }
}
