package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolMode;
import java.util.List;
import org.bukkit.entity.Villager.Profession;

public final class GuildPatrolConfig extends NPCsConfigFields {
   public GuildPatrolConfig() {
      super(
         "guild_patrol",
         true,
         "<g:#A3C75B:#5F8A36>Gribble the Lookout</g>",
         "<g:#7A9A45:#4D6B2C><Goblin Kingdom></g>",
         Profession.ARMORER,
         "em_adventurers_guild,286.531,92,284.350,0,0",
         List.of(),
         List.of(),
         List.of(),
         false,
         3.0,
         NPCInteractions.NPCInteractionType.COMMAND
      );
      this.setCommand("em dungeontp goblin_kingdom.yml");
      this.setCustomModel("em_ag_gribble");
      this.setPatrolPauseNearPlayersRadius(3.0);
      this.setPatrolFaceNearbyPlayers(true);
      this.setSyncMovement(true);
   }

   @Override
   public void processConfigFields() {
      this.processBoolean("patrol.enabled", true, false, true);
      this.processString("patrol.mode", PatrolMode.LOOP.name(), null, true);
      this.processDouble("patrol.speed", 0.5, 0.5, true);
      this.processStringList("patrol.nodes", List.of("0,0,0", "0.126,0,12.284", "13.895,0,12.206", "14.014,0,0.002"), null, true);
      super.processConfigFields();
   }
}
