package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonEndermiteBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
   public EnderDragonEndermiteBombardmentLuaConfig() {
      super(
         "ender_dragon_endermite_bombardment",
         120,
         30,
         "local function rotate_around_y_bug(vector, rotation)\n  local cosine = math.cos(rotation)\n  local sine = math.sin(rotation)\n  return em.create_vector(\n    (vector.x * cosine) + (vector.z * sine),\n    vector.y,\n    (-vector.x * sine) + (vector.z * cosine)\n  )\nend\n\nlocal function spawn_endermite(offset)\n  local spawned = context.world:spawn_custom_boss_at_location(\n    \"binder_of_worlds_phase_1_endermite_reinforcement.yml\",\n    offset_location(context.boss:get_location(), offset.x, offset.y, offset.z),\n    {\n      add_as_reinforcement = true\n    }\n  )\n  if spawned ~= nil then\n    spawned:apply_potion_effect(\"SLOW_FALLING\", 20 * 5, 0)\n  end\nend\n\nlocal direction = context.boss:get_location().direction\nlocal rotation = math.atan2(direction.x, direction.z) * 180 / math.pi\nlocal direction1 = rotate_around_y_bug(em.create_vector(1, -4, 0), rotation)\nlocal direction2 = rotate_around_y_bug(em.create_vector(-1, -4, 0), rotation)\n\nspawn_endermite(direction1)\nspawn_endermite(direction2)\n"
      );
   }
}
