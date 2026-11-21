package com.github.retrooper.packetevents.wrapper.configuration.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonCookieResponse;
import org.jetbrains.annotations.Nullable;

public class WrapperConfigClientCookieResponse extends WrapperCommonCookieResponse<WrapperConfigClientCookieResponse> {
   @Deprecated
   public WrapperConfigClientCookieResponse(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigClientCookieResponse(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperConfigClientCookieResponse(ResourceLocation key, @Nullable byte[] payload) {
      super(PacketType.Configuration.Client.COOKIE_RESPONSE, key, payload);
   }
}
