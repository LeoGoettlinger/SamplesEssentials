package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerTickingState extends PacketWrapper<WrapperPlayServerTickingState> {
   private float tickRate;
   private boolean frozen;

   public WrapperPlayServerTickingState(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerTickingState(float tickRate, boolean frozen) {
      super(PacketType.Play.Server.TICKING_STATE);
      this.tickRate = tickRate;
      this.frozen = frozen;
   }

   @Override
   public void read() {
      this.tickRate = this.readFloat();
      this.frozen = this.readBoolean();
   }

   @Override
   public void write() {
      this.writeFloat(this.tickRate);
      this.writeBoolean(this.frozen);
   }

   public void copy(WrapperPlayServerTickingState wrapper) {
      this.tickRate = wrapper.tickRate;
      this.frozen = wrapper.frozen;
   }

   public float getTickRate() {
      return this.tickRate;
   }

   public void setTickRate(float tickRate) {
      this.tickRate = tickRate;
   }

   public boolean isFrozen() {
      return this.frozen;
   }

   public void setFrozen(boolean frozen) {
      this.frozen = frozen;
   }
}
