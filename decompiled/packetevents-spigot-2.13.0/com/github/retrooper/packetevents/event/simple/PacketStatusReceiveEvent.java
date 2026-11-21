package com.github.retrooper.packetevents.event.simple;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PacketStatusReceiveEvent extends PacketReceiveEvent {
   public PacketStatusReceiveEvent(Object channel, User user, @UnknownNullability Object player, Object rawByteBuf, boolean autoProtocolTranslation) throws PacketProcessException {
      super(channel, user, player, rawByteBuf, autoProtocolTranslation);
   }

   protected PacketStatusReceiveEvent(
      int packetId, PacketTypeCommon packetType, ServerVersion serverVersion, Object channel, User user, @UnknownNullability Object player, Object byteBuf
   ) throws PacketProcessException {
      super(packetId, packetType, serverVersion, channel, user, player, byteBuf);
   }

   public PacketStatusReceiveEvent clone() {
      Object clonedBuffer = ByteBufHelper.retainedDuplicate(this.getByteBuf());
      return new PacketStatusReceiveEvent(
         this.getPacketId(), this.getPacketType(), this.getServerVersion(), this.getChannel(), this.getUser(), this.getPlayer(), clonedBuffer
      );
   }

   public PacketType.Status.Client getPacketType() {
      return (PacketType.Status.Client)super.getPacketType();
   }
}
