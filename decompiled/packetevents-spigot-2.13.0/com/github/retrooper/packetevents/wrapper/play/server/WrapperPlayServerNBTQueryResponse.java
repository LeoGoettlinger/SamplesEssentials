package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerNBTQueryResponse extends PacketWrapper<WrapperPlayServerNBTQueryResponse> {
   private int transactionId;
   private NBTCompound tag;

   public WrapperPlayServerNBTQueryResponse(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerNBTQueryResponse(int transactionId, NBTCompound tag) {
      super(PacketType.Play.Server.NBT_QUERY_RESPONSE);
      this.transactionId = transactionId;
      this.tag = tag;
   }

   @Override
   public void read() {
      this.transactionId = this.readVarInt();
      this.tag = this.readNBT();
   }

   @Override
   public void write() {
      this.writeVarInt(this.transactionId);
      this.writeNBT(this.tag);
   }

   public void copy(WrapperPlayServerNBTQueryResponse wrapper) {
      this.transactionId = wrapper.transactionId;
      this.tag = wrapper.tag;
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public void setTransactionId(int transactionId) {
      this.transactionId = transactionId;
   }

   public NBTCompound getTag() {
      return this.tag;
   }

   public void setTag(NBTCompound tag) {
      this.tag = tag;
   }
}
