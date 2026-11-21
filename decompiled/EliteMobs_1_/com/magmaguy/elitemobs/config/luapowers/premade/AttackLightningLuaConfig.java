package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackLightningLuaConfig extends LuaPowersConfigFields {
   public AttackLightningLuaConfig() {
      super("attack_lightning", Material.HORN_CORAL.toString(), PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "local function strike_after_warning(context, location)\n  local task_id\n  local counter = 0\n  task_id = context.scheduler:run_every(1, function()\n    counter = counter + 1\n    if counter > 60 then\n      context.scheduler:cancel_task(task_id)\n      context.world:strike_lightning_at_location(location)\n      return\n    end\n\n    context.world:spawn_particle_at_location(location, {\n      particle = \"CRIT\",\n      amount = 10,\n      x = 0.5,\n      y = 1.5,\n      z = 0.5,\n      speed = 0.3\n    }, 10)\n  end)\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if not context.cooldowns.global_ready() then\n      return\n    end\n\n    context.cooldowns.set_global(300)\n    for _, player in ipairs(context.boss:get_nearby_players(20)) do\n      strike_after_warning(context, player:get_location())\n    end\n  end\n}\n";
   }
}
