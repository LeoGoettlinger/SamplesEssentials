package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerPluginMessage;

public class WrapperConfigServerPluginMessage extends WrapperCommonServerPluginMessage<WrapperConfigServerPluginMessage> {
   public WrapperConfigServerPluginMessage(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerPluginMessage(ResourceLocation channelName, byte[] data) {
      this(channelName.toString(), data);
   }

   public WrapperConfigServerPluginMessage(String channelName, byte[] data) {
      super(PacketType.Configuration.Server.PLUGIN_MESSAGE, channelName, data);
   }

   @Override
   public void read() {
      this.channelName = this.readString();
      if (ByteBufHelper.readableBytes(this.buffer) > 32767) {
         throw new RuntimeException("Payload may not be larger than 32767 bytes");
      } else {
         this.data = this.readRemainingBytes();
      }
   }

   @Override
   public void write() {
      this.writeString(this.channelName);
      this.writeBytes(this.data);
   }
}
