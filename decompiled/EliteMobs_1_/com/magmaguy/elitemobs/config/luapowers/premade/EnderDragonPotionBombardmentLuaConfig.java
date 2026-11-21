package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonPotionBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
   public EnderDragonPotionBombardmentLuaConfig() {
      super(
         "ender_dragon_potion_bombardment",
         30,
         5,
         "local boss_location = context.boss:get_location()\nfor _ = 1, 5 do\n  local effect_type = \"SLOWNESS\"\n  if math.random(0, 1) == 1 then\n    effect_type = \"STRENGTH\"\n  end\n\n  local spawn_location = offset_location(\n    boss_location,\n    -math.random(-1, 0),\n    -1,\n    -math.random(-1, 0)\n  )\n\n  context.world:spawn_splash_potion_at_location(spawn_location, {\n    effects = {\n      {\n        type = effect_type,\n        duration = 0,\n        amplifier = 1,\n        overwrite = true\n      }\n    },\n    velocity = em.create_vector(\n      math.random() - 0.5,\n      0,\n      math.random() - 0.5\n    )\n  })\nend\n"
      );
   }
}
