package me.chancesd.pvpmanager.integration.type;

import org.bukkit.entity.Player;

public interface GroupDependency extends Dependency {
   boolean canAttack(Player var1, Player var2);
}
