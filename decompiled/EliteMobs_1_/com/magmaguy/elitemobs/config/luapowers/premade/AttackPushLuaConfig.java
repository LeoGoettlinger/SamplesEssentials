package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackPushLuaConfig extends LuaPowersConfigFields {
   public AttackPushLuaConfig() {
      super("attack_push", Material.PISTON.toString(), PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "return {\n  api_version = 1,\n  on_player_damaged_by_boss = function(context)\n    if context.player == nil or not context.cooldowns.global_ready() then\n      return\n    end\n\n    local boss_location = context.boss:get_location()\n    local player_location = context.player:get_location()\n    local push = context.vectors.get_vector_between_locations(boss_location, player_location)\n    push = context.vectors.normalize_vector(push)\n\n    context.player:set_velocity_vector({\n      x = push.x * 3,\n      y = push.y * 3,\n      z = push.z * 3\n    })\n    context.cooldowns.set_global(200)\n  end\n}\n";
   }
}
