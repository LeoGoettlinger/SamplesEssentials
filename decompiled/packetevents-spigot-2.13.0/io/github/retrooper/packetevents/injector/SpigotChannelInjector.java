package io.github.retrooper.packetevents.injector;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.injector.ChannelInjector;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.reflection.ReflectionObject;
import io.github.retrooper.packetevents.injector.connection.ServerChannelHandler;
import io.github.retrooper.packetevents.injector.connection.ServerConnectionInitializer;
import io.github.retrooper.packetevents.injector.handlers.PacketEventsDecoder;
import io.github.retrooper.packetevents.injector.handlers.PacketEventsEncoder;
import io.github.retrooper.packetevents.util.InjectedList;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

@Internal
public class SpigotChannelInjector implements ChannelInjector {
   public final Set<Channel> injectedConnectionChannels = new HashSet<>();
   public List<Object> networkManagers;
   private int connectionChannelsListIndex = -1;

   public void updatePlayer(User user, Object player) {
      Object channel = user.getChannel();
      if (channel == null) {
         channel = PacketEvents.getAPI().getPlayerManager().getChannel(player);
      }

      this.setPlayer(channel, player);
   }

   @Override
   public boolean isPlayerSet(Object channel) {
      if (channel == null) {
         return false;
      } else {
         PacketEventsEncoder encoder = this.getEncoder((Channel)channel);
         if (encoder != null && encoder.player != null) {
            return true;
         } else {
            PacketEventsDecoder decoder = this.getDecoder((Channel)channel);
            return decoder != null && decoder.player != null;
         }
      }
   }

