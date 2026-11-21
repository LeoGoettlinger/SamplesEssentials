package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonArrowBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
   public EnderDragonArrowBombardmentLuaConfig() {
      super(
         "ender_dragon_arrow_bombardment",
         30,
         5,
         "local boss_location = context.boss:get_location()\nlocal players = get_box_players(context, 200, 100, 200)\nfor index = 1, #players do\n  local target_location = offset_location(players[index]:get_location(), 0, 1, 0)\n  local shot_vector = context.vectors:get_vector_between_locations(boss_location, target_location)\n  shot_vector = em.create_vector(\n    shot_vector.x + (math.random() - 0.5),\n    shot_vector.y + (math.random() - 0.5),\n    shot_vector.z + (math.random() - 0.5)\n  )\n  shot_vector = context.vectors:normalize_vector(shot_vector)\n  shot_vector = em.create_vector(\n    shot_vector.x * 2,\n    shot_vector.y * 2,\n    shot_vector.z * 2\n  )\n\n  local origin = offset_location(boss_location, shot_vector.x, shot_vector.y + 1, shot_vector.z)\n  context.boss:summon_projectile(\n    \"ARROW\",\n    origin,\n    offset_location(origin, shot_vector.x, shot_vector.y, shot_vector.z),\n    vector_length(shot_vector),\n    {\n      spawn_at_origin = true,\n      gravity = false,\n      persistent = false,\n      duration = 20 * 4\n    }\n  )\nend\n"
      );
   }
}
