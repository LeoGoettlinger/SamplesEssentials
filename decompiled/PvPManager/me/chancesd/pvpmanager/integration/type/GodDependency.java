package me.chancesd.pvpmanager.integration.type;

import org.bukkit.entity.Player;

public interface GodDependency extends Dependency {
   boolean hasGodMode(Player var1);

   void enableGodMode(Player var1);

   void disableGodMode(Player var1);
}
