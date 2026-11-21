package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientGenerateStructure extends PacketWrapper<WrapperPlayClientGenerateStructure> {
   private Vector3i blockPosition;
   private int levels;
   private boolean keepJigsaws;

   public WrapperPlayClientGenerateStructure(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientGenerateStructure(Vector3i blockPosition, int levels, boolean keepJigsaws) {
      super(PacketType.Play.Client.GENERATE_STRUCTURE);
      this.blockPosition = blockPosition;
      this.levels = levels;
      this.keepJigsaws = keepJigsaws;
   }

   @Override
   public void read() {
      this.blockPosition = new Vector3i(this.readLong(), this.serverVersion);
      this.levels = this.readVarInt();
      this.keepJigsaws = this.readBoolean();
   }

   @Override
   public void write() {
      this.writeLong(this.blockPosition.getSerializedPosition(this.serverVersion));
      this.writeVarInt(this.levels);
      this.writeBoolean(this.keepJigsaws);
   }

   public void copy(WrapperPlayClientGenerateStructure wrapper) {
      this.blockPosition = wrapper.blockPosition;
      this.levels = wrapper.levels;
      this.keepJigsaws = wrapper.keepJigsaws;
   }

   public Vector3i getBlockPosition() {
      return this.blockPosition;
   }

   public void setBlockPosition(Vector3i blockPosition) {
      this.blockPosition = blockPosition;
   }

   public int getLevels() {
      return this.levels;
   }

   public void setLevels(int levels) {
      this.levels = levels;
   }

   public boolean isKeepingJigsaws() {
      return this.keepJigsaws;
   }

   public void setKeepJigsaws(boolean keepJigsaws) {
      this.keepJigsaws = keepJigsaws;
   }
}
