package io.github.retrooper.packetevents.impl.netty.manager.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ProtocolVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.buffer.ByteBuf;

public abstract class ProtocolManagerAbstract implements ProtocolManager {
   @Override
   public abstract ProtocolVersion getPlatformVersion();

   @Override
   public void sendPacket(Object channel, Object byteBuf) {
      if (ChannelHelper.isOpen(channel)) {
         ChannelHelper.writeAndFlush(channel, byteBuf);
      } else {
         ((ByteBuf)byteBuf).release();
      }
   }

   @Override
   public void sendPacketSilently(Object channel, Object byteBuf) {
      if (ChannelHelper.isOpen(channel)) {
         ChannelHelper.writeAndFlushInContext(channel, PacketEvents.ENCODER_NAME, byteBuf);
      } else {
         ((ByteBuf)byteBuf).release();
      }
   }

   @Override
   public void writePacket(Object channel, Object byteBuf) {
      if (ChannelHelper.isOpen(channel)) {
         ChannelHelper.write(channel, byteBuf);
      } else {
         ((ByteBuf)byteBuf).release();
      }
   }

   @Override
   public void writePacketSilently(Object channel, Object byteBuf) {
      if (ChannelHelper.isOpen(channel)) {
         ChannelHelper.writeInContext(channel, PacketEvents.ENCODER_NAME, byteBuf);
      } else {
         ((ByteBuf)byteBuf).release();
      }
   }

   @Override
   public void receivePacket(Object channel, Object byteBuf) {
      if (ChannelHelper.isOpen(channel)) {
         ChannelHelper.fireChannelRead(channel, byteBuf);
      } else {
         ((ByteBuf)byteBuf).release();
      }
   }

   @Override
   public void receivePacketSilently(Object channel, Object byteBuf) {
      if (ChannelHelper.isOpen(channel)) {
         ChannelHelper.fireChannelReadInContext(channel, PacketEvents.ENCODER_NAME, byteBuf);
      } else {
         ((ByteBuf)byteBuf).release();
      }
   }

   @Override
   public ClientVersion getClientVersion(Object channel) {
      User user = this.getUser(channel);
      ClientVersion version = user.getClientVersion();
      if (version == null) {
         version = ClientVersion.getById(PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion());
      }

      return version;
   }
}
