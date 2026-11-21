package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.ItemStackSerialization;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientCreativeInventoryAction extends PacketWrapper<WrapperPlayClientCreativeInventoryAction> {
   private int slot;
   private ItemStack itemStack;

   public WrapperPlayClientCreativeInventoryAction(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientCreativeInventoryAction(int slot, ItemStack itemStack) {
      super(PacketType.Play.Client.CREATIVE_INVENTORY_ACTION);
      this.slot = slot;
      this.itemStack = itemStack;
   }

   @Override
   public void read() {
      this.slot = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_5) ? this.readUnsignedShort() : this.readShort();
      this.itemStack = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)
         ? ItemStackSerialization.readUntrusted(this)
         : ItemStackSerialization.read(this);
   }

   @Override
   public void write() {
      this.writeShort(this.slot);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
         ItemStackSerialization.writeUntrusted(this, this.itemStack);
      } else {
         ItemStackSerialization.write(this, this.itemStack);
      }
   }

   public void copy(WrapperPlayClientCreativeInventoryAction wrapper) {
      this.slot = wrapper.slot;
      this.itemStack = wrapper.itemStack;
   }

   public int getSlot() {
      return this.slot;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public void setItemStack(ItemStack itemStack) {
      this.itemStack = itemStack;
   }
}
