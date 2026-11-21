package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class TheClimbTeleporter extends NPCsConfigFields {
   public TheClimbTeleporter() {
      super(
         "the_climb_teleporter",
         true,
         "<g:#6A6A6A:#7A7A7A>Miner Regulus</g>",
         "<g:#5A5A5A:#6A6A6A><The Climb></g>",
         Profession.ARMORER,
         "em_adventurers_guild,308.4,78,208.4,90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_climb_dungeon.yml");
      this.setCustomModel("em_ag_minerregulus");
      this.setSyncMovement(false);
      this.setInstanced(false);
   }
}
