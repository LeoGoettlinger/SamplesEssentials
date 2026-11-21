package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class SteamworksTeleporter extends NPCsConfigFields {
   public SteamworksTeleporter() {
      super(
         "steamworks_teleporter",
         true,
         "<g:#8A6A4A:#9A7A5A>Steam Engineer</g>",
         "<g:#7A5A3A:#8A6A4A><Steamworks></g>",
         Profession.TOOLSMITH,
         "em_adventurers_guild,297.5,92,267.5,-180,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp steamworks_lair.yml");
      this.setCustomModel("em_ag_steamengineer");
      this.setSyncMovement(false);
   }
}
