package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class RuinsTeleporter extends NPCsConfigFields {
   public RuinsTeleporter() {
      super(
         "ruins_teleporter",
         true,
         "<g:#9A8A7A:#AA9A8A>Ruinwarden</g>",
         "<g:#8A7A6A:#9A8A7A><The Ruins></g>",
         Profession.ARMORER,
         "em_adventurers_guild,302.7,92,292.5,-90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_ruins.yml");
      this.setCustomModel("em_ag_ruinwarden");
      this.setSyncMovement(false);
   }
}
