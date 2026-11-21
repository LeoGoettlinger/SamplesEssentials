package com.oheers.fish.fishing.items.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.ConfigBase;
import java.io.File;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.jspecify.annotations.NonNull;

public class RarityConversions {
   public void performCheck() {
      File raritiesFile = new File(EvenMoreFish.getInstance().getDataFolder(), "rarities.yml");
      if (raritiesFile.exists() && raritiesFile.isFile()) {
         EvenMoreFish.getInstance().getLogger().info("Performing automatic conversion of rarity configs.");
         File raritiesDir = this.getRaritiesDirectory();
         if (!raritiesDir.exists()) {
            raritiesDir.mkdirs();
         }

         ConfigBase config = new ConfigBase(raritiesFile, EvenMoreFish.getInstance(), false);
         Section raritiesSection = config.getConfig().getSection("rarities");
         if (raritiesSection == null) {
            this.finalizeConversion(config);
         } else {
            for (String rarityKey : raritiesSection.getRoutesAsStrings(false)) {
               Section section = raritiesSection.getSection(rarityKey);
               if (section != null) {
                  this.convertSectionToFile(section);
               }
            }

            this.finalizeConversion(config);
         }
      }
   }

   private void finalizeConversion(@NonNull ConfigBase raritiesConfig) {
      File file = raritiesConfig.getFile();
      file.renameTo(new File(EvenMoreFish.getInstance().getDataFolder(), "rarities.yml.old"));
      file.delete();
      Logging.info(ComponentMessage.componentMessage("<yellow>Your rarity configs have been automatically converted to the new format.").get());
   }

   public File getRaritiesDirectory() {
      return new File(EvenMoreFish.getInstance().getDataFolder(), "rarities");
   }

   private void convertSectionToFile(@NonNull Section section) {
      String id = section.getNameAsString();
      if (id != null) {
         File file = new File(EvenMoreFish.getInstance().getDataFolder(), "rarities/" + id + ".yml");
         ConfigBase configBase = new ConfigBase(file, EvenMoreFish.getInstance(), false);
         YamlDocument config = configBase.getConfig();
         config.setAll(section.getRouteMappedValues(true));
         config.set("id", id);
         config.set("disabled", false);
         configBase.save();
      }
   }
}
