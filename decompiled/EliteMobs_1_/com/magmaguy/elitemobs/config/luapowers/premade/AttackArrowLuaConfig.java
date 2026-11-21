package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackArrowLuaConfig extends LuaPowersConfigFields {
   public AttackArrowLuaConfig() {
      super("attack_arrow", Material.DISPENSER.toString(), PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "local function valid_player(player)\n  return player ~= nil and (player.game_mode == \"SURVIVAL\" or player.game_mode == \"ADVENTURE\")\nend\n\nlocal function clear_task(context)\n  if context.state.attack_arrow_task ~= nil then\n    context.scheduler:cancel_task(context.state.attack_arrow_task)\n    context.state.attack_arrow_task = nil\n  end\nend\n\nreturn {\n  api_version = 1,\n  on_boss_target_changed = function(context)\n    if context.state.attack_arrow_task ~= nil then\n      return\n    end\n\n    local task_id\n    task_id = context.scheduler:run_every(160, function()\n      local target = context.boss:get_target_player()\n      if target == nil then\n        clear_task(context)\n        return\n      end\n\n      for _, player in ipairs(context.boss:get_nearby_players(20)) do\n        if valid_player(player) then\n          context.boss:summon_projectile(\"ARROW\", context.boss:get_location(), player:get_location(), 2.0)\n        end\n      end\n    end)\n\n    context.state.attack_arrow_task = task_id\n  end\n}\n";
   }
}
