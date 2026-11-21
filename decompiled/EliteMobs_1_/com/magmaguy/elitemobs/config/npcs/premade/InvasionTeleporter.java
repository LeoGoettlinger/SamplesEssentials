package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public class InvasionTeleporter extends NPCsConfigFields {
   public InvasionTeleporter() {
      super(
         "invasion_teleporter",
         true,
         "<g:#2E8B57:#3CB371>Invasion Scout</g>",
         "<g:#228B22:#6B8E23><Invasion></g>",
         Profession.ARMORER,
         "em_adventurers_guild,295.5,92,304.5,90,0",
         List.of(""),
         List.of(),
         List.of(),
         true,
         1.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp invasion_minidungeon.yml");
      this.setCustomModel("em_ag_invasionscout");
      this.setSyncMovement(false);
   }
}
