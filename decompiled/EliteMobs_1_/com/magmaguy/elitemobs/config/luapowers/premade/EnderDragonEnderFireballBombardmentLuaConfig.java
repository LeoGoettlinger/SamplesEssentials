package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonEnderFireballBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
   public EnderDragonEnderFireballBombardmentLuaConfig() {
      super(
         "ender_dragon_ender_fireball_bombardment",
         30,
         5,
         "if math.random() > 0.1 then\n  return\nend\n\nlocal boss_location = context.boss:get_location()\nlocal players = get_box_players(context, 200, 100, 200)\nfor index = 1, #players do\n  context.boss:summon_projectile(\n    \"DRAGON_FIREBALL\",\n    clone_location(boss_location),\n    players[index]:get_location(),\n    1,\n    {\n      spawn_at_origin = true,\n      direction_only = true,\n      track = false\n    }\n  )\nend\n"
      );
   }
}
