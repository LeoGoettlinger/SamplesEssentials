package com.viaversion.viabackwards.api.entities.storage;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;

public class PlayerPosRotStorage extends PlayerPositionStorage {
   private float yRot;
   private float xRot;

   public float yRot() {
      return this.yRot;
   }

   public void setYRot(float yRot) {
      this.yRot = yRot;
   }

   public float xRot() {
      return this.xRot;
   }

   public void setXRot(float xRot) {
      this.xRot = xRot;
   }

   public void setRotation(float yRot, float xRot) {
      this.yRot = yRot;
      this.xRot = xRot;
   }

   public void setRotFromPacket(PacketWrapper wrapper) {
      this.yRot = (Float)wrapper.passthrough(Types.FLOAT);
      this.xRot = (Float)wrapper.passthrough(Types.FLOAT);
   }

   public void setPosRotFromPacket(PacketWrapper wrapper) {
      this.setPosFromPacket(wrapper);
      this.setRotFromPacket(wrapper);
   }
}
