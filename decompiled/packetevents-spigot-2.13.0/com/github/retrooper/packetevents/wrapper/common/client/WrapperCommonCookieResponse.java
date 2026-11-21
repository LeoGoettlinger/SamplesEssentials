package com.github.retrooper.packetevents.wrapper.common.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

public abstract class WrapperCommonCookieResponse<T extends WrapperCommonCookieResponse<T>> extends PacketWrapper<T> {
   public static final int MAX_PAYLOAD_SIZE = 5120;
   private ResourceLocation key;
   @Nullable
   private byte[] payload;

   @Deprecated
   public WrapperCommonCookieResponse(PacketSendEvent event) {
      super(event);
   }

   public WrapperCommonCookieResponse(PacketReceiveEvent event) {
      super(event);
   }

   protected WrapperCommonCookieResponse(PacketTypeCommon packetType, ResourceLocation key, @Nullable byte[] payload) {
      super(packetType);
      this.key = key;
      this.payload = payload;
   }

   @Override
   public void read() {
      this.key = this.readIdentifier();
      this.payload = this.readOptional(wrapper -> wrapper.readByteArray(5120));
   }

   @Override
   public void write() {
      this.writeIdentifier(this.key);
      this.writeOptional(this.payload, PacketWrapper::writeByteArray);
   }

   public void copy(T wrapper) {
      this.key = wrapper.getKey();
      this.payload = wrapper.getPayload();
   }

   public ResourceLocation getKey() {
      return this.key;
   }

   public void setKey(ResourceLocation key) {
      this.key = key;
   }

   @Nullable
   public byte[] getPayload() {
      return this.payload;
   }

   public void setPayload(@Nullable byte[] payload) {
      this.payload = payload;
   }
}
