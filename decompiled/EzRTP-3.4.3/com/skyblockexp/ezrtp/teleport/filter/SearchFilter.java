package com.skyblockexp.ezrtp.teleport.filter;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import org.bukkit.Location;

@FunctionalInterface
public interface SearchFilter {
   SearchFilterResult apply(Location var1, RandomTeleportSettings var2);
}
