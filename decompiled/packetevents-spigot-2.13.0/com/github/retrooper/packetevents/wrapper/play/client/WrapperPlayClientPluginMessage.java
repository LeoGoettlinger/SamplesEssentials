package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientPluginMessage;

public class WrapperPlayClientPluginMessage extends WrapperCommonClientPluginMessage<WrapperPlayClientPluginMessage> {
   public WrapperPlayClientPluginMessage(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientPluginMessage(ResourceLocation channelName, byte[] data) {
      this(channelName.toString(), data);
   }

   public WrapperPlayClientPluginMessage(String channelName, byte[] data) {
      super(PacketType.Play.Client.PLUGIN_MESSAGE, channelName, data);
   }

   @Override
   public void read() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
         this.channelName = this.readString();
      } else {
         this.channelName = this.readString(20);
      }

      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         short var1 = this.readShort();
      }

      if (ByteBufHelper.readableBytes(this.buffer) > 32767) {
         throw new RuntimeException("Payload may not be larger than 32767 bytes");
      } else {
         this.data = this.readRemainingBytes();
      }
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
         this.writeString(this.channelName);
      } else {
         this.writeString(this.channelName, 20);
      }

      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         this.writeShort(this.data.length);
      }

      this.writeBytes(this.data);
   }
}
