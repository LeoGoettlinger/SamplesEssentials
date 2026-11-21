package com.sk89q.worldedit.extent.clipboard;

import com.fastasyncworldedit.core.extent.clipboard.SimpleClipboard;
import com.fastasyncworldedit.core.function.visitor.Order;
import com.fastasyncworldedit.core.math.MutableBlockVector2;
import com.fastasyncworldedit.core.math.OffsetBlockVector3;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockArrayClipboard implements Clipboard {
   private final Region region;
   private final Clipboard parent;
   private final BlockVector3 offset;
   private BlockVector3 origin;

   public BlockArrayClipboard(Region region) {
      this(region, UUID.randomUUID());
   }

   public BlockArrayClipboard(SimpleClipboard parent, BlockVector3 offset) {
      this.parent = parent;
      Region shifted = parent.getRegion().clone();
      shifted.shift(offset);
      this.region = shifted;
      this.offset = shifted.getMinimumPoint();
      this.origin = parent.getOrigin().add(this.offset);
   }

   public BlockArrayClipboard(Region region, UUID clipboardId) {
      this(region, (SimpleClipboard)Clipboard.create(region, clipboardId));
   }

   public BlockArrayClipboard(Region region, SimpleClipboard parent) {
      Preconditions.checkNotNull(parent);
      Preconditions.checkNotNull(region);
      this.parent = parent;
      this.region = region.clone();
      this.offset = region.getMinimumPoint();
      this.origin = parent.getOrigin();
   }

   @Override
   public Region getRegion() {
      return this.region.clone();
   }

   @Override
   public BlockVector3 getOrigin() {
      return this.origin;
   }

   @Override
   public void setOrigin(BlockVector3 origin) {
      this.origin = origin;
      this.getParent().setOrigin(origin);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.region.getMinimumPoint();
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.region.getMaximumPoint();
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      if (this.region.contains(position)) {
         int x = position.x() - this.offset.x();
         int y = position.y() - this.offset.y();
         int z = position.z() - this.offset.z();
         return this.getParent().getBlock(x, y, z);
      } else {
         return BlockTypes.AIR.getDefaultState();
      }
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      if (this.region.contains(position)) {
         int x = position.x() - this.offset.x();
         int y = position.y() - this.offset.y();
         int z = position.z() - this.offset.z();
         return this.getParent().getFullBlock(x, y, z);
      } else {
         return BlockTypes.AIR.getDefaultState().toBaseBlock();
      }
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) {
      if (this.region.contains(position)) {
         int x = position.x();
         int y = position.y();
         int z = position.z();
         return this.setBlock(x, y, z, block);
      } else {
         return false;
      }
   }

   @Override
   public boolean tile(int x, int y, int z, FaweCompoundTag tag) {
      x -= this.offset.x();
      y -= this.offset.y();
      z -= this.offset.z();
      return this.getParent().tile(x, y, z, tag);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   public boolean setTile(BlockVector3 position, CompoundTag tag) {
      return this.tile(position.x(), position.y(), position.z(), FaweCompoundTag.of(tag.toLinTag()));
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) throws WorldEditException {
      x -= this.offset.x();
      y -= this.offset.y();
      z -= this.offset.z();
      return this.parent.setBlock(x, y, z, block);
   }

   @Override
   public boolean hasBiomes() {
      return this.getParent().hasBiomes();
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      if (!this.region.contains(position)) {
         return null;
      } else {
         int x = position.x() - this.offset.x();
         int y = position.y() - this.offset.y();
         int z = position.z() - this.offset.z();
         return this.getParent().getBiomeType(x, y, z);
      }
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      int x = position.x() - this.offset.x();
      int y = position.y() - this.offset.y();
      int z = position.z() - this.offset.z();
      return this.getParent().setBiome(x, y, z, biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      x -= this.offset.x();
      y -= this.offset.y();
      z -= this.offset.z();
      return this.getParent().setBiome(x, y, z, biome);
   }

   @Override
   public List<? extends Entity> getEntities(Region region) {
      region = region.clone();
      region.shift(BlockVector3.ZERO.subtract(this.offset));
      return this.getParent()
         .getEntities(region)
         .stream()
         .map(
            e -> {
               if (e instanceof BlockArrayClipboard.ClipboardEntity ce) {
                  Location oldloc = ce.getLocation();
                  Location loc = new Location(
                     oldloc.getExtent(),
                     oldloc.x() + this.offset.x(),
                     oldloc.y() + this.offset.y(),
                     oldloc.z() + this.offset.z(),
                     oldloc.getYaw(),
                     oldloc.getPitch()
                  );
                  return new BlockArrayClipboard.ClipboardEntity(loc, ce.entity);
               } else {
                  return (Entity)e;
               }
            }
         )
         .collect(Collectors.toList());
   }

   @Override
   public List<? extends Entity> getEntities() {
      return this.getParent()
         .getEntities()
         .stream()
         .map(
            e -> {
               if (e instanceof BlockArrayClipboard.ClipboardEntity ce) {
                  Location oldloc = ce.getLocation();
                  Location loc = new Location(
                     oldloc.getExtent(),
                     oldloc.x() + this.offset.x(),
                     oldloc.y() + this.offset.y(),
                     oldloc.z() + this.offset.z(),
                     oldloc.getYaw(),
                     oldloc.getPitch()
                  );
                  return new BlockArrayClipboard.ClipboardEntity(loc, ce.entity);
               } else {
                  return (Entity)e;
               }
            }
         )
         .collect(Collectors.toList());
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      Location l = new Location(
         location.getExtent(),
         location.x() - this.offset.x(),
         location.y() - this.offset.y(),
         location.z() - this.offset.z(),
         location.getYaw(),
         location.getPitch()
      );
      return this.getParent().createEntity(l, entity);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      Location l = new Location(
         location.getExtent(),
         location.x() - this.offset.x(),
         location.y() - this.offset.y(),
         location.z() - this.offset.z(),
         location.getYaw(),
         location.getPitch()
      );
      return this.getParent().createEntity(l, entity, uuid);
   }

   @Override
   public void removeEntity(int x, int y, int z, UUID uuid) {
      x -= this.offset.x();
      y -= this.offset.y();
      z -= this.offset.z();
      this.getParent().removeEntity(x, y, z, uuid);
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      x -= this.offset.x();
      y -= this.offset.y();
      z -= this.offset.z();
      return this.getParent().getBlock(x, y, z);
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      x -= this.offset.x();
      y -= this.offset.y();
      z -= this.offset.z();
      return this.getParent().getFullBlock(x, y, z);
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      x -= this.offset.x();
      y -= this.offset.y();
      z -= this.offset.z();
      return this.getParent().getBiomeType(x, y, z);
   }

   @Nonnull
   @Override
   public Iterator<BlockVector3> iterator() {
      OffsetBlockVector3 mutable = new OffsetBlockVector3(this.offset);
      return Iterators.transform(this.getParent().iterator(), mutable::init);
   }

   @Override
   public Iterator<BlockVector2> iterator2d() {
      MutableBlockVector2 mutable = new MutableBlockVector2();
      return Iterators.transform(this.getParent().iterator2d(), input -> mutable.setComponents(input.x() + this.offset.x(), input.z() + this.offset.z()));
   }

   @Override
   public Iterator<BlockVector3> iterator(Order order) {
      OffsetBlockVector3 mutable = new OffsetBlockVector3(this.offset);
      return Iterators.transform(this.getParent().iterator(order), mutable::init);
   }

   @Override
   public BlockVector3 getDimensions() {
      return this.parent.getDimensions();
   }

   @Override
   public void removeEntity(Entity entity) {
      this.parent.removeEntity(entity);
   }

   public Clipboard getParent() {
      return this.parent;
   }

   @Override
   public void close() {
      this.parent.close();
   }

   @Override
   public void flush() {
      this.parent.flush();
   }

   public static class ClipboardEntity implements Entity {
      private final BaseEntity entity;
      private final Clipboard clipboard;
      private final double x;
      private final double y;
      private final double z;
      private final float yaw;
      private final float pitch;

      public ClipboardEntity(Location loc, BaseEntity entity) {
         this((Clipboard)loc.getExtent(), loc.x(), loc.y(), loc.z(), loc.getYaw(), loc.getPitch(), entity);
      }

      public ClipboardEntity(Clipboard clipboard, double x, double y, double z, float yaw, float pitch, BaseEntity entity) {
         Preconditions.checkNotNull(entity);
         Preconditions.checkNotNull(clipboard);
         this.clipboard = clipboard;
         this.x = x;
         this.y = y;
         this.z = z;
         this.yaw = yaw;
         this.pitch = pitch;
         this.entity = new BaseEntity(entity);
      }

      @Override
      public boolean remove() {
         this.clipboard.removeEntity(this);
         return true;
      }

      @Nullable
      @Override
      public <T> T getFacet(Class<? extends T> cls) {
         return null;
      }

      BaseEntity getEntity() {
         return this.entity;
      }

      @Override
      public BaseEntity getState() {
         return new BaseEntity(this.entity);
      }

      @Override
      public Location getLocation() {
         return new Location(this.clipboard, this.x, this.y, this.z, this.yaw, this.pitch);
      }

      @Override
      public Extent getExtent() {
         return this.clipboard;
      }

      @Override
      public boolean setLocation(Location loc) {
         this.clipboard.removeEntity(this);
         Entity result = this.clipboard.createEntity(loc, this.entity);
         return result != null;
      }
   }
}
