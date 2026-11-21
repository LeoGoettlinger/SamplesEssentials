package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class PirateShipTeleporter extends NPCsConfigFields {
   public PirateShipTeleporter() {
      super(
         "pirate_ship_teleporter",
         true,
         "<g:#4A7A9A:#5A8AAA>Sea Dog</g>",
         "<g:#3A6A8A:#4A7A9A><Pirate Ship></g>",
         Profession.FISHERMAN,
         "em_adventurers_guild,302.7,92,288.5,-90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp pirate_ship_minidungeon.yml");
      this.setCustomModel("em_ag_seadog");
      this.setSyncMovement(false);
   }
}
