package com.github.retrooper.packetevents.wrapper.common.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public abstract class WrapperCommonClientPluginMessage<T extends WrapperCommonClientPluginMessage<T>> extends PacketWrapper<T> {
   protected String channelName;
   protected byte[] data;

   protected WrapperCommonClientPluginMessage(PacketReceiveEvent event) {
      super(event);
   }

   protected WrapperCommonClientPluginMessage(PacketTypeCommon packetType) {
      super(packetType);
   }

   protected WrapperCommonClientPluginMessage(PacketTypeCommon packetType, ResourceLocation channelName, byte[] data) {
      this(packetType, channelName.toString(), data);
   }

   protected WrapperCommonClientPluginMessage(PacketTypeCommon packetType, String channelName, byte[] data) {
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
