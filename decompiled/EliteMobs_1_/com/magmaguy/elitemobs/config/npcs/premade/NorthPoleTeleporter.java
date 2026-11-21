package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class NorthPoleTeleporter extends NPCsConfigFields {
   public NorthPoleTeleporter() {
      super(
         "north_pole_teleporter",
         true,
         "<g:#8AB8C8:#A0D0E0>Frosty Guide</g>",
         "<g:#6A98A8:#8AB8C8><North Pole></g>",
         Profession.ARMORER,
         "em_adventurers_guild,289.5,92,277.5,180,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp north_pole_minidungeon.yml");
      this.setCustomModel("em_ag_frostyguide");
      this.setSyncMovement(false);
   }
}
