package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface CustomEntityViewerHook {
   void handle(CustomEntityHandle var1, Player var2);
}
