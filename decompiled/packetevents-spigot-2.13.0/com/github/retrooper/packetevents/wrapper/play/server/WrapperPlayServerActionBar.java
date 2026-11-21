package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;

public class WrapperPlayServerActionBar extends PacketWrapper<WrapperPlayServerActionBar> {
   private Component actionBarText;

   public WrapperPlayServerActionBar(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerActionBar(Component actionBarText) {
      super(PacketType.Play.Server.ACTION_BAR);
      this.actionBarText = actionBarText;
   }

   @Override
   public void read() {
      this.actionBarText = this.readComponent();
   }

   @Override
   public void write() {
      this.writeComponent(this.actionBarText);
   }

   public void copy(WrapperPlayServerActionBar wrapper) {
      this.actionBarText = wrapper.actionBarText;
   }

   public Component getActionBarText() {
      return this.actionBarText;
   }

   public void setActionBarText(Component actionBarText) {
      this.actionBarText = actionBarText;
   }
}
