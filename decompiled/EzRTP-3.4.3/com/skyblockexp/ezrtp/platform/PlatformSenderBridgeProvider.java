package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public interface PlatformSenderBridgeProvider {
   int priority();

   boolean supports(Plugin var1);

   PlatformSenderBridge create(Plugin var1);
}
