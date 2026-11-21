package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class MovementSpeedLuaConfig extends LuaPowersConfigFields {
   public MovementSpeedLuaConfig() {
      super("movement_speed", Material.GOLDEN_BOOTS.toString(), PowersConfigFields.PowerType.MISCELLANEOUS);
   }

   @Override
   public String getSource() {
      return "return {\n  api_version = 1,\n  on_spawn = function(context)\n    context.scheduler:run_after(1, function()\n      if context.boss:is_alive() then\n        context.boss:apply_potion_effect(\"SPEED\", 100000, 1)\n      end\n    end)\n  end\n}\n";
   }
}
