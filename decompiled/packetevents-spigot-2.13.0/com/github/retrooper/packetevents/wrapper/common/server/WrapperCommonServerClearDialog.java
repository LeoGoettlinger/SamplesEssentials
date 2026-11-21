package com.github.retrooper.packetevents.wrapper.common.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public abstract class WrapperCommonServerClearDialog<T extends WrapperCommonServerClearDialog<T>> extends PacketWrapper<T> {
   public WrapperCommonServerClearDialog(PacketSendEvent event) {
      super(event);
   }

   public WrapperCommonServerClearDialog(PacketTypeCommon packetType) {
      super(packetType);
   }
}
