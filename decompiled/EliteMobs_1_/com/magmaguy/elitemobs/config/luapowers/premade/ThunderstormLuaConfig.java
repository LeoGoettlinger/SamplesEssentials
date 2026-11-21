package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class ThunderstormLuaConfig extends LuaPowersConfigFields {
   public ThunderstormLuaConfig() {
      super("thunderstorm", null, PowersConfigFields.PowerType.UNIQUE);
   }

   @Override
   public String getSource() {
      return "local function random_location_around(origin)\n  return em.create_location(\n    origin.x + math.random(-20, 19),\n    origin.y,\n    origin.z + math.random(-20, 19),\n    origin.world,\n    origin.yaw,\n    origin.pitch\n  )\nend\n\nlocal function strike_after_warning(context, location)\n  local task_id\n  local counter = 0\n  task_id = context.scheduler:run_every(1, function()\n    counter = counter + 1\n    if counter > 60 then\n      context.scheduler:cancel_task(task_id)\n      context.world:strike_lightning_at_location(location)\n      return\n    end\n\n    context.world:spawn_particle_at_location(location, {\n      particle = \"CRIT\",\n      amount = 10,\n      x = 0.5,\n      y = 1.5,\n      z = 0.5,\n      speed = 0.3\n    }, 10)\n  end)\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if not context.cooldowns.global_ready() or math.random() > 0.25 then\n      return\n    end\n\n    context.cooldowns.set_global(400)\n    context.boss:set_ai_enabled(false, 100)\n    context.state.thunderstorm_active = true\n\n    local storm_task\n    local counter = 0\n    storm_task = context.scheduler:run_every(1, function()\n      counter = counter + 1\n      if counter > 100 or not context.boss:is_alive() then\n        context.scheduler:cancel_task(storm_task)\n        context.state.thunderstorm_active = false\n        return\n      end\n\n      if counter % 2 == 0 then\n        strike_after_warning(context, random_location_around(context.boss:get_location()))\n      end\n\n      if counter % 20 == 0 then\n        for _, player in ipairs(context.boss:get_nearby_players(20)) do\n          strike_after_warning(context, player:get_location())\n        end\n      end\n    end)\n  end,\n\n  on_boss_damaged = function(context)\n    if context.state.thunderstorm_active and context.event.damage_cause == \"LIGHTNING\" then\n      context.event.cancel_event()\n    end\n  end\n}\n";
   }
}
