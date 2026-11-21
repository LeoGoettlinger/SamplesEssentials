package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonCookieResponse;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayClientCookieResponse extends WrapperCommonCookieResponse<WrapperPlayClientCookieResponse> {
   @Deprecated
   public WrapperPlayClientCookieResponse(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayClientCookieResponse(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientCookieResponse(ResourceLocation key, @Nullable byte[] payload) {
      super(PacketType.Play.Client.COOKIE_RESPONSE, key, payload);
   }
}
