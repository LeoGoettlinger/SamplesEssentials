package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerClearTitles extends PacketWrapper<WrapperPlayServerClearTitles> {
   private boolean reset;

   public WrapperPlayServerClearTitles(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerClearTitles(boolean reset) {
      super(PacketType.Play.Server.CLEAR_TITLES);
      this.reset = reset;
   }

   @Override
   public void read() {
      this.reset = this.readBoolean();
   }

   public void copy(WrapperPlayServerClearTitles wrapper) {
      this.reset = wrapper.reset;
   }

   @Override
   public void write() {
      this.writeBoolean(this.reset);
   }

   public boolean getReset() {
      return this.reset;
   }

   public void setReset(boolean reset) {
      this.reset = reset;
   }
}
