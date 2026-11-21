package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class BeastsSanctuaryTeleporter extends NPCsConfigFields {
   public BeastsSanctuaryTeleporter() {
      super(
         "beasts_sanctuary_teleporter",
         true,
         "<g:#7A5A40:#8A6A50>Beast Warden</g>",
         "<g:#6A4A30:#7A5A40><Beast's Sanctuary></g>",
         Profession.ARMORER,
         "em_adventurers_guild,291.5,92,304.5,-90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp beasts_sanctuary_lair.yml");
      this.setCustomModel("em_ag_beastwarden");
      this.setSyncMovement(false);
   }
}
