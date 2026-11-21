package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import java.util.List;
import org.bukkit.Material;

public class ZombieFriendsLuaConfig extends LuaPowersConfigFields {
   private static final List<String> FRIEND_DEATH_MESSAGE = List.of(
      "Noooo!", "Mediocre!", "Zacharias!", "He's deader than before!", "Vengeance!", "Revenge!", "I can't believe you've done this."
   );
   private static final List<String> ORIGINAL_ENTITY_DIALOG = List.of(
      "Let's play ZombieCraft later!",
      "Feel the power of friendship!",
      "El pueblo, unido!",
      "I called my friends over!",
      "BFF power!",
      "One for all!",
      "Get him!",
      "Screw you guys, I'm going home!"
   );
   private static final List<String> REINFORCEMENT_DIALOG = List.of(
      "Don't mess with our friends!",
      "We got your back Zach!",
      "Backup has arrived!",
      "One for all, one for all!",
      "This is going to be easy!",
      "Give up we have the high ground!",
      "You wanna go bruv?",
      "Worldstaaaaaaaaar!",
      "What are you doing to our friend",
      "Feel the power of friendship!",
      "Friendship power at 100%!",
      "Zombies, assemble!",
      "We got your back mate!",
      "Together we are better!",
      "The more the merrier!",
      "I got you fam!",
      "All for one!"
   );

   public ZombieFriendsLuaConfig() {
      super("zombie_friends", Material.ZOMBIE_HEAD.toString(), PowersConfigFields.PowerType.MAJOR_ZOMBIE);
   }

   @Override
   public String getSource() {
      return "local friend_death_message = %s\nlocal original_entity_dialog = %s\nlocal reinforcement_dialog = %s\n\nlocal function say_random_line(context, entity, list)\n  if entity == nil or #list == 0 then\n    return\n  end\n  entity:set_custom_name(list[math.random(#list)])\n  context.scheduler:run_after(20 * 3, function()\n    if entity ~= nil and entity.is_alive ~= nil and entity:is_alive() then\n      entity:reset_custom_name()\n    end\n  end)\nend\n\nlocal function start_dialog_task(context, reinforcement_one, reinforcement_two)\n  context.scheduler:run_after(20, function(context)\n    local task_id\n    local function run_dialog()\n      local boss_alive = context.boss:is_alive()\n      local reinforcement_one_alive = reinforcement_one ~= nil and reinforcement_one:is_alive()\n      local reinforcement_two_alive = reinforcement_two ~= nil and reinforcement_two:is_alive()\n\n      if not boss_alive or (not reinforcement_one_alive and not reinforcement_two_alive) then\n        if reinforcement_one_alive then\n          say_random_line(context, reinforcement_one, friend_death_message)\n        end\n        if reinforcement_two_alive then\n          say_random_line(context, reinforcement_two, friend_death_message)\n        end\n        if task_id ~= nil then\n          context.scheduler:cancel_task(task_id)\n        end\n        return false\n      end\n\n      if math.random() < 0.5 then\n        say_random_line(context, context.boss, original_entity_dialog)\n      end\n\n      if reinforcement_one_alive and math.random() < 0.5 then\n        say_random_line(context, reinforcement_one, reinforcement_dialog)\n      end\n\n      if reinforcement_two_alive and math.random() < 0.5 then\n        say_random_line(context, reinforcement_two, reinforcement_dialog)\n      end\n\n      return true\n    end\n\n    if run_dialog() then\n      task_id = context.scheduler:run_every(20 * 8, function(context)\n        run_dialog()\n      end)\n    end\n  end)\nend\n\nreturn {\n  api_version = 1,\n  on_boss_damaged_by_player = function(context)\n    if context.state.zombie_friends_firing then\n      return\n    end\n\n    if math.random() > 0.01 then\n      return\n    end\n\n    context.state.zombie_friends_firing = true\n\n    local boss_location = context.boss:get_location()\n    local reinforcement_one = context.world:spawn_custom_boss_at_location(\n      \"zombie_friends_friend.yml\",\n      boss_location,\n      { level = context.boss.level, silent = false }\n    )\n    local reinforcement_two = context.world:spawn_custom_boss_at_location(\n      \"zombie_friends_friend.yml\",\n      boss_location,\n      { level = context.boss.level, silent = false }\n    )\n\n    start_dialog_task(context, reinforcement_one, reinforcement_two)\n  end\n}\n"
         .formatted(luaList(FRIEND_DEATH_MESSAGE), luaList(ORIGINAL_ENTITY_DIALOG), luaList(REINFORCEMENT_DIALOG));
   }
}
