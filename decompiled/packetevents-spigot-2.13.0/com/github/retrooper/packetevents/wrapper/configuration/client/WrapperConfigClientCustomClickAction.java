package com.github.retrooper.packetevents.wrapper.configuration.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import org.jspecify.annotations.Nullable;

public class WrapperConfigClientCustomClickAction extends WrapperCommonClientCustomClickAction<WrapperConfigClientCustomClickAction> {
   public WrapperConfigClientCustomClickAction(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperConfigClientCustomClickAction(ResourceLocation id, @Nullable NBT payload) {
      super(PacketType.Configuration.Client.CUSTOM_CLICK_ACTION, id, payload);
   }
}
