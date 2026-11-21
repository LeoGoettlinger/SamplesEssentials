package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class ColosseumTeleporter extends NPCsConfigFields {
   public ColosseumTeleporter() {
      super(
         "colosseum_teleporter",
         true,
         "<g:#A89040:#B8A050>Arena Master</g>",
         "<g:#988030:#A89040><Colosseum></g>",
         Profession.ARMORER,
         "em_adventurers_guild,291.5,92,307.5,-90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_colosseum_lair.yml");
      this.setCustomModel("em_ag_arenamaster");
      this.setSyncMovement(false);
   }
}
