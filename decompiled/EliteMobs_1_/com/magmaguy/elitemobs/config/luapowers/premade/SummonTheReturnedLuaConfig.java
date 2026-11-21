package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class SummonTheReturnedLuaConfig extends InlineLuaPowerConfig {
   public SummonTheReturnedLuaConfig() {
      super(
         "summon_the_returned",
         null,
         PowersConfigFields.PowerType.UNIQUE,
         "local function offset_location(location, x, y, z)\n  return em.create_location(\n    location.x + x,\n    location.y + y,\n    location.z + z,\n    location.world,\n    location.yaw,\n    location.pitch\n  )\nend\n\nlocal function do_summon(context)\n  for _ = 1, 10 do\n    local spawn_location = context.boss:get_location()\n    context.world.spawn_boss_at_location(\"the_returned.yml\", spawn_location, context.boss.level)\n    if context.boss:is_alive() then\n      context.boss:set_velocity_vector(em.create_vector(\n        math.random() - 0.5,\n        0.5,\n        math.random() - 0.5\n      ))\n    end\n  end\nend\n\nlocal function do_summon_particles(context)\n  context.boss:set_ai_enabled(false)\n  local counter = 0\n  local task_id\n  task_id = context.scheduler:run_every(1, function(context)\n    if not context.boss:is_alive() then\n      context.scheduler:cancel_task(task_id)\n      return\n    end\n    counter = counter + 1\n    context.world.spawn_particle_at_location(\n      offset_location(context.boss:get_location(), 0, 1, 0),\n      {\n        particle = \"PORTAL\",\n        amount = 50,\n        x = 0.01,\n        y = 0.01,\n        z = 0.01,\n        speed = 1\n      }\n    )\n    if counter < 60 then\n      return\n    end\n    context.scheduler:cancel_task(task_id)\n    do_summon(context)\n    context.boss:set_ai_enabled(true)\n  end)\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if math.random() > 0.25\n      or not context.boss:is_ai_enabled()\n      or not context.cooldowns.global_ready() then\n      return\n    end\n\n    context.cooldowns.set_global(400)\n    do_summon_particles(context)\n  end\n}\n"
      );
   }
}
