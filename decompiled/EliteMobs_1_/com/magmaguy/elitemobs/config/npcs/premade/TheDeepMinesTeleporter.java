package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class TheDeepMinesTeleporter extends NPCsConfigFields {
   public TheDeepMinesTeleporter() {
      super(
         "the_deep_mines_teleporter",
         true,
         "<g:#8A4A3A:#9A5A4A>Nether Lurk</g>",
         "<g:#7A3A2A:#8A4A3A><The Deep Mines></g>",
         Profession.ARMORER,
         "em_adventurers_guild,301.4,78,201.3,0,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_deep_mines_dungeon.yml");
      this.setCustomModel("em_ag_netherlurk");
      this.setSyncMovement(false);
   }
}
