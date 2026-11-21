package com.github.retrooper.packetevents.manager.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ProtocolVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.PacketTransformationUtil;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface ProtocolManager {
   @Internal
   Map<UUID, Object> CHANNELS = new ConcurrentHashMap<>();
   @Internal
   Map<Object, User> USERS = new ConcurrentHashMap<>();

   default Collection<User> getUsers() {
      return USERS.values();
   }

   default Collection<Object> getChannels() {
      return CHANNELS.values();
   }

   ProtocolVersion getPlatformVersion();

   void sendPacket(Object channel, Object byteBuf);

   void sendPacketSilently(Object channel, Object byteBuf);

   void writePacket(Object channel, Object byteBuf);

   void writePacketSilently(Object channel, Object byteBuf);

   void receivePacket(Object channel, Object byteBuf);

   void receivePacketSilently(Object channel, Object byteBuf);

   @Nullable
   ClientVersion getClientVersion(Object channel);

   default void sendPackets(Object channel, Object... byteBuf) {
      for (Object buf : byteBuf) {
         this.sendPacket(channel, buf);
      }
   }

   default void sendPacketsSilently(Object channel, Object... byteBuf) {
      for (Object buf : byteBuf) {
         this.sendPacketSilently(channel, buf);
      }
   }

   default void writePackets(Object channel, Object... byteBuf) {
      for (Object buf : byteBuf) {
         this.writePacket(channel, buf);
      }
   }

   default void writePacketsSilently(Object channel, Object... byteBuf) {
      for (Object buf : byteBuf) {
         this.writePacketSilently(channel, buf);
      }
   }

   default void receivePackets(Object channel, Object... byteBuf) {
      for (Object buf : byteBuf) {
         this.receivePacket(channel, buf);
      }
   }

   default void receivePacketsSilently(Object channel, Object... byteBuf) {
      for (Object buf : byteBuf) {
         this.receivePacketSilently(channel, buf);
      }
   }

   default void setClientVersion(Object channel, ClientVersion version) {
      this.getUser(channel).setClientVersion(version);
   }

   @Internal
   default Object[] transformWrappers(PacketWrapper<?> wrapper, Object channel, boolean outgoing) {
      PacketWrapper<?>[] wrappers = PacketTransformationUtil.transform(wrapper);
      Object[] buffers = new Object[wrappers.length];

      for (int i = 0; i < wrappers.length; i++) {
         PacketWrapper<?> wrappper = wrappers[i];
         synchronized (wrappper.bufferLock) {
            wrappper.prepareForSend(channel, outgoing);
            buffers[i] = wrappper.buffer;
            wrappper.buffer = null;
         }
      }

      return buffers;
   }

   default void sendPacket(Object channel, PacketWrapper<?> wrapper) {
      Object[] transformed = this.transformWrappers(wrapper, channel, true);
      this.sendPackets(channel, transformed);
   }

   default void sendPacketSilently(Object channel, PacketWrapper<?> wrapper) {
      Object[] transformed = this.transformWrappers(wrapper, channel, true);
      this.sendPacketsSilently(channel, transformed);
   }

   default void writePacket(Object channel, PacketWrapper<?> wrapper) {
      Object[] transformed = this.transformWrappers(wrapper, channel, true);
      this.writePackets(channel, transformed);
   }

   default void writePacketSilently(Object channel, PacketWrapper<?> wrapper) {
      Object[] transformed = this.transformWrappers(wrapper, channel, true);
      this.writePacketsSilently(channel, transformed);
   }

   default void receivePacket(Object channel, PacketWrapper<?> wrapper) {
      Object[] transformed = this.transformWrappers(wrapper, channel, false);
      this.receivePackets(channel, transformed);
   }

   default void receivePacketSilently(Object channel, PacketWrapper<?> wrapper) {
      Object[] transformed = this.transformWrappers(wrapper, channel, false);
      this.receivePacketsSilently(channel, transformed);
   }

   @Nullable
   default User getUser(Object channel) {
      Object pipeline = ChannelHelper.getPipeline(channel);
      return USERS.get(pipeline);
   }

   @Internal
   @Nullable
   default User removeUser(Object channel) {
      Object pipeline = ChannelHelper.getPipeline(channel);
      return USERS.remove(pipeline);
   }

   @Internal
   default void setUser(Object channel, User user) {
      synchronized (channel) {
         Object pipeline = ChannelHelper.getPipeline(channel);
         USERS.put(pipeline, user);
      }

      PacketEvents.getAPI().getInjector().updateUser(channel, user);
   }

   @Nullable
   default Object getChannel(UUID uuid) {
      return CHANNELS.get(uuid);
   }

   @Internal
   default void setChannel(UUID uuid, Object channel) {
      CHANNELS.put(uuid, channel);
   }

   @Internal
   default void removeChannel(Object channel) {
      CHANNELS.values().remove(channel);
   }

   @Internal
   default void removeChannelById(UUID uuid) {
      CHANNELS.remove(uuid);
   }

   default boolean hasChannel(Object channel) {
      return CHANNELS.containsValue(channel);
   }
}
