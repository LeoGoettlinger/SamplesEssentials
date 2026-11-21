package com.oheers.fish.fishing.items.config;

import com.oheers.fish.FishUtils;
import com.oheers.fish.fishing.items.Rarity;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;

public class RarityFileUpdates {
   private final Rarity rarity;
   private boolean updated = false;

   public RarityFileUpdates(@NonNull Rarity rarity) {
      this.rarity = rarity;
   }

   public void update() {
      YamlDocument config = this.rarity.getConfig();
      if (config.contains("colour")) {
         String colour = config.getString("colour");
         String format = FishUtils.getFormat(colour);
         config.set("format", format);
         config.remove("colour");
         this.updated = true;
      }

      if (config.contains("broadcast")) {
         Section broadcast = config.getSection("broadcast");
         if (broadcast == null) {
            config.move("broadcast", "broadcast.enabled");
            config.set("broadcast.only-rods", false);
            config.set("broadcast.range", -1);
            this.updated = true;
         }
      }

      this.updateRequirementFormats();
      if (this.updated) {
         this.rarity.save();
      }
   }

   private void updateRequirementFormats() {
      Section config = this.rarity.getConfig();
      this.updateRequirementFormats(config);
      Section fishSect = config.getSection("fish");
      if (fishSect != null) {
         fishSect.getRoutesAsStrings(false).forEach(fishName -> {
            Section section = fishSect.getSection(fishName);
            if (section != null) {
               this.updateRequirementFormats(section);
            }
         });
      }
   }

   private void updateRequirementFormats(@NonNull Section section) {
      Section ingameSection = section.getSection("requirement.ingame-time");
      if (ingameSection != null) {
         int min = ingameSection.getInt("minTime");
         int max = ingameSection.getInt("maxTime");
         ingameSection.remove("minTime");
         ingameSection.remove("maxTime");
         section.set("requirement.ingame-time", min + "-" + max);
         this.updated = true;
      }

      Section irlSection = section.getSection("requirement.irl-time");
      if (irlSection != null) {
         String min = irlSection.getString("minTime");
         String max = irlSection.getString("maxTime");
         irlSection.remove("minTime");
         irlSection.remove("maxTime");
         section.set("requirement.irl-time", min + "-" + max);
         this.updated = true;
      }
   }
}
