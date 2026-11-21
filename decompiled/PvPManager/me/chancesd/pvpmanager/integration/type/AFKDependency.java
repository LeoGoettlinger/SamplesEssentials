package me.chancesd.pvpmanager.integration.type;

import org.bukkit.entity.Player;

public interface AFKDependency extends Dependency {
   boolean isAFK(Player var1);

   default boolean shouldProtectAFK() {
      return this.getConfigBoolean("Protect AFK", true);
   }
}
