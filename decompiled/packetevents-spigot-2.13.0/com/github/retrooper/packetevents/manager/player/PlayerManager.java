package com.github.retrooper.packetevents.manager.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public interface PlayerManager {
   int getPing(@NotNull Object player);

   @NotNull
   ClientVersion getClientVersion(@NotNull Object player);

   @Nullable
   Object getChannel(@NotNull Object player);

   @Nullable
   User getUser(@NotNull Object player);

   @Obsolete
   default ConnectionState getConnectionState(@NotNull Object player) throws IllegalStateException {
      return this.getUser(player).getConnectionState();
   }

   default void sendPacket(@NotNull Object player, @NotNull Object byteBuf) {
      PacketEvents.getAPI().getProtocolManager().sendPacket(this.getChannel(player), byteBuf);
   }

   default void sendPacket(@NotNull Object player, @NotNull PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().sendPacket(this.getChannel(player), wrapper);
   }

   default void sendPacketSilently(@NotNull Object player, @NotNull Object byteBuf) {
      PacketEvents.getAPI().getProtocolManager().sendPacketSilently(this.getChannel(player), byteBuf);
   }

   default void sendPacketSilently(@NotNull Object player, @NotNull PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().sendPacketSilently(this.getChannel(player), wrapper);
   }

   default void writePacket(@NotNull Object player, @NotNull Object byteBuf) {
      PacketEvents.getAPI().getProtocolManager().writePacket(this.getChannel(player), byteBuf);
   }

   default void writePacket(@NotNull Object player, @NotNull PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().writePacket(this.getChannel(player), wrapper);
   }

   default void writePacketSilently(@NotNull Object player, @NotNull Object byteBuf) {
      PacketEvents.getAPI().getProtocolManager().writePacketSilently(this.getChannel(player), byteBuf);
   }

   default void writePacketSilently(@NotNull Object player, @NotNull PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().writePacketSilently(this.getChannel(player), wrapper);
   }

   default void receivePacket(Object player, Object byteBuf) {
      PacketEvents.getAPI().getProtocolManager().receivePacket(this.getChannel(player), byteBuf);
   }

   default void receivePacket(Object player, PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().receivePacket(this.getChannel(player), wrapper);
   }

   default void receivePacketSilently(Object player, Object byteBuf) {
      PacketEvents.getAPI().getProtocolManager().receivePacketSilently(this.getChannel(player), byteBuf);
   }

   default void receivePacketSilently(Object player, PacketWrapper<?> wrapper) {
      PacketEvents.getAPI().getProtocolManager().receivePacketSilently(this.getChannel(player), wrapper);
   }
}
