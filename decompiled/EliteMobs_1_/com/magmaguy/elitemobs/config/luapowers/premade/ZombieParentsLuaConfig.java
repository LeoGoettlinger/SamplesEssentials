package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import java.util.List;
import org.bukkit.Material;

public class ZombieParentsLuaConfig extends LuaPowersConfigFields {
   private static final List<String> DEATH_MESSAGE = List.of(
      "You monster!",
      "My baby!",
      "What have you done!?",
      "Revenge!",
      "Nooooo!",
      "You will pay for that!",
      "Eh, he was adopted",
      "He's dead! Again!",
      "He's deader than before!",
      "You broke him!"
   );
   private static final List<String> BOSS_ENTITY_DIALOG = List.of(
      "You're embarrassing me!",
      "He's bullying me!",
      "He's the one picking on me!",
      "I can deal with this alone!",
      "Leave me alone, I got this!",
      "Stop following me around!",
      "God this is so embarrassing!",
      "He took my lunch money!",
      "He's bullying me!"
   );
   private static final List<String> ZOMBIE_DAD = List.of(
      "Get away from my son!",
      "Stand up for yourself son!",
      "I'll deal with him!",
      "Stop picking on my son!",
      "Why are you doing this?",
      "I'll talk to your parents!",
      "You go kiddo!",
      "Show him who's boss kiddo!",
      "Nice punch kiddo!"
   );
   private static final List<String> ZOMBIE_MOM = List.of(
      "Hands off my child!",
      "Are you hurt sweetie?",
      "Did he hurt you sweetie?",
      "Let me see that booboo sweetie",
      "I'll talk to his parents!",
      "You forgot your jacket sweetie!",
      "Posture, sweetheart",
      "Break it up!",
      "Stop this!",
      "Did you take out the garbage?"
   );

   public ZombieParentsLuaConfig() {
      super("zombie_parents", Material.SKELETON_SKULL.toString(), PowersConfigFields.PowerType.MAJOR_ZOMBIE);
   }

   @Override
   public String getSource() {
      return "local death_message = %s\nlocal boss_entity_dialog = %s\nlocal zombie_dad = %s\nlocal zombie_mom = %s\n\nlocal function say_random_line(context, entity, list)\n  if entity == nil or #list == 0 then\n    return\n  end\n  entity:set_custom_name(list[math.random(#list)])\n  context.scheduler:run_after(20 * 3, function()\n    if entity ~= nil and entity.is_alive ~= nil and entity:is_alive() then\n      entity:reset_custom_name()\n    end\n  end)\nend\n\nlocal function start_dialog(context, reinforcement_mom, reinforcement_dad)\n  local families = context.state.zombie_parent_families\n  if families == nil then\n    families = {}\n    context.state.zombie_parent_families = families\n  end\n  families[#families + 1] = { mom = reinforcement_mom, dad = reinforcement_dad }\n  if context.state.zombie_parents_dialog_task ~= nil then\n    return\n  end\n\n  context.state.zombie_parents_dialog_task = context.scheduler:run_repeating(20, 20 * 8, function(context)\n    local boss_alive = context.boss:is_alive()\n    for index = #families, 1, -1 do\n      local family = families[index]\n      local mom_alive = family.mom ~= nil and family.mom:is_alive()\n      local dad_alive = family.dad ~= nil and family.dad:is_alive()\n      if not mom_alive and not dad_alive then\n        table.remove(families, index)\n      else\n        if dad_alive and (not boss_alive or math.random() < 0.5) then\n          say_random_line(context, family.dad, boss_alive and zombie_dad or death_message)\n        end\n        if mom_alive and (not boss_alive or math.random() < 0.5) then\n          say_random_line(context, family.mom, boss_alive and zombie_mom or death_message)\n        end\n      end\n    end\n    if not boss_alive or #families == 0 then\n      context.scheduler:cancel_task(context.state.zombie_parents_dialog_task)\n      context.state.zombie_parents_dialog_task = nil\n      context.state.zombie_parent_families = nil\n      return\n    end\n    if math.random() < 0.5 then\n      say_random_line(context, context.boss, boss_entity_dialog)\n    end\n  end)\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if math.random() > 0.01 then\n      return\n    end\n\n    local boss_location = context.boss:get_location()\n    local reinforcement_mom = context.world:spawn_custom_boss_at_location(\n      \"zombie_parents_mom.yml\",\n      boss_location,\n      { level = context.boss.level, silent = false }\n    )\n    if reinforcement_mom == nil then\n      return\n    end\n\n    local reinforcement_dad = context.world:spawn_custom_boss_at_location(\n      \"zombie_parents_dad.yml\",\n      boss_location,\n      { level = context.boss.level, silent = false }\n    )\n    if reinforcement_dad == nil then\n      reinforcement_mom:remove_elite()\n      return\n    end\n\n    start_dialog(context, reinforcement_mom, reinforcement_dad)\n  end\n}\n"
         .formatted(luaList(DEATH_MESSAGE), luaList(BOSS_ENTITY_DIALOG), luaList(ZOMBIE_DAD), luaList(ZOMBIE_MOM));
   }
}
