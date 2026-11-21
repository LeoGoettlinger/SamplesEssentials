package me.chancesd.pvpmanager.integration.hook;

import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.DisguiseDependency;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.entity.Player;

public class LibsDisguisesHook extends BaseDependency implements DisguiseDependency {
   public LibsDisguisesHook(Hook hook) {
      super(hook);
   }

   @Override
   public boolean isDisguised(Player player) {
      return DisguiseAPI.isDisguised(player);
   }

   @Override
   public void unDisguise(Player player) {
      DisguiseAPI.undisguiseToAll(player);
   }

   @Override
   public String onEnableMessage() {
      return "LibsDisguises Found! Using it to disable disguises";
   }
}
