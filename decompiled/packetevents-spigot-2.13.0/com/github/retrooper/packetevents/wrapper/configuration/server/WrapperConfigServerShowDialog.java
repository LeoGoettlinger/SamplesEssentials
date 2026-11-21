package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerShowDialog;

public class WrapperConfigServerShowDialog extends WrapperCommonServerShowDialog<WrapperConfigServerShowDialog> {
   public WrapperConfigServerShowDialog(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerShowDialog(Dialog dialog) {
      super(PacketType.Configuration.Server.SHOW_DIALOG, dialog);
   }

   @Override
   public void read() {
      this.dialog = Dialog.readDirect(this);
   }

   @Override
   public void write() {
      Dialog.writeDirect(this, this.dialog);
   }
}
