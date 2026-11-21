package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class AirshipTeleporter extends NPCsConfigFields {
   public AirshipTeleporter() {
      super(
         "airship_teleporter",
         true,
         "<g:#6A9AB0:#7AAAC0>Sky Captain</g>",
         "<g:#5A8AA0:#6A9AB0><Airship></g>",
         Profession.ARMORER,
         "em_adventurers_guild,291.5,92,301.5,-90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp the_airship.yml");
      this.setCustomModel("em_ag_skycaptain");
      this.setSyncMovement(false);
   }
}
