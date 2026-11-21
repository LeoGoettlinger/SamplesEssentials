package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientCloseWindow extends PacketWrapper<WrapperPlayClientCloseWindow> {
   private int windowID;

   public WrapperPlayClientCloseWindow(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientCloseWindow(int windowID) {
      super(PacketType.Play.Client.CLOSE_WINDOW);
      this.windowID = windowID;
   }

   @Override
   public void read() {
      this.windowID = this.readContainerId();
   }

   @Override
   public void write() {
      this.writeContainerId(this.windowID);
   }

   public void copy(WrapperPlayClientCloseWindow wrapper) {
      this.windowID = wrapper.windowID;
   }

   public int getWindowId() {
      return this.windowID;
   }

   public void setWindowId(int windowID) {
      this.windowID = windowID;
   }
}
