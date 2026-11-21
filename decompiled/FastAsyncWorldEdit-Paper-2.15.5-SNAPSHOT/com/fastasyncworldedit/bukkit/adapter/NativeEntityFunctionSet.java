package com.fastasyncworldedit.bukkit.adapter;

import com.sk89q.worldedit.entity.Entity;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class NativeEntityFunctionSet<NativeEntity, Result> extends AbstractSet<Result> {
   private final List<NativeEntity> nativeEntities;
   private final Function<NativeEntity, UUID> uuidGetter;
   private final Function<NativeEntity, Result> resultFunction;

   public NativeEntityFunctionSet(List<NativeEntity> nativeEntities, Function<NativeEntity, UUID> uuidGetter, Function<NativeEntity, Result> resultFunction) {
      this.nativeEntities = nativeEntities;
      this.uuidGetter = uuidGetter;
      this.resultFunction = resultFunction;
   }

   @Override
   public int size() {
      return this.nativeEntities.size();
   }

   @Override
   public boolean isEmpty() {
      return false;
   }

   @Override
   public boolean contains(Object get) {
      if (get instanceof Entity e) {
         UUID getUUID = e.getState().getUUID();

         for (NativeEntity entity : this.nativeEntities) {
            UUID uuid = this.uuidGetter.apply(entity);
            if (uuid.equals(getUUID)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public Iterator<Result> iterator() {
      return this.nativeEntities.stream().map(this.resultFunction).iterator();
   }
}
