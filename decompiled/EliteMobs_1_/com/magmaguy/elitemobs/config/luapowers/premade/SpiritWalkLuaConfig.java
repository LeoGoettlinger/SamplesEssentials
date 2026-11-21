package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class SpiritWalkLuaConfig extends InlineLuaPowerConfig {
   public SpiritWalkLuaConfig() {
      super(
         "spirit_walk",
         null,
         PowersConfigFields.PowerType.UNIQUE,
         "return {\n  api_version = 1,\n  on_boss_damaged = function(context)\n    if context.event.damage_cause == nil then\n      return\n    end\n    context.boss:handle_spirit_walk_damage(context.event.damage_cause)\n  end\n}\n"
      );
   }
}
