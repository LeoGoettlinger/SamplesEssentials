package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientUpdateCommandBlockMinecart extends PacketWrapper<WrapperPlayClientUpdateCommandBlockMinecart> {
   private int entityId;
   private String command;
   private boolean trackOutput;

   public WrapperPlayClientUpdateCommandBlockMinecart(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientUpdateCommandBlockMinecart(int entityId, String command, boolean trackOutput) {
      super(PacketType.Play.Client.UPDATE_COMMAND_BLOCK_MINECART);
      this.entityId = entityId;
      this.command = command;
      this.trackOutput = trackOutput;
   }

   @Override
   public void read() {
      this.entityId = this.readVarInt();
      this.command = this.readString();
      this.trackOutput = this.readBoolean();
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityId);
      this.writeString(this.command);
      this.writeBoolean(this.trackOutput);
   }

   public void copy(WrapperPlayClientUpdateCommandBlockMinecart wrapper) {
      this.entityId = wrapper.entityId;
      this.command = wrapper.command;
      this.trackOutput = wrapper.trackOutput;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public String getCommand() {
      return this.command;
   }

   public void setCommand(String command) {
      this.command = command;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }

   public void setTrackOutput(boolean trackOutput) {
      this.trackOutput = trackOutput;
   }
}
