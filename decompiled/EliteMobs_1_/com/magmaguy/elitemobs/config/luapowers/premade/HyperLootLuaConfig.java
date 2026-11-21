package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class HyperLootLuaConfig extends InlineLuaPowerConfig {
   public HyperLootLuaConfig() {
      super(
         "hyper_loot",
         null,
         PowersConfigFields.PowerType.UNIQUE,
         "return {\n  api_version = 1,\n  on_death = function(context)\n    context.world:generate_player_loot(10)\n  end\n}\n"
      );
   }
}
