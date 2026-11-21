package com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.storage;

import com.viaversion.viabackwards.api.entities.storage.EntityPositionStorage;
import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LinkedEntityStorage extends EntityPositionStorage implements StorableObject {
   private int[] entities;
   private int[] passengers;
   private Integer vehicleId;

   @Nullable
   public int[] entities() {
      return this.entities;
   }

   public void setEntities(int... entities) {
      this.entities = entities;
   }

   @Nullable
   public int[] passengers() {
      return this.passengers;
   }

   public void setPassengers(int... passengers) {
      this.passengers = passengers;
   }

   @Nullable
   public Integer vehicleId() {
      return this.vehicleId;
   }

   public boolean isVehicle(int vehicleId) {
      return this.vehicleId != null && this.vehicleId == vehicleId;
   }

   public void setVehicleId(@Nullable Integer vehicleId) {
      this.vehicleId = vehicleId;
   }

   public void remove(UserConnection connection) {
      PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_19_3.REMOVE_ENTITIES, connection);
      wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, this.entities);
      wrapper.send(Protocol1_19_4To1_19_3.class);
      this.entities = null;
   }
}
