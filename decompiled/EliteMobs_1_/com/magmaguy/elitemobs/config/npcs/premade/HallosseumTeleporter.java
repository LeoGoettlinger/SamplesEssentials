package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class HallosseumTeleporter extends NPCsConfigFields {
   public HallosseumTeleporter() {
      super(
         "hallosseum_teleporter",
         true,
         "<g:#9A5A30:#AA6A40>Haunted Harbinger</g>",
         "<g:#8A4A20:#9A5A30><Hallosseum></g>",
         Profession.ARMORER,
         "em_adventurers_guild,289.5,92,271.5,0,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp hallosseum_lair.yml");
      this.setCustomModel("em_ag_hauntedharbinger");
      this.setSyncMovement(false);
   }
}
