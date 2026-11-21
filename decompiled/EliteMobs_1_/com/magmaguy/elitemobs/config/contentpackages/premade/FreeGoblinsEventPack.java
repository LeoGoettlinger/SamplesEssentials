package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import java.util.List;

public class FreeGoblinsEventPack extends ContentPackagesConfigFields {
   public FreeGoblinsEventPack() {
      super("free_goblins_event_pack", true);
      this.setName("&2Free Goblins Events Pack");
      this.setCustomInfo(List.of("Unleash annoying little goblins all across your server!"));
      this.setCustomEventFilenames(
         List.of(
            "em_free_goblins_event_pack_bone_seeker_goblin_event.yml",
            "em_free_goblins_event_pack_doom_shroom_goblin_event.yml",
            "em_free_goblins_event_pack_gold_tooth_goblin_event.yml",
            "em_free_goblins_event_pack_good_citizen_goblin_event.yml",
            "em_free_goblins_event_pack_master_blacksmith_goblin_event.yml",
            "em_free_goblins_event_pack_meat_hunter_goblin_event.yml",
            "em_free_goblins_event_pack_treasure_hunter_goblin_event.yml",
            "em_free_goblins_event_pack_trophy_collector_goblin_event.yml",
            "em_free_goblins_event_pack_weapon_enchanter_goblin_event.yml",
            "em_free_goblins_event_pack_bling_covered_goblin_event.yml"
         )
      );
      this.setSetupMenuDescription(List.of("&2Unleash annoying little goblins", "&2all across your server!"));
      this.setDefaultDungeon(true);
      this.setNightbreakSlug("10-free-elitemobs-goblin-events");
   }
}
