package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackFireLuaConfig extends LuaPowersConfigFields {
   public AttackFireLuaConfig() {
      super("attack_fire", Material.BLAZE_POWDER.toString(), PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "return {\n  api_version = 1,\n  on_player_damaged_by_boss = function(context)\n    if context.player == nil then\n      return\n    end\n    context.player:set_fire_ticks(60)\n  end\n}\n";
   }
}
