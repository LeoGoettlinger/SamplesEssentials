package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ShieldWallLuaConfig extends InlineLuaPowerConfig {
   public ShieldWallLuaConfig() {
      super(
         "shield_wall",
         Material.SHIELD.toString(),
         PowersConfigFields.PowerType.DEFENSIVE,
         "return {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if context.boss:shield_wall_is_active() then\n      if context.player ~= nil and context.boss:shield_wall_absorb_damage(context.player, context.event.damage_amount) then\n        context.event.cancel_event()\n      end\n      return\n    end\n\n    if not context.cooldowns:local_ready(\"shield_wall\")\n      or not context.cooldowns:global_ready()\n      or math.random() >= 0.1 then\n      return\n    end\n\n    context.cooldowns:set_local(20 * 60, \"shield_wall\")\n    context.cooldowns:set_global(20 * 7)\n    context.boss:initialize_shield_wall(math.random(1, 4))\n  end,\n  on_exit_combat = function(context)\n    context.boss:deactivate_shield_wall()\n  end\n}\n"
      );
   }
}
