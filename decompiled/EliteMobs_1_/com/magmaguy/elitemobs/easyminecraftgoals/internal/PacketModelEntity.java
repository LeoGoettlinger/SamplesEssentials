package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface PacketModelEntity extends PacketEntityInterface {
   void sendLocationAndRotationPacket(Location var1, EulerAngle var2);

   void sendLocationAndRotationAndScalePacket(Location var1, EulerAngle var2, float var3);

   default void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scaleX, float scaleY, float scaleZ) {
      this.sendLocationAndRotationAndScalePacket(location, eulerAngle, scaleX);
   }

   AbstractPacketBundle generateLocationAndRotationAndScalePackets(AbstractPacketBundle var1, Location var2, EulerAngle var3, float var4);

   default AbstractPacketBundle generateLocationAndRotationAndScalePackets(
      AbstractPacketBundle packetBundle, Location location, EulerAngle eulerAngle, float scaleX, float scaleY, float scaleZ
   ) {
      return this.generateLocationAndRotationAndScalePackets(packetBundle, location, eulerAngle, scaleX);
   }

   default void initializeModel(Location location, int modelID) {
      throw new UnsupportedOperationException("Integer modelID not supported by this implementation.");
   }

   default void initializeModel(Location location, String modelID) {
      throw new UnsupportedOperationException("String modelID not supported by this implementation.");
   }

   void setScale(float var1);

   void setHorseLeatherArmorColor(Color var1);

   @Override
   boolean hasViewers();

   default void setBillboard(Billboard billboard) {
   }

   default void setBrightness(int blockLight, int skyLight) {
   }

   default void setViewRange(float range) {
   }

   default void setTeleportDuration(int ticks) {
   }

   default void setTransformation(Vector3f translation, Quaternionf leftRotation, Vector3f scale, int interpolationTicks) {
   }

   default void setItemModel(String modelID, Color tint) {
   }

   default void queueMetadataUpdate(AbstractPacketBundle packetBundle) {
   }
}
