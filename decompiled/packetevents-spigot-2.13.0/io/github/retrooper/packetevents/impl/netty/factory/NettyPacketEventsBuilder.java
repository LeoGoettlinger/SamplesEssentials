package io.github.retrooper.packetevents.impl.netty.factory;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.injector.ChannelInjector;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.netty.NettyManager;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import io.github.retrooper.packetevents.impl.netty.BuildData;
import io.github.retrooper.packetevents.impl.netty.NettyManagerImpl;
import io.github.retrooper.packetevents.impl.netty.manager.player.PlayerManagerAbstract;
import io.github.retrooper.packetevents.impl.netty.manager.server.ServerManagerAbstract;
import java.util.Locale;

public class NettyPacketEventsBuilder {
   private static PacketEventsAPI<BuildData> INSTANCE;

   public static void clearBuildCache() {
      INSTANCE = null;
   }

   public static PacketEventsAPI<BuildData> build(
      BuildData data, ChannelInjector injector, ProtocolManager protocolManager, ServerManagerAbstract serverManager, PlayerManagerAbstract playerManager
   ) {
      if (INSTANCE == null) {
         INSTANCE = buildNoCache(data, injector, protocolManager, serverManager, playerManager);
      }

      return INSTANCE;
   }

   public static PacketEventsAPI<BuildData> build(
      BuildData data,
      ChannelInjector injector,
      ProtocolManager protocolManager,
      ServerManagerAbstract serverManager,
      PlayerManagerAbstract playerManager,
      PacketEventsSettings settings
   ) {
      if (INSTANCE == null) {
         INSTANCE = buildNoCache(data, injector, protocolManager, serverManager, playerManager, settings);
      }

      return INSTANCE;
   }

   public static PacketEventsAPI<BuildData> buildNoCache(
      BuildData data, ChannelInjector injector, ProtocolManager protocolManager, ServerManagerAbstract serverManager, PlayerManagerAbstract playerManager
   ) {
      return buildNoCache(data, injector, protocolManager, serverManager, playerManager, new PacketEventsSettings());
   }

   public static PacketEventsAPI<BuildData> buildNoCache(
      BuildData data,
      ChannelInjector injector,
      ProtocolManager protocolManager,
      ServerManagerAbstract serverManager,
      PlayerManagerAbstract playerManager,
      PacketEventsSettings inSettings
   ) {
      return new PacketEventsAPI<BuildData>() {
         private final PacketEventsSettings settings = inSettings;
         private final NettyManager nettyManager = new NettyManagerImpl();
         private boolean loaded;
         private boolean initialized;
         private boolean terminated;

         @Override
         public void load() {
            if (!this.loaded) {
               String id = data.getName().toLowerCase(Locale.ROOT);
               PacketEvents.IDENTIFIER = "pe-" + id;
               PacketEvents.ENCODER_NAME = "pe-encoder-" + id;
               PacketEvents.DECODER_NAME = "pe-decoder-" + id;
               PacketEvents.CONNECTION_HANDLER_NAME = "pe-connection-handler-" + id;
               PacketEvents.SERVER_CHANNEL_HANDLER_NAME = "pe-connection-initializer-" + id;
               PacketEvents.TIMEOUT_HANDLER_NAME = "pe-timeout-handler-" + id;
               super.load();
               injector.inject();
               this.loaded = true;
               this.getLogManager().info("Loaded packetevents v2.13.0" + ("packetevents".equals(id) ? "" : " for " + data.getName()));
            }
         }

         @Override
         public boolean isLoaded() {
            return this.loaded;
         }

         @Override
         public void init() {
            this.load();
            if (!this.initialized) {
               if (this.settings.shouldCheckForUpdates()) {
                  this.getUpdateChecker().handleUpdateCheck();
               }

               PacketType.Play.Client.load();
               PacketType.Play.Server.load();
               this.initialized = true;
            }
         }

         @Override
         public boolean isInitialized() {
            return this.initialized;
         }

         @Override
         public void terminate() {
            if (this.initialized) {
               super.terminate();
               this.initialized = false;
               this.terminated = true;
            }
         }

         @Override
         public boolean isTerminated() {
            return this.terminated;
         }

         public BuildData getPlugin() {
            return data;
         }

         @Override
         public ProtocolManager getProtocolManager() {
            return protocolManager;
         }

         @Override
         public ServerManager getServerManager() {
            return serverManager;
         }

         @Override
         public PlayerManager getPlayerManager() {
            return playerManager;
         }

         @Override
         public PacketEventsSettings getSettings() {
            return this.settings;
         }

         @Override
         public NettyManager getNettyManager() {
            return this.nettyManager;
         }

         @Override
         public ChannelInjector getInjector() {
            return injector;
         }
      };
   }
}
