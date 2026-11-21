package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class ArrowRainLuaConfig extends LuaPowersConfigFields {
   public ArrowRainLuaConfig() {
      super("arrow_rain", Particle.DRIPPING_WATER.toString(), PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "local function random_between(minimum, maximum)\n  return minimum + math.random() * (maximum - minimum)\nend\n\nlocal function random_int(minimum, maximum)\n  return math.random(minimum, maximum)\nend\n\nlocal function clone_location(location)\n  return em.create_location(location.x, location.y, location.z, location.world, location.yaw, location.pitch)\nend\n\nlocal function offset_location(location, x, y, z)\n  local copy = clone_location(location)\n  copy:add(x, y, z)\n  return copy\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if not context.boss:is_ai_enabled() then\n      return\n    end\n    if not context.cooldowns.global_ready() or math.random() > 0.15 then\n      return\n    end\n\n    context.cooldowns.set_global(300)\n    local initial_location = clone_location(context.boss:get_location())\n    local task_id\n    local counter = 0\n    task_id = context.scheduler:run_every(1, function()\n      counter = counter + 1\n      if counter > 200 or not context.boss:is_alive() then\n        context.scheduler:cancel_task(task_id)\n        if context.boss:is_alive() then\n          context.boss:teleport_to_location(initial_location)\n        end\n        return\n      end\n\n      local cloud_origin = offset_location(context.boss:get_location(), 0, 10, 0)\n      local cloud_location = offset_location(\n        cloud_origin,\n        random_int(-15, 14),\n        random_int(0, 1),\n        random_int(-15, 14)\n      )\n      context.world:spawn_particle_at_location(cloud_location, \"EXPLOSION\", 1)\n\n      if counter > 20 then\n        local arrow_origin = offset_location(\n          cloud_origin,\n          random_int(-15, 14),\n          random_int(0, 1),\n          random_int(-15, 14)\n        )\n        local arrow_target = offset_location(\n          arrow_origin,\n          random_between(-0.5, 0.5),\n          -0.5,\n          random_between(-0.5, 0.5)\n        )\n        context.boss:summon_projectile(\"ARROW\", arrow_origin, arrow_target, 1.0, { spawn_at_origin = true })\n      end\n    end)\n  end\n}\n";
   }
}