   @Override
   public boolean isServerBound() {
      Object serverConnection = SpigotReflectionUtil.getMinecraftServerConnectionInstance();
      if (serverConnection != null) {
         ReflectionObject reflectServerConnection = new ReflectionObject(serverConnection);

         for (int i = 0; i < 2; i++) {
            for (Object value : reflectServerConnection.readList(i)) {
               if (value instanceof ChannelFuture) {
                  this.connectionChannelsListIndex = i;
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Override
   public void inject() {
      Object serverConnection = SpigotReflectionUtil.getMinecraftServerConnectionInstance();
      if (serverConnection != null) {
         ReflectionObject reflectServerConnection = new ReflectionObject(serverConnection);
         List<ChannelFuture> connectionChannelFutures = reflectServerConnection.readList(this.connectionChannelsListIndex);
         InjectedList<ChannelFuture> wrappedList = new InjectedList<>(connectionChannelFutures, future -> {
            Channel channelx = future.channel();
            this.injectServerChannel(channelx);
            this.injectedConnectionChannels.add(channelx);
         });
         reflectServerConnection.writeList(this.connectionChannelsListIndex, wrappedList);
         if (this.networkManagers == null) {
            this.networkManagers = SpigotReflectionUtil.getNetworkManagers();
         }

         synchronized (this.networkManagers) {
            if (!this.networkManagers.isEmpty()) {
               PacketEvents.getAPI().getLogManager().debug("Late bind not enabled, injecting into existing channel");
            }

            for (Object networkManager : this.networkManagers) {
               ReflectionObject networkManagerWrapper = new ReflectionObject(networkManager);
               Channel channel = networkManagerWrapper.readObject(0, Channel.class);
               if (channel != null) {
                  try {
                     ServerConnectionInitializer.initChannel(channel, ConnectionState.PLAY);
                  } catch (Exception var12) {
                     PacketEvents.getAPI()
                        .getLogManager()
                        .severe(
                           "PacketEvents Spigot injector failed to inject into an existing channel. If you need assistance, join our Discord server: https://discord.gg/DVHxPPxHZc",
                           var12
                        );
                  }
               }
            }
         }
      }
   }

   @Override
   public void uninject() {
      for (Channel connectionChannel : this.injectedConnectionChannels) {
         this.uninjectServerChannel(connectionChannel);
      }

      this.injectedConnectionChannels.clear();
      Object serverConnection = SpigotReflectionUtil.getMinecraftServerConnectionInstance();
      if (serverConnection != null) {
         ReflectionObject reflectServerConnection = new ReflectionObject(serverConnection);
         List<ChannelFuture> connectionChannelFutures = reflectServerConnection.readList(this.connectionChannelsListIndex);
         if (connectionChannelFutures instanceof InjectedList) {
            reflectServerConnection.writeList(this.connectionChannelsListIndex, ((InjectedList)connectionChannelFutures).originalList());
         }
      }
   }

   private void injectServerChannel(Channel serverChannel) {
      ChannelPipeline pipeline = serverChannel.pipeline();
      ChannelHandler connectionHandler = pipeline.get(PacketEvents.CONNECTION_HANDLER_NAME);
      if (connectionHandler != null) {
         pipeline.remove(PacketEvents.CONNECTION_HANDLER_NAME);
      }

      if (pipeline.get("SpigotNettyServerChannelHandler#0") != null) {
         pipeline.addAfter("SpigotNettyServerChannelHandler#0", PacketEvents.CONNECTION_HANDLER_NAME, new ServerChannelHandler());
      } else if (pipeline.get("floodgate-init") != null) {
         pipeline.addAfter("floodgate-init", PacketEvents.CONNECTION_HANDLER_NAME, new ServerChannelHandler());
      } else if (pipeline.get("MinecraftPipeline#0") != null) {
         pipeline.addAfter("MinecraftPipeline#0", PacketEvents.CONNECTION_HANDLER_NAME, new ServerChannelHandler());
      } else {
         pipeline.addFirst(PacketEvents.CONNECTION_HANDLER_NAME, new ServerChannelHandler());
      }

      if (this.networkManagers == null) {
         this.networkManagers = SpigotReflectionUtil.getNetworkManagers();
      }

      synchronized (this.networkManagers) {
         for (Object networkManager : this.networkManagers) {
            ReflectionObject networkManagerWrapper = new ReflectionObject(networkManager);
            Channel channel = networkManagerWrapper.readObject(0, Channel.class);
            if (channel != null && channel.isOpen() && channel.localAddress().equals(serverChannel.localAddress())) {
               channel.close();
            }
         }
      }
   }

   private void uninjectServerChannel(Channel serverChannel) {
      if (serverChannel.pipeline().get(PacketEvents.CONNECTION_HANDLER_NAME) != null) {
         serverChannel.pipeline().remove(PacketEvents.CONNECTION_HANDLER_NAME);
      } else {
         PacketEvents.getAPI().getLogManager().warn("Failed to uninject server channel, handler not found");
      }
   }

   @Override
   public void updateUser(Object channel, User user) {
      PacketEventsEncoder encoder = this.getEncoder((Channel)channel);
      if (encoder != null) {
         encoder.user = user;
      }

      PacketEventsDecoder decoder = this.getDecoder((Channel)channel);
      if (decoder != null) {
         decoder.user = user;
      }
   }

   @Override
   public void setPlayer(Object channel, Object player) {
      PacketEventsEncoder encoder = this.getEncoder((Channel)channel);
      if (encoder != null) {
         encoder.player = (Player)player;
      }

      PacketEventsDecoder decoder = this.getDecoder((Channel)channel);
      if (decoder != null) {
         decoder.player = (Player)player;
         decoder.user.getProfile().setName(((Player)player).getName());
         decoder.user.getProfile().setUUID(((Player)player).getUniqueId());
      }
   }

   @Nullable
   public PacketEventsEncoder getEncoder(Channel channel) {
      return (PacketEventsEncoder)channel.pipeline().get(PacketEvents.ENCODER_NAME);
   }

   @Nullable
   public PacketEventsDecoder getDecoder(Channel channel) {
      return (PacketEventsDecoder)channel.pipeline().get(PacketEvents.DECODER_NAME);
   }

   @Override
   public boolean isProxy() {
      return false;
   }
}
