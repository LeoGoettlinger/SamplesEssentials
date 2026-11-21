package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackFireballLuaConfig extends LuaPowersConfigFields {
   public AttackFireballLuaConfig() {
      super("attack_fireball", Material.FIRE_CHARGE.toString(), PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "local function valid_player(player)\n  return player ~= nil and (player.game_mode == \"SURVIVAL\" or player.game_mode == \"ADVENTURE\")\nend\n\nlocal function clear_task(context)\n  if context.state.attack_fireball_task ~= nil then\n    context.scheduler:cancel_task(context.state.attack_fireball_task)\n    context.state.attack_fireball_task = nil\n  end\nend\n\nreturn {\n  api_version = 1,\n  on_boss_target_changed = function(context)\n    if context.state.attack_fireball_task ~= nil then\n      return\n    end\n    if not context.boss.is_monster then\n      return\n    end\n\n    local task_id\n    task_id = context.scheduler:run_every(160, function()\n      if not context.boss:is_alive() then\n        clear_task(context)\n        return\n      end\n\n      local target = context.boss:get_target_player()\n      if target == nil then\n        clear_task(context)\n        return\n      end\n\n      for _, player in ipairs(context.boss:get_nearby_players(20)) do\n        if valid_player(player) then\n          local boss_loc = context.boss:get_location()\n          local player_loc = player:get_location()\n          local direction = context.vectors.get_vector_between_locations(boss_loc, player_loc)\n          direction = context.vectors.normalize_vector(direction)\n          local origin = em.create_location(\n            boss_loc.x + direction.x, boss_loc.y + 1 + direction.y, boss_loc.z + direction.z,\n            boss_loc.world, boss_loc.yaw, boss_loc.pitch\n          )\n          context.boss:summon_projectile(\n            \"FIREBALL\",\n            origin,\n            player_loc,\n            0.01,\n            { yield = 3.0, incendiary = true, gravity = true, spawn_at_origin = true }\n          )\n        end\n      end\n    end)\n\n    context.state.attack_fireball_task = task_id\n  end\n}\n";
   }
}
