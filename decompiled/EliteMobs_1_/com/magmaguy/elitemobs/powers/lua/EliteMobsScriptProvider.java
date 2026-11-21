package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptProvider;
import java.nio.file.Path;

public class EliteMobsScriptProvider implements ScriptProvider {
   private final Path scriptDirectory;

   public EliteMobsScriptProvider(Path scriptDirectory) {
      this.scriptDirectory = scriptDirectory;
   }

   @Override
   public String getNamespace() {
      return "elitemobs";
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
         case "on_boss_damaged" -> ScriptableBoss.ON_DAMAGED;
         case "on_boss_damaged_by_player" -> ScriptableBoss.ON_DAMAGED_BY_PLAYER;
         case "on_reinforcement_damaged_by_player" -> ScriptableBoss.ON_REINFORCEMENT_DAMAGED_BY_PLAYER;
         case "on_boss_damaged_by_elite" -> ScriptableBoss.ON_DAMAGED_BY_ELITE;
         case "on_player_damaged_by_boss" -> ScriptableBoss.ON_PLAYER_DAMAGED;
         case "on_enter_combat" -> ScriptableBoss.ON_ENTER_COMBAT;
         case "on_exit_combat" -> ScriptableBoss.ON_EXIT_COMBAT;
         case "on_heal" -> ScriptableBoss.ON_HEAL;
         case "on_boss_target_changed" -> ScriptableBoss.ON_TARGET;
         case "on_death" -> ScriptableBoss.ON_DEATH;
         case "on_phase_switch" -> ScriptableBoss.ON_PHASE_SWITCH;
         case "on_mind_action" -> ScriptableBoss.ON_MIND_ACTION;
         default -> null;
      };
   }
}
