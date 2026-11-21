package me.chancesd.pvpmanager.integration.type;

import org.bukkit.entity.Player;

public interface DisguiseDependency extends Dependency {
   boolean isDisguised(Player var1);

   void unDisguise(Player var1);
}
