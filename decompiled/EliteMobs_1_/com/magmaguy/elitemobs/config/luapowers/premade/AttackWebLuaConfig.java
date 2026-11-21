package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackWebLuaConfig extends LuaPowersConfigFields {
   public AttackWebLuaConfig() {
      super("attack_web", Material.COBWEB.toString(), PowersConfigFields.PowerType.MISCELLANEOUS);
   }

   @Override
   public String getSource() {
      return "return {\n  api_version = 1,\n  on_player_damaged_by_boss = function(context)\n    if context.player == nil then\n      return\n    end\n    if not context.cooldowns.local_ready() or not context.cooldowns.global_ready() then\n      return\n    end\n\n    context.player:place_temporary_block(\"COBWEB\", 60, true)\n    context.boss:apply_potion_effect(\"SPEED\", 60, 2)\n    context.cooldowns.set_local(60)\n    context.cooldowns.set_global(20)\n  end\n}\n";
   }
}
