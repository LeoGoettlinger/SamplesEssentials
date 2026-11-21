package com.github.retrooper.packetevents.wrapper.common.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperCommonServerShowDialog<T extends WrapperCommonServerShowDialog<T>> extends PacketWrapper<T> {
   protected Dialog dialog;

   public WrapperCommonServerShowDialog(PacketSendEvent event) {
      super(event);
   }

   public WrapperCommonServerShowDialog(PacketTypeCommon packetType, Dialog dialog) {
      super(packetType);
      this.dialog = dialog;
   }

   @Override
   public void read() {
      this.dialog = Dialog.read(this);
   }

   @Override
   public void write() {
      Dialog.write(this, this.dialog);
   }

   public void copy(T wrapper) {
      this.dialog = wrapper.getDialog();
   }

   public Dialog getDialog() {
      return this.dialog;
   }

   public void setDialog(Dialog dialog) {
      this.dialog = dialog;
   }
}
