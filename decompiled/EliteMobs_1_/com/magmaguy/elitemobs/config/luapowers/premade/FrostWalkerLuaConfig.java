package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class FrostWalkerLuaConfig extends InlineLuaPowerConfig {
   public FrostWalkerLuaConfig() {
      super(
         "frost_walker",
         null,
         PowersConfigFields.PowerType.MISCELLANEOUS,
         "return {\n  api_version = 1,\n  on_spawn = function(context)\n    context.scheduler:run_after(1, function(context)\n      context.boss:set_equipment(\"FEET\", \"LEATHER_BOOTS\", {\n        enchantments = {\n          { type = \"FROST_WALKER\", level = 2 },\n          { type = \"DEPTH_STRIDER\", level = 3 }\n        }\n      })\n    end)\n  end\n}\n"
      );
   }
}
