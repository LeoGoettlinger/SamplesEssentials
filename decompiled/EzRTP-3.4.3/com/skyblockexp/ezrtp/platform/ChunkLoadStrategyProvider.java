package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public interface ChunkLoadStrategyProvider {
   int priority();

   boolean supports(Plugin var1);

   ChunkLoadStrategy create(Plugin var1);
}
