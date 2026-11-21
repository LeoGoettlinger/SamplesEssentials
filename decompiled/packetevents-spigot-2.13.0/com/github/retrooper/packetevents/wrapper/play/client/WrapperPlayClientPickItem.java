package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;

@Obsolete
public class WrapperPlayClientPickItem extends PacketWrapper<WrapperPlayClientPickItem> {
   private int slot;

   public WrapperPlayClientPickItem(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientPickItem(int slot) {
      super(PacketType.Play.Client.PICK_ITEM);
      this.slot = slot;
   }

   @Override
   public void read() {
      this.slot = this.readVarInt();
   }

   @Override
   public void write() {
      this.writeVarInt(this.slot);
   }

   public void copy(WrapperPlayClientPickItem wrapper) {
      this.slot = wrapper.slot;
   }

   public int getSlot() {
      return this.slot;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }
}
