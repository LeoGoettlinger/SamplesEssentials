package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class TheCityTeleporter extends NPCsConfigFields {
   public TheCityTeleporter() {
      super(
         "the_city_teleporter",
         true,
         "<g:#7A5A4A:#8A6A5A>Dwarf Olav</g>",
         "<g:#6A4A3A:#7A5A4A><The City></g>",
         Profession.ARMORER,
         "em_adventurers_guild,307.4,78,201.4,0,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_city_dungeon.yml");
      this.setCustomModel("em_ag_dwarfolav");
      this.setSyncMovement(false);
   }
}
