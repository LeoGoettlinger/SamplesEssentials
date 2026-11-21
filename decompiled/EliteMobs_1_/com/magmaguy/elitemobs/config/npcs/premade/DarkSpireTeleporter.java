package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class DarkSpireTeleporter extends NPCsConfigFields {
   public DarkSpireTeleporter() {
      super(
         "dark_spire_teleporter",
         true,
         "<g:#3A3A5A:#4A4A6A>Dark Warlock</g>",
         "<g:#2A2A4A:#3A3A5A><Dark Spire></g>",
         Profession.ARMORER,
         "em_adventurers_guild,293.5,92,299.5,0,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_dark_spire.yml");
      this.setCustomModel("em_ag_darkwarlock");
      this.setSyncMovement(false);
   }
}
