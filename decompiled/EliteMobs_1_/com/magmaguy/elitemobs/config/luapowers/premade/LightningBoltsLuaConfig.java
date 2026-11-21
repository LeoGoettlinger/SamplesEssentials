package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class LightningBoltsLuaConfig extends LuaPowersConfigFields {
   public LightningBoltsLuaConfig() {
      super("lightning_bolts", null, PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "local function strike_after_warning(context, location, delay)\n  context.scheduler:run_after(delay, function()\n    local task_id\n    local counter = 0\n    task_id = context.scheduler:run_every(1, function()\n      counter = counter + 1\n      if counter > 40 then\n        context.scheduler:cancel_task(task_id)\n        context.world:strike_lightning_at_location(location)\n        return\n      end\n\n      context.world:spawn_particle_at_location(location, {\n        particle = \"CRIT\",\n        amount = 10,\n        x = 0.5,\n        y = 1.5,\n        z = 0.5,\n        speed = 0.3\n      }, 10)\n    end)\n  end)\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if not context.cooldowns.local_ready(\"lightning_bolts\") or not context.cooldowns.global_ready() then\n      return\n    end\n\n    context.cooldowns.set_local(400, \"lightning_bolts\")\n    context.cooldowns.set_global(100)\n    context.boss:set_ai_enabled(false, 80)\n\n    for _, player in ipairs(context.boss:get_nearby_players(20)) do\n      local player_location = player:get_location()\n      local boss_location = context.boss:get_location()\n      local ray = context.vectors.get_vector_between_locations(boss_location, player_location)\n      ray = context.vectors.normalize_vector(ray)\n      local dx = player_location.x - boss_location.x\n      local dy = player_location.y - boss_location.y\n      local dz = player_location.z - boss_location.z\n      local steps = math.max(1, math.floor(math.sqrt(dx * dx + dy * dy + dz * dz)))\n\n      for step = 1, steps do\n        local location = em.create_location(\n          boss_location.x + ray.x * step,\n          boss_location.y + ray.y * step,\n          boss_location.z + ray.z * step,\n          boss_location.world\n        )\n        strike_after_warning(context, location, step * 5)\n      end\n    end\n  end\n}\n";
   }
}
