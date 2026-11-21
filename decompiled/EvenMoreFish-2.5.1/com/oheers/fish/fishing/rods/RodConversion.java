package com.oheers.fish.fishing.rods;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import java.io.File;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;

public class RodConversion {
   public void performCheck() {
      Section rodSection = MainConfig.getInstance().getConfig().getSection("nbt-rod-item");
      if (rodSection != null) {
         Logging.info("Performing automatic conversion of nbt-rod configs.");
         this.convertSectionToFile(rodSection);
         this.finalizeConversion();
      }
   }

   private void finalizeConversion() {
      MainConfig.getInstance().getConfig().remove("nbt-rod-item");
      MainConfig.getInstance().getConfig().remove("require-nbt-rod");
      MainConfig.getInstance().save();
      Logging.info(ComponentMessage.componentMessage("<yellow>Your nbt-rod config has been automatically converted to the new format.").get());
   }

   private File getRodsDirectory() {
      return new File(EvenMoreFish.getInstance().getDataFolder(), "rods");
   }

   private void convertSectionToFile(Section section) {
      File rodsDir = this.getRodsDirectory();
      if (!rodsDir.exists()) {
         rodsDir.mkdirs();
      }

      File file = new File(rodsDir, "default.yml");
      ConfigBase configBase = new ConfigBase(file, EvenMoreFish.getInstance(), false);
      YamlDocument config = configBase.getConfig();
      config.set("id", "default");
      config.set("allowed-rarities", "ALL");
      config.set("item", section.get("item"));
      configBase.save();
   }
}
