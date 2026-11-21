package com.oheers.fish.fishing.items.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.ConfigBase;
import java.io.File;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.jspecify.annotations.NonNull;

public class FishConversions extends RarityConversions {
   @Override
   public void performCheck() {
      File fishFile = new File(EvenMoreFish.getInstance().getDataFolder(), "fish.yml");
      if (fishFile.exists() && fishFile.isFile()) {
         EvenMoreFish.getInstance().getLogger().info("Performing automatic conversion of fish configs.");
         File raritiesDir = this.getRaritiesDirectory();
         if (raritiesDir.exists()) {
            ConfigBase config = new ConfigBase(fishFile, EvenMoreFish.getInstance(), false);
            Section fishSection = config.getConfig().getSection("fish");
            if (fishSection == null) {
               this.finalizeConversion(config);
            } else {
               for (String rarityKey : fishSection.getRoutesAsStrings(false)) {
                  Section section = fishSection.getSection(rarityKey);
                  if (section != null) {
                     this.convertSectionToFile(section);
                  }
               }

               this.finalizeConversion(config);
            }
         }
      }
   }

   private void finalizeConversion(@NonNull ConfigBase fishConfig) {
      File file = fishConfig.getFile();
      file.renameTo(new File(EvenMoreFish.getInstance().getDataFolder(), "fish.yml.old"));
      file.delete();
      Logging.info(
         ComponentMessage.componentMessage("<yellow>Your fish configs have been automatically converted to the new format.").get(),
         ComponentMessage.componentMessage("<yellow>They are now located in the rarity files.").get()
      );
   }

   private void convertSectionToFile(@NonNull Section section) {
      String id = section.getNameAsString();
      if (id != null) {
         File rarityFile = new File(EvenMoreFish.getInstance().getDataFolder(), "rarities/" + id + ".yml");
         if (rarityFile.exists()) {
            ConfigBase configBase = new ConfigBase(rarityFile, EvenMoreFish.getInstance(), false);
            Section fishSection = configBase.getConfig().createSection("fish");
            fishSection.setAll(section.getRouteMappedValues(true));
            configBase.save();
         }
      }
   }
}
