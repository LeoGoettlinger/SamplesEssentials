package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public interface PlatformRuntimeProvider {
   int priority();

   boolean supports(Plugin var1);

   PlatformRuntime create(Plugin var1);
}
