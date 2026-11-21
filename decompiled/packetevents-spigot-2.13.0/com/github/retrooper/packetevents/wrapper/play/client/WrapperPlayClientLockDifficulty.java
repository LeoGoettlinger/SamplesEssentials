package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientLockDifficulty extends PacketWrapper<WrapperPlayClientLockDifficulty> {
   private boolean locked;

   public WrapperPlayClientLockDifficulty(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientLockDifficulty(boolean locked) {
      super(PacketType.Play.Client.LOCK_DIFFICULTY);
      this.locked = locked;
   }

   @Override
   public void read() {
      this.locked = this.readBoolean();
   }

   @Override
   public void write() {
      this.writeBoolean(this.locked);
   }

   public void copy(WrapperPlayClientLockDifficulty wrapper) {
      this.locked = wrapper.locked;
   }

   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean locked) {
      this.locked = locked;
   }
}
