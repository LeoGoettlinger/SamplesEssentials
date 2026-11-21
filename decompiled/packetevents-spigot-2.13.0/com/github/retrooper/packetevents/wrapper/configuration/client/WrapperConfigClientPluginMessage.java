package com.github.retrooper.packetevents.wrapper.configuration.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientPluginMessage;

public class WrapperConfigClientPluginMessage extends WrapperCommonClientPluginMessage<WrapperConfigClientPluginMessage> {
   public WrapperConfigClientPluginMessage(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperConfigClientPluginMessage(ResourceLocation channelName, byte[] data) {
      this(channelName.toString(), data);
   }

   public WrapperConfigClientPluginMessage(String channelName, byte[] data) {
      super(PacketType.Configuration.Client.PLUGIN_MESSAGE, channelName, data);
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
