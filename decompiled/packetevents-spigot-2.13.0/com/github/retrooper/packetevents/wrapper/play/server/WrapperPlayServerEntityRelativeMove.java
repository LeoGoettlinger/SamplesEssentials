package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerEntityRelativeMove extends PacketWrapper<WrapperPlayServerEntityRelativeMove> {
   private static double MODERN_DELTA_DIVISOR = 4096.0;
   private static double LEGACY_DELTA_DIVISOR = 32.0;
   private int entityID;
   private double deltaX;
   private double deltaY;
   private double deltaZ;
   private boolean onGround;

   public WrapperPlayServerEntityRelativeMove(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerEntityRelativeMove(int entityID, double deltaX, double deltaY, double deltaZ, boolean onGround) {
      super(PacketType.Play.Server.ENTITY_RELATIVE_MOVE);
      this.entityID = entityID;
      this.deltaX = deltaX;
      this.deltaY = deltaY;
      this.deltaZ = deltaZ;
      this.onGround = onGround;
   }

   @Override
   public void read() {
      this.entityID = this.readVarInt();
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.deltaX = this.readShort() / MODERN_DELTA_DIVISOR;
         this.deltaY = this.readShort() / MODERN_DELTA_DIVISOR;
         this.deltaZ = this.readShort() / MODERN_DELTA_DIVISOR;
      } else {
         this.deltaX = this.readByte() / LEGACY_DELTA_DIVISOR;
         this.deltaY = this.readByte() / LEGACY_DELTA_DIVISOR;
         this.deltaZ = this.readByte() / LEGACY_DELTA_DIVISOR;
      }

      this.onGround = this.readBoolean();
   }

   @Override
   public void write() {
      this.writeVarInt(this.entityID);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.writeShort((short)(this.deltaX * MODERN_DELTA_DIVISOR));
         this.writeShort((short)(this.deltaY * MODERN_DELTA_DIVISOR));
         this.writeShort((short)(this.deltaZ * MODERN_DELTA_DIVISOR));
      } else {
         this.writeByte((byte)(this.deltaX * LEGACY_DELTA_DIVISOR));
         this.writeByte((byte)(this.deltaY * LEGACY_DELTA_DIVISOR));
         this.writeByte((byte)(this.deltaZ * LEGACY_DELTA_DIVISOR));
      }

      this.writeBoolean(this.onGround);
   }

   public void copy(WrapperPlayServerEntityRelativeMove wrapper) {
      this.entityID = wrapper.entityID;
      this.deltaX = wrapper.deltaX;
      this.deltaY = wrapper.deltaY;
      this.deltaZ = wrapper.deltaZ;
      this.onGround = wrapper.onGround;
   }

   public int getEntityId() {
      return this.entityID;
   }

   public void setEntityId(int entityID) {
      this.entityID = entityID;
   }

   public double getDeltaX() {
      return this.deltaX;
   }

   public void setDeltaX(double deltaX) {
      this.deltaX = deltaX;
   }

   public double getDeltaY() {
      return this.deltaY;
   }

   public void setDeltaY(double deltaY) {
      this.deltaY = deltaY;
   }

   public double getDeltaZ() {
      return this.deltaZ;
   }

   public void setDeltaZ(double deltaZ) {
      this.deltaZ = deltaZ;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }
}
