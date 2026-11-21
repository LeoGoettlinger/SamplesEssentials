package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerWorldBorderSize extends PacketWrapper<WrapperPlayServerWorldBorderSize> {
   private double diameter;

   public WrapperPlayServerWorldBorderSize(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerWorldBorderSize(double diameter) {
      super(PacketType.Play.Server.WORLD_BORDER_SIZE);
      this.diameter = diameter;
   }

   @Override
   public void read() {
      this.diameter = this.readDouble();
   }

   @Override
   public void write() {
      this.writeDouble(this.diameter);
   }

   public void copy(WrapperPlayServerWorldBorderSize packet) {
      this.diameter = packet.diameter;
   }

   public double getDiameter() {
      return this.diameter;
   }

   public void setDiameter(double diameter) {
      this.diameter = diameter;
   }
}
