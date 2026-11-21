package com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.storage;

import com.viaversion.viabackwards.protocol.v1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_3;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityTracker1_19_4 extends EntityTrackerBase {
   private final IntSet generatedEntities = new IntOpenHashSet();

   public EntityTracker1_19_4(UserConnection connection) {
      super(connection, EntityTypes1_19_4.PLAYER);
   }

   public int spawnEntity(EntityTypes1_19_3 entityType, double x, double y, double z, int data) {
      int entityId = this.nextEntityId();
      PacketWrapper addEntity = PacketWrapper.create(ClientboundPackets1_19_3.ADD_ENTITY, this.user());
      addEntity.write(Types.VAR_INT, entityId);
      addEntity.write(Types.UUID, UUID.randomUUID());
      addEntity.write(Types.VAR_INT, entityType.getId());
      addEntity.write(Types.DOUBLE, x);
      addEntity.write(Types.DOUBLE, y);
      addEntity.write(Types.DOUBLE, z);
      addEntity.write(Types.BYTE, (byte)0);
      addEntity.write(Types.BYTE, (byte)0);
      addEntity.write(Types.BYTE, (byte)0);
      addEntity.write(Types.VAR_INT, data);
      addEntity.write(Types.SHORT, (short)0);
      addEntity.write(Types.SHORT, (short)0);
      addEntity.write(Types.SHORT, (short)0);
      addEntity.send(Protocol1_19_4To1_19_3.class);
      return entityId;
   }

   @Nullable
   public TrackedEntity removeEntity(int id) {
      TrackedEntity entity = super.removeEntity(id);
      if (entity != null) {
         this.clearLinkedEntities((LinkedEntityStorage)entity.get(LinkedEntityStorage.class));
      }

      return entity;
   }

   public void clearLinkedEntities(LinkedEntityStorage storage) {
      if (storage != null && storage.entities() != null) {
         for (int entity : storage.entities()) {
            this.generatedEntities.remove(entity);
         }

         storage.remove(this.user());
      }
   }

   @Nullable
   public LinkedEntityStorage linkedEntityStorage(int id) {
      TrackedEntity entity = this.entity(id);
      return entity != null ? (LinkedEntityStorage)entity.get(LinkedEntityStorage.class) : null;
   }

   private int nextEntityId() {
      int entityId = ThreadLocalRandom.current().nextInt(-1000000, -1);
      return !this.generatedEntities.add(entityId) ? this.nextEntityId() : entityId;
   }
}
