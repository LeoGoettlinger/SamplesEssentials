package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonAimedFireballLuaConfig extends EnderDragonBombardmentLuaConfig {
   public EnderDragonAimedFireballLuaConfig() {
      super(
         "ender_dragon_aimed_fireball",
         30,
         5,
         "if context.state.firing_timer % 20 ~= 0 then\n  return\nend\n\nlocal boss_location = context.boss:get_location()\nlocal players = get_box_players(context, 200, 100, 200)\nfor index = 1, #players do\n  context.boss:summon_projectile(\n    \"FIREBALL\",\n    clone_location(boss_location),\n    players[index]:get_location(),\n    1,\n    {\n      spawn_at_origin = true,\n      direction_only = true,\n      yield = 5\n    }\n  )\nend\n"
      );
   }
}
