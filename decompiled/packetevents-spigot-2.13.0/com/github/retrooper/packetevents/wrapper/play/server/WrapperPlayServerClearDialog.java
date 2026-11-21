package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerClearDialog;

public class WrapperPlayServerClearDialog extends WrapperCommonServerClearDialog<WrapperPlayServerClearDialog> {
   public WrapperPlayServerClearDialog(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerClearDialog() {
      super(PacketType.Play.Server.CLEAR_DIALOG);
   }
}
