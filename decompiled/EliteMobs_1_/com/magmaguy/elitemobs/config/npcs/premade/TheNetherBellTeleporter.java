package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class TheNetherBellTeleporter extends NPCsConfigFields {
   public TheNetherBellTeleporter() {
      super(
         "the_nether_bell_teleporter",
         true,
         "<g:#7A3A3A:#8A4A4A>Ancient Corpse</g>",
         "<g:#6A2A2A:#7A3A3A><The Nether Bell></g>",
         Profession.ARMORER,
         "em_adventurers_guild,300.4,78,198.3,180,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_nether_bell_sanctum.yml");
      this.setCustomModel("em_ag_ancientcorpse");
      this.setSyncMovement(false);
   }
}
