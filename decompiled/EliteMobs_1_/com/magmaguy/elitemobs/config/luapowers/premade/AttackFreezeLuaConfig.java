package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackFreezeLuaConfig extends LuaPowersConfigFields {
   public AttackFreezeLuaConfig() {
      super("attack_freeze", Material.PACKED_ICE.toString(), PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "return {\n  api_version = 1,\n  on_player_damaged_by_boss = function(context)\n    if context.player == nil then\n      return\n    end\n    if not context.cooldowns.local_ready() or not context.cooldowns.global_ready() then\n      return\n    end\n\n    local player = context.player\n    player:apply_potion_effect(\"slowness\", 60, 5)\n    player:place_temporary_block(\"PACKED_ICE\", 60, true)\n\n    local iterations = 0\n    local task_id = nil\n    task_id = context.scheduler.run_every(1, function()\n      iterations = iterations + 1\n      player:add_visual_freeze_ticks(3)\n      if iterations >= 60 then\n        context.scheduler.cancel_task(task_id)\n      end\n    end)\n\n    context.cooldowns.set_local(300)\n    context.cooldowns.set_global(60)\n  end\n}\n";
   }
}
