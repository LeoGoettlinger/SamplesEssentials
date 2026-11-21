package com.magmaguy.elitemobs.config.contentpackages;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.magmacore.config.CustomConfig;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;

public class ContentPackagesConfig extends CustomConfig {
   private static Map<String, ContentPackagesConfigFields> dungeonPackages = new HashMap<>();
   private static Map<String, ContentPackagesConfigFields> enchantedChallengeDungeonPackages = new HashMap<>();

   public ContentPackagesConfig() {
      super("content_packages", "com.magmaguy.elitemobs.config.contentpackages.premade", ContentPackagesConfigFields.class);
      dungeonPackages = new HashMap<>();
      enchantedChallengeDungeonPackages = new HashMap<>();

      for (String key : super.getCustomConfigFieldsHashMap().keySet()) {
         if (!((ContentPackagesConfigFields)super.getCustomConfigFieldsHashMap().get(key)).isEnchantmentChallenge()) {
            dungeonPackages.put(key, (ContentPackagesConfigFields)super.getCustomConfigFieldsHashMap().get(key));
         } else {
            enchantedChallengeDungeonPackages.put(key, (ContentPackagesConfigFields)super.getCustomConfigFieldsHashMap().get(key));
         }
      }

      File worldsBluePrint = new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "world_blueprints");
      if (!worldsBluePrint.exists()) {
         worldsBluePrint.mkdir();
      }
   }

   public static void initializePackages() {
      for (ContentPackagesConfigFields fields : dungeonPackages.values()) {
         EMPackage.initialize(fields);
      }

      for (ContentPackagesConfigFields fields : enchantedChallengeDungeonPackages.values()) {
         EMPackage.initialize(fields);
      }
   }

   @Generated
   public static Map<String, ContentPackagesConfigFields> getDungeonPackages() {
      return dungeonPackages;
   }

   @Generated
   public static Map<String, ContentPackagesConfigFields> getEnchantedChallengeDungeonPackages() {
      return enchantedChallengeDungeonPackages;
   }
}
