package com.github.retrooper.packetevents.manager.server;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.Nullable;

public interface ServerManager {
   ServerVersion getVersion();

   default SystemOS getOS() {
      return SystemOS.getOS();
   }

   @Nullable
   default Object getRegistryCacheKey(User user, ClientVersion version) {
      return null;
   }
}
