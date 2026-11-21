package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerShowDialog;

public class WrapperPlayServerShowDialog extends WrapperCommonServerShowDialog<WrapperPlayServerShowDialog> {
   public WrapperPlayServerShowDialog(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerShowDialog(Dialog dialog) {
      super(PacketType.Play.Server.SHOW_DIALOG, dialog);
   }
}
