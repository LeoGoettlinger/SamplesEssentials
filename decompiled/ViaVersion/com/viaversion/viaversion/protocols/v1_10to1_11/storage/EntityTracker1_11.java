package com.viaversion.viaversion.protocols.v1_10to1_11.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EntityTracker1_11 extends EntityTrackerBase {
   private final IntSet holograms = new IntOpenHashSet();

   public EntityTracker1_11(UserConnection user) {
      super(user, EntityTypes1_11.EntityType.PLAYER);
   }

   @Nullable
   @Override
   public TrackedEntity removeEntity(int entityId) {
      this.removeHologram(entityId);
      return super.removeEntity(entityId);
   }

   public boolean addHologram(int entId) {
      return this.holograms.add(entId);
   }

   public boolean isHologram(int entId) {
      return this.holograms.contains(entId);
   }

   public void removeHologram(int entId) {
      this.holograms.remove(entId);
   }
}
