package com.skyblockexp.ezrtp.pvptag;

import org.bukkit.entity.Player;

public interface PvpTagProvider {
   String getName();

   boolean isAvailable();

   boolean isInCombat(Player var1);
}
