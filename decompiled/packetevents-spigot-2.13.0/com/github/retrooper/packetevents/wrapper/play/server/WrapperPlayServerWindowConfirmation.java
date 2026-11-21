package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerWindowConfirmation extends PacketWrapper<WrapperPlayServerWindowConfirmation> {
   private int windowId;
   private short actionId;
   private boolean accepted;

   public WrapperPlayServerWindowConfirmation(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerWindowConfirmation(int windowId, short actionId, boolean accepted) {
      super(PacketType.Play.Server.WINDOW_CONFIRMATION);
      this.windowId = windowId;
      this.actionId = actionId;
      this.accepted = accepted;
   }

   @Override
   public void read() {
      this.windowId = this.readUnsignedByte();
      this.actionId = this.readShort();
      this.accepted = this.readBoolean();
   }

   @Override
   public void write() {
      this.writeByte(this.windowId);
      this.writeShort(this.actionId);
      this.writeBoolean(this.accepted);
   }

   public void copy(WrapperPlayServerWindowConfirmation wrapper) {
      this.windowId = wrapper.getWindowId();
      this.actionId = wrapper.getActionId();
      this.accepted = wrapper.isAccepted();
   }

   public int getWindowId() {
      return this.windowId;
   }

   public void setWindowId(int windowID) {
      this.windowId = windowID;
   }

   public short getActionId() {
      return this.actionId;
   }

   public void setActionId(short actionID) {
      this.actionId = actionID;
   }

   public boolean isAccepted() {
      return this.accepted;
   }

   public void setAccepted(boolean accepted) {
      this.accepted = accepted;
   }
}
