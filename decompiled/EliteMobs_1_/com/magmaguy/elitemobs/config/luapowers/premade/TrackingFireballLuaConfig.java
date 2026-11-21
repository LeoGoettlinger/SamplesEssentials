package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class TrackingFireballLuaConfig extends LuaPowersConfigFields {
   public TrackingFireballLuaConfig() {
      super("tracking_fireball", Material.FIRE_CHARGE.toString(), PowersConfigFields.PowerType.MAJOR_GHAST);
   }

   @Override
   public String getSource() {
      return "local fireball_speed = 0.5\n\nreturn {\n  api_version = 1,\n  on_boss_target_changed = function(context)\n    if not context.boss.is_monster then\n      return\n    end\n\n    context.boss:start_tracking_fireball_system(fireball_speed)\n  end\n}\n";
   }
}
