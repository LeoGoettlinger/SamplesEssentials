package com.sk89q.worldedit.session.request;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class RequestExtent implements Extent {
   private Request request;

   protected Extent getExtent() {
      if (this.request == null || !this.request.isValid()) {
         this.request = Request.request();
      }

      EditSession editSession = this.request.getEditSession();
      return (Extent)(editSession == null ? this.request.getWorld() : editSession);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.getExtent().getMinimumPoint();
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.getExtent().getMaximumPoint();
   }

   @Override
   public List<? extends Entity> getEntities(Region region) {
      return this.getExtent().getEntities(region);
   }

   @Override
   public List<? extends Entity> getEntities() {
      return this.getExtent().getEntities();
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      return this.getExtent().createEntity(location, entity);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      return this.getExtent().createEntity(location, entity, uuid);
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return this.getExtent().getBlock(position);
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return this.getExtent().getFullBlock(position);
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.getExtent().getBiome(position);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
      return this.getExtent().setBlock(position, block);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return this.getExtent().setBlock(x, y, z, block);
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return this.getExtent().fullySupports3DBiomes();
   }

   @Override
   public boolean tile(int x, int y, int z, FaweCompoundTag tile) throws WorldEditException {
      return this.getExtent().tile(x, y, z, tile);
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.getExtent().setBiome(position, biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.getExtent().setBiome(x, y, z, biome);
   }

   @Nullable
   @Override
   public Operation commit() {
      Operation commit = this.getExtent().commit();
      this.request = null;
      return commit;
   }
}
