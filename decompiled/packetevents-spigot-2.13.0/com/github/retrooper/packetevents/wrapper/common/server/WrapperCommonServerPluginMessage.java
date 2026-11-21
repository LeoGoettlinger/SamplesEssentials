package com.github.retrooper.packetevents.wrapper.common.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public abstract class WrapperCommonServerPluginMessage<T extends WrapperCommonServerPluginMessage<T>> extends PacketWrapper<T> {
   protected String channelName;
   protected byte[] data;

   protected WrapperCommonServerPluginMessage(PacketSendEvent event) {
      super(event);
   }

   protected WrapperCommonServerPluginMessage(PacketTypeCommon packetType) {
      super(packetType);
   }

   protected WrapperCommonServerPluginMessage(PacketTypeCommon packetType, ResourceLocation channelName, byte[] data) {
      this(packetType, channelName.toString(), data);
   }

   protected WrapperCommonServerPluginMessage(PacketTypeCommon packetType, String channelName, byte[] data) {
      super(packetType);
      this.channelName = channelName;
      this.data = data;
   }

   public void copy(T wrapper) {
      this.channelName = wrapper.channelName;
      this.data = wrapper.data;
   }

   public final String getChannelName() {
      return this.channelName;
   }

   public final void setChannelName(String channelName) {
      this.channelName = channelName;
   }

   public final byte[] getData() {
      return this.data;
   }

   public final void setData(byte[] data) {
      this.data = data;
   }
}
