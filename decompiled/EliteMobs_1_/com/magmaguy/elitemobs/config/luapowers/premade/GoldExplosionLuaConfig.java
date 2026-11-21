package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class GoldExplosionLuaConfig extends InlineLuaPowerConfig {
   public GoldExplosionLuaConfig() {
      super(
         "gold_explosion",
         null,
         PowersConfigFields.PowerType.UNIQUE,
         "local function offset_location(location, x, y, z)\n  return em.create_location(\n    location.x + x,\n    location.y + y,\n    location.z + z,\n    location.world,\n    location.yaw,\n    location.pitch\n  )\nend\n\nlocal function generate_visual_projectiles(context)\n  local projectiles = {}\n  local boss_location = context.boss:get_location()\n  for _ = 1, 200 do\n    local velocity = em.create_vector(\n      math.random() - 0.5,\n      math.random() / 1.5,\n      math.random() - 0.5\n    )\n    projectiles[#projectiles + 1] = context.world:spawn_fake_gold_nugget_at_location(\n      offset_location(boss_location, velocity.x, 0.5, velocity.z),\n      velocity,\n      true\n    )\n  end\n  return projectiles\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if not context.boss:is_ai_enabled()\n      or not context.cooldowns:global_ready()\n      or math.random() > 0.25 then\n      return\n    end\n\n    context.cooldowns:set_global(20 * 20)\n    context.boss:set_ai_enabled(false)\n\n    local counter = 0\n    local task_id\n    task_id = context.scheduler:run_every(1, function(context)\n      if not context.boss:is_alive() then\n        context.scheduler:cancel_task(task_id)\n        return\n      end\n\n      counter = counter + 1\n      if context.settings:warning_visual_effects_enabled() then\n        context.world:spawn_particle_at_location(context.boss:get_location(), {\n          particle = \"SMOKE\",\n          amount = counter,\n          x = 1,\n          y = 1,\n          z = 1,\n          speed = 0\n        })\n      end\n\n      if counter < (20 * 1.5) then\n        return\n      end\n\n      context.scheduler:cancel_task(task_id)\n      context.boss:set_ai_enabled(true)\n      context.world:run_fake_gold_nugget_damage(generate_visual_projectiles(context))\n    end)\n  end\n}\n"
      );
   }
}
