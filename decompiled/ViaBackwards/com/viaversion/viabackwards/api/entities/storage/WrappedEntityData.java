package com.viaversion.viabackwards.api.entities.storage;

import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public record WrappedEntityData(List<EntityData> entityDataList) {
   public boolean has(EntityData data) {
      return this.entityDataList.contains(data);
   }

   public void remove(EntityData data) {
      this.entityDataList.remove(data);
   }

   public void remove(int index) {
      this.entityDataList.removeIf(data -> data.id() == index);
   }

   public void add(EntityData data) {
      this.entityDataList.add(data);
   }

   @Nullable
   public EntityData get(int index) {
      for (EntityData data : this.entityDataList) {
         if (index == data.id()) {
            return data;
         }
      }

      return null;
   }
}
