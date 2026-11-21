package com.viaversion.viaversion.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.viaversion.viaversion.VelocityPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ConnectionManager;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.ConnectionDetails;
import java.util.UUID;

public class ConnectionDetailsListener {
   private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("vv:proxy_details");

   @Subscribe
   public void onPostServerJoin(ServerPostConnectEvent event) {
      ConnectionManager manager = Via.getManager().getConnectionManager();
      UUID uuid = event.getPlayer().getUniqueId();
      UserConnection frontend = manager.getServerConnection(uuid);
      if (frontend != null) {
         ConnectionDetails.sendPlayerDetails(frontend, "vv:proxy_details");
      }

      UserConnection backend = manager.getClientConnection(uuid);
      if (backend != null) {
         ConnectionDetails.sendServerDetails(backend, "vv:proxy_details");
      }
   }

   @Subscribe
   public void onProxyInitialize(ProxyInitializeEvent event) {
      VelocityPlugin.PROXY.getChannelRegistrar().register(new ChannelIdentifier[]{CHANNEL});
   }

   @Subscribe
   public void onPluginMessage(PluginMessageEvent event) {
      if (CHANNEL.equals(event.getIdentifier())) {
         event.setResult(ForwardResult.handled());
      }
   }
}
