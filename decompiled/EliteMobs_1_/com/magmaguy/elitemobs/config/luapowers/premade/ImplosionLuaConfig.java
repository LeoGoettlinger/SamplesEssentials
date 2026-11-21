package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ImplosionLuaConfig extends InlineLuaPowerConfig {
   public ImplosionLuaConfig() {
      super(
         "implosion",
         Material.SLIME_BALL.toString(),
         PowersConfigFields.PowerType.MISCELLANEOUS,
         "local function clone_location(location)\n  return em.create_location(\n    location.x,\n    location.y,\n    location.z,\n    location.world,\n    location.yaw,\n    location.pitch\n  )\nend\n\nreturn {\n  api_version = 1,\n  on_death = function(context)\n    local death_location = clone_location(context.boss:get_location())\n    local counter = 0\n    local task_id\n    task_id = context.scheduler.run_every(1, function(context)\n      if counter < 20 then\n        for _ = 1, 20 do\n          context.world.spawn_particle_at_location(death_location, {\n            particle = \"PORTAL\",\n            amount = 1,\n            x = 0.1,\n            y = 0.1,\n            z = 0.1,\n            speed = 1\n          })\n        end\n      end\n\n      if counter > 60 then\n        local entities = context.entities.get_all_entities(\"living\")\n        for index = 1, #entities do\n          local entity = entities[index]\n          local location = entity:get_location()\n          if location.world == death_location.world then\n            local dx = location.x - death_location.x\n            local dy = location.y - death_location.y\n            local dz = location.z - death_location.z\n            if (dx * dx) + (dy * dy) + (dz * dz) <= 100 and entity.game_mode ~= \"SPECTATOR\" then\n              local pull = context.vectors.get_vector_between_locations(location, death_location)\n              pull = context.vectors.normalize_vector(pull)\n              entity:set_velocity_vector(pull)\n            end\n          end\n        end\n\n        context.scheduler.cancel_task(task_id)\n        return\n      end\n\n      counter = counter + 1\n    end)\n  end\n}\n"
      );
   }
}
