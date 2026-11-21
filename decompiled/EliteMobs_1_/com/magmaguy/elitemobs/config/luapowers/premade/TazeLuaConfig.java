package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class TazeLuaConfig extends LuaPowersConfigFields {
   public TazeLuaConfig() {
      super("taze", null, PowersConfigFields.PowerType.OFFENSIVE);
   }

   @Override
   public String getSource() {
      return "local function taze_player(context, player_location, depth)\n  if depth > 2 or context.player == nil then\n    return\n  end\n\n  local push = context.vectors.get_vector_between_locations(player_location, context.player:get_location())\n  if push.x == 0 and push.y == 0 and push.z == 0 then\n    push = em.create_vector(0, 0.5, 0)\n  else\n    push = context.vectors.normalize_vector(push)\n  end\n\n  context.world:spawn_particle_at_location(context.player:get_location(), {\n    particle = \"FIREWORKS_SPARK\",\n    amount = 20,\n    x = 0.5,\n    y = 0.5,\n    z = 0.5,\n    speed = 0.1\n  })\n  context.player:show_title(\"&7Tazed!\", \"\", 5, 30, 5)\n  context.player:set_velocity_vector(push)\n  context.player:apply_potion_effect(\"SLOWNESS\", 30, 5)\n  context.scheduler:run_after(5, function()\n    taze_player(context, player_location, depth + 1)\n  end)\nend\n\nreturn {\n  api_version = 1,\n  on_player_damaged_by_boss = function(context)\n    if context.player == nil\n      or not context.cooldowns.local_ready(\"taze\")\n      or not context.cooldowns.global_ready() then\n      return\n    end\n\n    context.cooldowns.set_local(60, \"taze\")\n    context.cooldowns.set_global(20)\n    taze_player(context, context.boss:get_location(), 0)\n  end\n}\n";
   }
}
