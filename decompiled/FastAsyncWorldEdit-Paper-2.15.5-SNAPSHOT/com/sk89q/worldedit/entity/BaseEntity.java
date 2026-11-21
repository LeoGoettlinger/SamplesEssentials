package com.sk89q.worldedit.entity;

import com.google.common.base.Preconditions;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.NbtValued;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinTag;

public class BaseEntity implements NbtValued {
   private final EntityType type;
   @Nullable
   private LazyReference<LinCompoundTag> nbtData;

   @Deprecated
   public BaseEntity(EntityType type, CompoundTag nbtData) {
      this(type);
      this.setNbtData(nbtData);
   }

   public BaseEntity(EntityType type, LazyReference<LinCompoundTag> nbtData) {
      this(type);
      this.setNbtReference(nbtData);
   }

   public BaseEntity(EntityType type) {
      this.type = type;
   }

   public BaseEntity(BaseEntity other) {
      Preconditions.checkNotNull(other);
      this.type = other.getType();
      this.setNbtReference(other.getNbtReference());
   }

   @Nullable
   @Override
   public LazyReference<LinCompoundTag> getNbtReference() {
      return this.nbtData;
   }

   @Override
   public void setNbtReference(@Nullable LazyReference<LinCompoundTag> nbtData) {
      this.nbtData = nbtData;
   }

   public EntityType getType() {
      return this.type;
   }

   public BaseEntity(CompoundTag tag) {
      this(EntityTypes.parse(tag.getString("Id")), tag);
   }

   @Nullable
   public UUID getUUID() {
      LinCompoundTag tag = this.getNbt();
      if (tag == null) {
         return null;
      } else {
         Map<String, LinTag<?>> value = tag.value();
         if (value.get("UUID") instanceof LinIntArrayTag intArrayTag) {
            int[] arr = intArrayTag.value();
            return new UUID((long)arr[0] << 32 | arr[1] & 4294967295L, (long)arr[2] << 32 | arr[3] & 4294967295L);
         } else if (value.get("UUIDMost") instanceof LinLongTag longTag) {
            return new UUID(longTag.value(), ((LinLongTag)value.get("UUIDLeast")).value());
         } else if (value.get("WorldUUIDMost") instanceof LinLongTag longTag) {
            return new UUID(longTag.value(), ((LinLongTag)value.get("WorldUUIDLeast")).value());
         } else {
            return value.get("PersistentIDMSB") instanceof LinLongTag longTag
               ? new UUID(longTag.value(), ((LinLongTag)value.get("PersistentIDLSB")).value())
               : null;
         }
      }
   }
}
