package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class TheBridgeTeleporter extends NPCsConfigFields {
   public TheBridgeTeleporter() {
      super(
         "the_bridge_teleporter",
         true,
         "<g:#7A6A5A:#8A7A6A>Old Dwarf Jotun</g>",
         "<g:#6A5A4A:#7A6A5A><The Bridge></g>",
         Profession.ARMORER,
         "em_adventurers_guild,308.4,78,202.4,90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_bridge_sanctum.yml");
      this.setCustomModel("em_ag_olddwarfjotun");
      this.setSyncMovement(false);
   }
}
