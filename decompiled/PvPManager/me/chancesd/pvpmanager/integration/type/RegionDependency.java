package me.chancesd.pvpmanager.integration.type;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface RegionDependency extends Dependency {
   boolean canAttackAt(Player var1, Location var2);

   @Override
   default String onEnableMessage() {
      return this.getName() + " Found! Enabling Support For " + this.getName() + " Regions";
   }
}
