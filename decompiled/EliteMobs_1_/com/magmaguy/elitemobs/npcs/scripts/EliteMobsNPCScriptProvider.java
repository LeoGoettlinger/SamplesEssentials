package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptProvider;
import java.nio.file.Path;

public class EliteMobsNPCScriptProvider implements ScriptProvider {
   private final Path scriptDirectory;

   public EliteMobsNPCScriptProvider(Path scriptDirectory) {
      this.scriptDirectory = scriptDirectory;
   }

   @Override
   public String getNamespace() {
      return "elitemobs_npc";
   }

   @Override
   public Path getScriptDirectory() {
      return this.scriptDirectory;
   }

   @Override
   public ScriptHook resolveHook(String key) {
      return switch (key) {
         case "on_spawn" -> ScriptHook.ON_SPAWN;
         case "on_game_tick" -> ScriptHook.ON_TICK;
         case "on_zone_enter" -> ScriptHook.ON_ZONE_ENTER;
         case "on_zone_leave" -> ScriptHook.ON_ZONE_LEAVE;
         case "on_remove" -> ScriptableNPC.ON_REMOVE;
         case "on_npc_interact" -> ScriptableNPC.ON_INTERACT;
         case "on_npc_proximity_enter" -> ScriptableNPC.ON_PROXIMITY_ENTER;
         case "on_npc_proximity_leave" -> ScriptableNPC.ON_PROXIMITY_LEAVE;
         default -> null;
      };
   }
}
