package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;
import com.skyblockexp.ezrtp.network.NetworkService;

public final class NetworkCoordinator {
   private final EzRtpPlugin plugin;
   private NetworkService networkService;

   public NetworkCoordinator(EzRtpPlugin plugin) {
      this.plugin = plugin;
   }

   public void reload(EzRtpConfiguration configuration) {
      this.shutdown();
      NetworkConfiguration networkConfiguration = configuration.getNetworkConfiguration();
      if (networkConfiguration != null && networkConfiguration.isEnabled() && networkConfiguration.isLobbyServer()) {
         this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, "BungeeCord");
         this.networkService = new NetworkService(this.plugin, networkConfiguration, this.plugin.getLogger());
      } else {
         this.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this.plugin, "BungeeCord");
      }
   }

   public void shutdown() {
      if (this.networkService != null) {
         this.networkService.shutdown();
         this.networkService = null;
      }

      this.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this.plugin, "BungeeCord");
   }

   public NetworkService getNetworkService() {
      return this.networkService;
   }
}
