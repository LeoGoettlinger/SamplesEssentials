package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;

public class SoundsConfig extends ConfigurationFile {
   public static String treasureChestOpenSound;
   public static String questProgressionSound;
   public static String questCompleteSound;
   public static String questAbandonSound;
   public static String questAcceptSound;

   public SoundsConfig() {
      super("Sounds.yml");
   }

   @Override
   public void initializeValues() {
      treasureChestOpenSound = ConfigurationEngine.setString(
         List.of("Sets the sound that will play when a treasure chest opens"),
         this.file,
         this.fileConfiguration,
         "treasureChestOpenSound",
         "elitemobs:treasure_chest.open",
         false
      );
      questProgressionSound = ConfigurationEngine.setString(
         List.of("Sets the sound that will play when a player progresses a quest"),
         this.file,
         this.fileConfiguration,
         "questProgressionSound",
         "elitemobs:quest.progression",
         false
      );
      questCompleteSound = ConfigurationEngine.setString(
         List.of("Sets the sound that will play when a player completes a quest"),
         this.file,
         this.fileConfiguration,
         "questCompleteSound",
         "elitemobs:quest.completion",
         false
      );
      questAbandonSound = ConfigurationEngine.setString(
         List.of("Sets the sound that will play when a player abandons a quest"),
         this.file,
         this.fileConfiguration,
         "questAbandonSound",
         "elitemobs:quest.abandon",
         false
      );
      questAcceptSound = ConfigurationEngine.setString(
         List.of("Sets the sound that will play when a player accepts a quest"),
         this.file,
         this.fileConfiguration,
         "questAcceptSound",
         "elitemobs:quest.accept",
         false
      );
   }
}
