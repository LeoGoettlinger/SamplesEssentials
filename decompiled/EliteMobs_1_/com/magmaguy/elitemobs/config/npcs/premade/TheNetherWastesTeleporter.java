package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class TheNetherWastesTeleporter extends NPCsConfigFields {
   public TheNetherWastesTeleporter() {
      super(
         "the_nether_wastes_teleporter",
         true,
         "<g:#9A5A3A:#AA6A4A>Nether Shroom</g>",
         "<g:#8A4A2A:#9A5A3A><The Nether Wastes></g>",
         Profession.ARMORER,
         "em_adventurers_guild,299.5,78,201.5,0,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_nether_wastes_dungeon.yml");
      this.setCustomModel("em_ag_nethershroom");
      this.setSyncMovement(false);
   }
}
