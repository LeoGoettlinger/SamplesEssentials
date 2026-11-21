package com.oheers.fish.baits.configs;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import java.io.File;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.jspecify.annotations.NonNull;

public class BaitConversions {
   public void performCheck() {
      File baitFile = new File(EvenMoreFish.getInstance().getDataFolder(), "baits.yml");
      if (baitFile.exists() && baitFile.isFile()) {
         EvenMoreFish.getInstance().getLogger().info("Performing automatic conversion of bait configs");
         File baitsDir = this.getBaitsDirectory();
         if (!baitsDir.exists()) {
            baitsDir.mkdirs();
         }

         ConfigBase config = new ConfigBase(baitFile, EvenMoreFish.getInstance(), false);
         this.handleConfigYml(config);
         this.handleMessagesYml(config);
         Section baitSection = config.getConfig().getSection("baits");
         if (baitSection == null) {
            this.finalizeConversion(config);
         } else {
            for (String baitKey : baitSection.getRoutesAsStrings(false)) {
               Section section = baitSection.getSection(baitKey);
               if (section != null) {
                  this.convertSectionToFile(section);
               }
            }

            this.finalizeConversion(config);
         }
      }
   }

   private void finalizeConversion(@NonNull ConfigBase baitConfig) {
      File file = baitConfig.getFile();
      file.renameTo(new File(EvenMoreFish.getInstance().getDataFolder(), "baits.yml.old"));
      file.delete();
      Logging.info(ComponentMessage.componentMessage("<yellow>Your bait configs have been automatically converted to the new format.").get());
   }

   public File getBaitsDirectory() {
      return new File(EvenMoreFish.getInstance().getDataFolder(), "baits");
   }

   private void convertSectionToFile(@NonNull Section section) {
      String id = section.getNameAsString();
      if (id != null) {
         File file = new File(EvenMoreFish.getInstance().getDataFolder(), "baits/" + id + ".yml");
         ConfigBase configBase = new ConfigBase(file, EvenMoreFish.getInstance(), false);
         YamlDocument config = configBase.getConfig();
         config.setAll(section.getRouteMappedValues(true));
         config.set("id", id);
         config.set("disabled", false);
         configBase.save();
      }
   }

   private void handleMessagesYml(@NonNull ConfigBase config) {
      YamlDocument messages = MessageConfig.getInstance().getConfig();
      YamlDocument baitConfig = config.getConfig();
      Section baitSection = baitConfig.getSection("format");
      if (baitSection != null) {
         Section messagesSection = messages.getSection("bait");
         if (messagesSection == null) {
            messagesSection = messages.createSection("bait");
         }

         messagesSection.setAll(baitSection.getRouteMappedValues(true));
         MessageConfig.getInstance().save();
      }
   }

   private void handleConfigYml(@NonNull ConfigBase config) {
      YamlDocument mainConfig = MainConfig.getInstance().getConfig();
      YamlDocument baitConfig = config.getConfig();
      Section baitSection = baitConfig.getSection("general");
      if (baitSection != null) {
         Section mainSection = mainConfig.getSection("bait");
         if (mainSection == null) {
            mainSection = mainConfig.createSection("bait");
         }

         mainSection.setAll(baitSection.getRouteMappedValues(true));
         MainConfig.getInstance().save();
      }
   }
}
