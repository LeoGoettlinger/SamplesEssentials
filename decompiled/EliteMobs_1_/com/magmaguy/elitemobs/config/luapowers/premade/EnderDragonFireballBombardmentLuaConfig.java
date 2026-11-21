package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonFireballBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
   public EnderDragonFireballBombardmentLuaConfig() {
      super(
         "ender_dragon_fireball_bombardment",
         30,
         5,
         "if context.state.firing_timer % 10 ~= 0 then\n  return\nend\n\nlocal function rotate_around_y_bug(vector, rotation)\n  local cosine = math.cos(rotation)\n  local sine = math.sin(rotation)\n  return em.create_vector(\n    (vector.x * cosine) + (vector.z * sine),\n    vector.y,\n    (-vector.x * sine) + (vector.z * cosine)\n  )\nend\n\nlocal function generate_fireball(vector)\n  local boss_location = context.boss:get_location()\n  local origin = offset_location(boss_location, vector.x, vector.y, vector.z)\n  context.boss:summon_projectile(\n    \"FIREBALL\",\n    origin,\n    offset_location(origin, 0, -0.1, 0),\n    0.1,\n    {\n      spawn_at_origin = true,\n      direction_only = true,\n      yield = 5\n    }\n  )\nend\n\nlocal direction = context.boss:get_location().direction\nlocal rotation = math.atan2(direction.x, direction.z) * 180 / math.pi\nlocal direction1 = rotate_around_y_bug(em.create_vector(3, -4, 0), rotation)\nlocal direction2 = rotate_around_y_bug(em.create_vector(-3, -4, 0), rotation)\n\ngenerate_fireball(direction1)\ngenerate_fireball(direction2)\n"
      );
   }
}
