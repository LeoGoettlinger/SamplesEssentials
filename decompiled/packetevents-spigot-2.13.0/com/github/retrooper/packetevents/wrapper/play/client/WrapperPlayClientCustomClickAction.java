package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.common.client.WrapperCommonClientCustomClickAction;
import org.jspecify.annotations.Nullable;

public class WrapperPlayClientCustomClickAction extends WrapperCommonClientCustomClickAction<WrapperPlayClientCustomClickAction> {
   public WrapperPlayClientCustomClickAction(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientCustomClickAction(ResourceLocation id, @Nullable NBT payload) {
      super(PacketType.Play.Client.CUSTOM_CLICK_ACTION, id, payload);
   }
}
