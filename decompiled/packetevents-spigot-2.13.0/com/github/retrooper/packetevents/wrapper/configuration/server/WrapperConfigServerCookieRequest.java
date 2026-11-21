package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperConfigServerCookieRequest extends PacketWrapper<WrapperConfigServerCookieRequest> {
   private ResourceLocation key;

   public WrapperConfigServerCookieRequest(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerCookieRequest(ResourceLocation key) {
      super(PacketType.Configuration.Server.COOKIE_REQUEST);
      this.key = key;
   }

   @Override
   public void read() {
      this.key = this.readIdentifier();
   }

   @Override
   public void write() {
      this.writeIdentifier(this.key);
   }

   public void copy(WrapperConfigServerCookieRequest wrapper) {
      this.key = wrapper.key;
   }

   public ResourceLocation getKey() {
      return this.key;
   }

   public void setKey(ResourceLocation key) {
      this.key = key;
   }
}
