package com.github.retrooper.packetevents.wrapper.login.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonCookieResponse;
import org.jetbrains.annotations.Nullable;

public class WrapperLoginClientCookieResponse extends WrapperCommonCookieResponse<WrapperLoginClientCookieResponse> {
   @Deprecated
   public WrapperLoginClientCookieResponse(PacketSendEvent event) {
      super(event);
   }

   public WrapperLoginClientCookieResponse(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperLoginClientCookieResponse(ResourceLocation key, @Nullable byte[] payload) {
      super(PacketType.Login.Client.COOKIE_RESPONSE, key, payload);
   }
}
