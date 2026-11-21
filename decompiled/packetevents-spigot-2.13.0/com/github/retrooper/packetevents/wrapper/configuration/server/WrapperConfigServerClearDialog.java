package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerClearDialog;

public class WrapperConfigServerClearDialog extends WrapperCommonServerClearDialog<WrapperConfigServerClearDialog> {
   public WrapperConfigServerClearDialog(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerClearDialog() {
      super(PacketType.Configuration.Server.CLEAR_DIALOG);
   }
}
