package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public interface PlatformGuiBridgeProvider {
   int priority();

   boolean supports(Plugin var1);

   PlatformGuiBridge create(Plugin var1);
}
