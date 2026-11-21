package com.sk89q.worldedit.extent;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class TracingExtent extends AbstractDelegateExtent {
   private final Set<BlockVector3> touchedLocations = Collections.newSetFromMap(BlockMap.create());
   private final SetMultimap<BlockVector3, TracingExtent.Action> failedActions = Multimaps.newSetMultimap(
      BlockMap.create(), () -> EnumSet.noneOf(TracingExtent.Action.class)
   );

   public TracingExtent(Extent extent) {
      super(extent);
   }

   public boolean isActive() {
      return !this.touchedLocations.isEmpty();
   }

   public Set<BlockVector3> getTouchedLocations() {
      return Collections.unmodifiableSet(this.touchedLocations);
   }

   public SetMultimap<BlockVector3, TracingExtent.Action> getFailedActions() {
      return Multimaps.unmodifiableSetMultimap(this.failedActions);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
      this.touchedLocations.add(location);
      boolean result = super.setBlock(location, block);
      if (!result) {
         this.failedActions.put(location, TracingExtent.Action.SET_BLOCK);
      }

      return result;
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      this.touchedLocations.add(position);
      boolean result = super.setBiome(position, biome);
      if (!result) {
         this.failedActions.put(position, TracingExtent.Action.SET_BIOME);
      }

      return result;
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      BlockVector3 blockVector3 = location.toVector().toBlockPoint();
      this.touchedLocations.add(blockVector3);
      Entity result = super.createEntity(location, entity);
      if (result == null) {
         this.failedActions.put(blockVector3, TracingExtent.Action.CREATE_ENTITY);
      }

      return result;
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      BlockVector3 blockVector3 = location.toVector().toBlockPoint();
      this.touchedLocations.add(blockVector3);
      Entity result = super.createEntity(location, entity, uuid);
      if (result == null) {
         this.failedActions.put(blockVector3, TracingExtent.Action.CREATE_ENTITY);
      }

      return result;
   }

   @Override
   public String toString() {
      return "TracingExtent{delegate=" + this.getExtent() + "}";
   }

   public static enum Action {
      SET_BLOCK,
      SET_BIOME,
      CREATE_ENTITY;
   }
}
