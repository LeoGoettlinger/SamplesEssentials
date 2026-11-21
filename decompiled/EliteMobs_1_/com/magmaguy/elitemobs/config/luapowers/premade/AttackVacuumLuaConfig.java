package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackVacuumLuaConfig extends LuaPowersConfigFields {
   public AttackVacuumLuaConfig() {
      super("attack_vacuum", Material.LEAD.toString(), PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "return {\n  api_version = 1,\n  on_player_damaged_by_boss = function(context)\n    if context.player == nil then\n      return\n    end\n\n    local pull = context.vectors.get_vector_between_locations(\n      context.player:get_location(),\n      context.boss:get_location()\n    )\n    pull = context.vectors.normalize_vector(pull)\n    context.player:set_velocity_vector(pull)\n  end\n}\n";
   }
}
