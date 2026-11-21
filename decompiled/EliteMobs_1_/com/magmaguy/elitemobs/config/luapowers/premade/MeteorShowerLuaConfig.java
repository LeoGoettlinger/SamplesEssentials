package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class MeteorShowerLuaConfig extends InlineLuaPowerConfig {
   public MeteorShowerLuaConfig() {
      super(
         "meteor_shower",
         null,
         PowersConfigFields.PowerType.UNIQUE,
         "local function clone_location(location)\n  return em.create_location(\n    location.x,\n    location.y,\n    location.z,\n    location.world,\n    location.yaw,\n    location.pitch\n  )\nend\n\nlocal function offset_location(location, x, y, z)\n  return em.create_location(\n    location.x + x,\n    location.y + y,\n    location.z + z,\n    location.world,\n    location.yaw,\n    location.pitch\n  )\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if math.random() > 0.25 or not context.cooldowns.global_ready() then\n      return\n    end\n\n    context.cooldowns.set_global(400)\n    local initial_location = clone_location(context.boss:get_location())\n    context.boss:set_ai_enabled(false)\n\n    local counter = 0\n    local task_id\n    task_id = context.scheduler.run_every(1, function(context)\n      if not context.boss:is_alive() then\n        context.scheduler.cancel_task(task_id)\n        return\n      end\n\n      if counter > 200 then\n        context.scheduler.cancel_task(task_id)\n        context.boss:set_ai_enabled(true)\n        context.boss:teleport_to_location(initial_location)\n        return\n      end\n\n      local cloud_center = offset_location(context.boss:get_location(), 0, 10, 0)\n      local cloud_location = offset_location(cloud_center, math.random(-15, 14), math.random(0, 1), math.random(-15, 14))\n      context.world.spawn_particle_at_location(cloud_location, {\n        particle = \"EXPLOSION\",\n        amount = 1\n      })\n\n      if counter > 40 then\n        local spawn_location = offset_location(cloud_center, math.random(-15, 14), math.random(0, 1), math.random(-15, 14))\n        local direction = em.create_vector(math.random() - 0.5, -0.5, math.random() - 0.5)\n        local destination = offset_location(spawn_location, direction.x, direction.y, direction.z)\n        context.boss:summon_projectile(\"FIREBALL\", spawn_location, destination, 0.5, {\n          duration = 100,\n          spawn_at_origin = true\n        })\n      end\n\n      counter = counter + 1\n    end)\n  end\n}\n"
      );
   }
}
