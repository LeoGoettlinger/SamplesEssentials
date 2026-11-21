package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.api.EliteMobDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.api.ElitePhaseSwitchEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.ScriptZoneEnterEvent;
import com.magmaguy.elitemobs.api.ScriptZoneLeaveEvent;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Generated;

public class ScriptEventsBlueprint {
   private final Set<Class> events = new HashSet<>();
   private boolean zoneListener = false;

   public ScriptEventsBlueprint(List<String> values, String scriptName, String filename) {
      for (String entry : values) {
         switch (entry) {
            case "EliteMobDamagedByEliteMobEvent":
               this.events.add(EliteMobDamagedByEliteMobEvent.class);
               break;
            case "EliteMobDamagedByPlayerEvent":
               this.events.add(EliteMobDamagedByPlayerEvent.class);
               break;
            case "EliteMobDamagedEvent":
               this.events.add(EliteMobDamagedEvent.class);
               break;
            case "EliteMobDeathEvent":
               this.events.add(EliteMobDeathEvent.class);
               break;
            case "EliteMobEnterCombatEvent":
               this.events.add(EliteMobEnterCombatEvent.class);
               break;
            case "EliteMobExitCombatEvent":
               this.events.add(EliteMobExitCombatEvent.class);
               break;
            case "EliteMobHealEvent":
               this.events.add(EliteMobHealEvent.class);
               break;
            case "EliteMobSpawnEvent":
               this.events.add(EliteMobSpawnEvent.class);
               break;
            case "EliteMobTargetPlayerEvent":
               this.events.add(EliteMobTargetPlayerEvent.class);
               break;
            case "PlayerDamagedByEliteMobEvent":
               this.events.add(PlayerDamagedByEliteMobEvent.class);
               break;
            case "ElitePhaseSwitchEvent":
               this.events.add(ElitePhaseSwitchEvent.class);
               break;
            case "ZoneEnterEvent":
               this.events.add(ScriptZoneEnterEvent.class);
               this.zoneListener = true;
               break;
            case "ZoneLeaveEvent":
               this.events.add(ScriptZoneLeaveEvent.class);
               this.zoneListener = true;
               break;
            default:
               Logger.warn("Failed to get valid script event from entry " + entry + " in " + scriptName + " for file " + filename + " !");
         }
      }
   }

   public ScriptEventsBlueprint(Map<String, Object> configurationValues, String scriptName, String filename) {
      this(MapListInterpreter.parseStringList("Events", configurationValues.get("Events"), scriptName), scriptName, filename);
   }

   @Generated
   public Set<Class> getEvents() {
      return this.events;
   }

   @Generated
   public boolean isZoneListener() {
      return this.zoneListener;
   }
}
