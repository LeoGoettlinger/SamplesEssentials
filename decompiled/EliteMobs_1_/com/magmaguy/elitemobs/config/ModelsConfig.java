package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;

public class ModelsConfig extends ConfigurationFile {
   public static String treasureChest;

   public ModelsConfig() {
      super("Models.yml");
   }

   @Override
   public void initializeValues() {
      treasureChest = ConfigurationEngine.setString(
         List.of("Name of the custom model for the treasure chest"), this.file, this.fileConfiguration, "treasureChest", "elitemobs_treasure_chest", false
      );
   }
}
