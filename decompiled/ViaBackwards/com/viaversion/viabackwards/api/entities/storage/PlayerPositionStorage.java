package com.viaversion.viabackwards.api.entities.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;

public class PlayerPositionStorage implements StorableObject {
   private double x;
   private double y;
   private double z;

   public double x() {
      return this.x;
   }

   public double y() {
      return this.y;
   }

   public double z() {
      return this.z;
   }

   public void setX(double x) {
      this.x = x;
   }

   public void setY(double y) {
      this.y = y;
   }

   public void setZ(double z) {
      this.z = z;
   }

   public void setPosition(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void setPosFromPacket(PacketWrapper wrapper) {
      this.x = (Double)wrapper.passthrough(Types.DOUBLE);
      this.y = (Double)wrapper.passthrough(Types.DOUBLE);
      this.z = (Double)wrapper.passthrough(Types.DOUBLE);
   }

   public void addRelativePosition(double relX, double relY, double relZ) {
      this.x += relX;
      this.y += relY;
      this.z += relZ;
   }
}
