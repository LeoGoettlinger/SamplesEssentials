package com.skyblockexp.ezrtp.config.gui;

import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;

public final class GuiServerOption {
   private final NetworkConfiguration.NetworkServer server;

   GuiServerOption(NetworkConfiguration.NetworkServer server) {
      this.server = server;
   }

   public NetworkConfiguration.NetworkServer getServer() {
      return this.server;
   }
}
