package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PacketInteractionCallback {
   void accept(Player var1, PacketEntityInterface var2, PacketInteractionContext var3);
}
