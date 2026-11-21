package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public interface PlatformMessageServiceProvider {
   int priority();

   boolean supports(Plugin var1);

   PlatformMessageService create(Plugin var1);
}
