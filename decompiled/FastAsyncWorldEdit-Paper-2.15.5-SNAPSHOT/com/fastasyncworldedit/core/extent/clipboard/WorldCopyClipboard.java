package com.fastasyncworldedit.core.extent.clipboard;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class WorldCopyClipboard extends ReadOnlyClipboard {
   private final boolean hasBiomes;
   private final boolean hasEntities;
   private final Extent extent;

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public WorldCopyClipboard(Supplier<Extent> supplier, Region region) {
      this(supplier, region, true, false);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public WorldCopyClipboard(Supplier<Extent> supplier, Region region, boolean hasEntities, boolean hasBiomes) {
      super(region);
      this.hasBiomes = hasBiomes;
      this.hasEntities = hasEntities;
      this.extent = supplier.get();
   }

   private WorldCopyClipboard(Extent extent, Region region, boolean hasEntities, boolean hasBiomes) {
      super(region);
      this.hasBiomes = hasBiomes;
      this.hasEntities = hasEntities;
      this.extent = extent;
   }

   public static WorldCopyClipboard of(Extent extent, Region region) {
      return of(extent, region, true);
   }

   public static WorldCopyClipboard of(Extent extent, Region region, boolean hasEntities) {
      return of(extent, region, hasEntities, false);
   }

   public static WorldCopyClipboard of(Extent extent, Region region, boolean hasEntities, boolean hasBiomes) {
      return new WorldCopyClipboard(extent, region, hasEntities, hasBiomes);
   }

   public Extent getExtent() {
      return this.extent;
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return this.getExtent().getFullBlock(x, y, z);
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.getExtent().getBlock(x, y, z);
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return this.getExtent().getBiomeType(x, y, z);
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.getExtent().getBiomeType(position.x(), position.y(), position.z());
   }

   @Deprecated
   @Override
   public List<? extends Entity> getEntities() {
      return (List<? extends Entity>)(!this.hasEntities ? new ArrayList<>() : this.getExtent().getEntities(this.getRegion()));
   }

   @Override
   public boolean hasBiomes() {
      return this.hasBiomes;
   }

   @Override
   public void paste(Extent toExtent, BlockVector3 to, boolean pasteAir, boolean pasteEntities, boolean pasteBiomes) {
      boolean close = false;
      if (toExtent instanceof World) {
         close = true;
         EditSessionBuilder builder = WorldEdit.getInstance()
            .newEditSessionBuilder()
            .world((World)toExtent)
            .checkMemory(false)
            .allowedRegionsEverywhere()
            .limitUnlimited()
            .changeSetNull();
         toExtent = builder.build();
      }

      Extent source = this.getExtent();
      Collection<Entity> entities = (Collection<Entity>)(pasteEntities ? ForwardExtentCopy.getEntities(source, this.region) : Collections.emptySet());
      BlockVector3 origin = this.getOrigin();
      int relx = to.x() - origin.x();
      int rely = to.y() - origin.y();
      int relz = to.z() - origin.z();
      pasteBiomes &= this.hasBiomes();

      for (BlockVector3 pos : this) {
         BaseBlock block = pos.getFullBlock(this);
         int xx = pos.x() + relx;
         int yy = pos.y() + rely;
         int zz = pos.z() + relz;
         if (pasteBiomes) {
            toExtent.setBiome(xx, yy, zz, pos.getBiome(this));
         }

         if (pasteAir || !block.getBlockType().getMaterial().isAir()) {
            toExtent.setBlock(xx, yy, zz, block);
         }
      }

      int entityOffsetX = to.x() - origin.x();
      int entityOffsetY = to.y() - origin.y();
      int entityOffsetZ = to.z() - origin.z();

      for (Entity entity : entities) {
         if (entity.getState() == null || !entity.getState().getType().getId().equals("minecraft:player")) {
            Location pos = entity.getLocation();
            Location newPos = new Location(
               pos.getExtent(), pos.x() + entityOffsetX, pos.y() + entityOffsetY, pos.z() + entityOffsetZ, pos.getYaw(), pos.getPitch()
            );
            toExtent.createEntity(newPos, entity.getState());
         }
      }

      if (close) {
         ((EditSession)toExtent).close();
      }
   }
}
