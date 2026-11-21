package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.history.changeset.AbstractChangeSet;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class HistoryExtent extends AbstractDelegateExtent {
   private final MutableBlockVector3 mutable = new MutableBlockVector3();
   private AbstractChangeSet changeSet;

   public HistoryExtent(Extent extent, AbstractChangeSet changeSet) {
      super(extent);
      Preconditions.checkNotNull(changeSet);
      this.changeSet = changeSet;
   }

   public AbstractChangeSet getChangeSet() {
      return this.changeSet;
   }

   @Override
   public void setChangeSet(AbstractChangeSet fcs) {
      this.changeSet = fcs;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) throws WorldEditException {
      BaseBlock previous = this.getFullBlock(x, y, z);
      if (previous.getInternalId() == block.getInternalId() && !previous.hasNbtData() && block instanceof BaseBlock && !block.hasNbtData()) {
         return false;
      } else {
         this.changeSet.add(x, y, z, previous, block.toBaseBlock());
         return this.getExtent().setBlock(x, y, z, block);
      }
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      return this.setBlock(location.x(), location.y(), location.z(), block);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity state) {
      Entity entity = super.createEntity(location, state);
      if (state != null) {
         this.changeSet.addEntityCreate(FaweCompoundTag.of(state.getNbt()));
      }

      return entity;
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity state, UUID uuid) {
      Entity entity = super.createEntity(location, state, uuid);
      if (state != null) {
         this.changeSet.addEntityCreate(FaweCompoundTag.of(state.getNbt()));
      }

      return entity;
   }

   @Override
   public List<? extends Entity> getEntities() {
      return this.wrapEntities(super.getEntities());
   }

   @Override
   public List<? extends Entity> getEntities(Region region) {
      return this.wrapEntities(super.getEntities(region));
   }

   private List<? extends Entity> wrapEntities(List<? extends Entity> entities) {
      List<Entity> newList = new ArrayList<>(entities.size());

      for (Entity entity : entities) {
         newList.add(new HistoryExtent.TrackedEntity(entity));
      }

      return newList;
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType newBiome) {
      BiomeType oldBiome = this.getBiome(position);
      if (!oldBiome.id().equals(newBiome.id())) {
         this.changeSet.addBiomeChange(position.x(), position.y(), position.z(), oldBiome, newBiome);
         return this.getExtent().setBiome(position, newBiome);
      } else {
         return false;
      }
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType newBiome) {
      BiomeType oldBiome = this.getBiome(this.mutable.setComponents(x, y, z));
      if (!oldBiome.id().equals(newBiome.id())) {
         this.changeSet.addBiomeChange(x, y, z, oldBiome, newBiome);
         return this.getExtent().setBiome(x, y, z, newBiome);
      } else {
         return false;
      }
   }

   @Override
   public Extent disableHistory() {
      return this.getExtent();
   }

   public class TrackedEntity implements Entity {
      private final Entity entity;

      private TrackedEntity(Entity entity) {
         this.entity = entity;
      }

      @Override
      public BaseEntity getState() {
         return this.entity.getState();
      }

      @Override
      public Location getLocation() {
         return this.entity.getLocation();
      }

      @Override
      public Extent getExtent() {
         return this.entity.getExtent();
      }

      @Override
      public boolean remove() {
         BaseEntity state = this.entity.getState();
         boolean success = this.entity.remove();
         if (state != null && success) {
            HistoryExtent.this.changeSet.addEntityRemove(FaweCompoundTag.of(state.getNbt()));
         }

         return success;
      }

      @Nullable
      @Override
      public <T> T getFacet(Class<? extends T> cls) {
         return this.entity.getFacet(cls);
      }

      @Override
      public boolean setLocation(Location location) {
         return this.entity.setLocation(location);
      }
   }
}
