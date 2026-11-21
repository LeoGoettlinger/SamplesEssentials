package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class TheMinesTeleporter extends NPCsConfigFields {
   public TheMinesTeleporter() {
      super(
         "the_mines_teleporter",
         true,
         "<g:#5A5A5A:#6A6A6A>Prospector Voultar</g>",
         "<g:#4A4A4A:#5A5A5A><The Mines></g>",
         Profession.ARMORER,
         "em_adventurers_guild,308.4,78,204.4,90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_mines_dungeon.yml");
      this.setCustomModel("em_ag_prospectorvoultar");
      this.setSyncMovement(false);
   }
}
