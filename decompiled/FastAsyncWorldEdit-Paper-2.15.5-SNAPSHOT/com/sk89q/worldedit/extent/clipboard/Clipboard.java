package com.sk89q.worldedit.extent.clipboard;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.clipboard.CPUOptimizedClipboard;
import com.fastasyncworldedit.core.extent.clipboard.DiskOptimizedClipboard;
import com.fastasyncworldedit.core.extent.clipboard.MemoryOptimizedClipboard;
import com.fastasyncworldedit.core.extent.clipboard.ReadOnlyClipboard;
import com.fastasyncworldedit.core.function.visitor.Order;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.internal.util.ClipboardTransformBaker;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Clipboard extends Extent, Iterable<BlockVector3>, Closeable, Flushable {
   @Deprecated
   static Clipboard create(Region region) {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(region.getWorld(), "World cannot be null (use the other constructor for the region)");
      EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(region.getWorld()).allowedRegionsEverywhere().build();
      return ReadOnlyClipboard.of(session, region);
   }

   @Deprecated
   static Clipboard create(Region region, UUID uuid) {
      if (Settings.settings().CLIPBOARD.USE_DISK) {
         return new DiskOptimizedClipboard(region, uuid);
      } else {
         return (Clipboard)(Settings.settings().CLIPBOARD.COMPRESSION_LEVEL == 0 ? new CPUOptimizedClipboard(region) : new MemoryOptimizedClipboard(region));
      }
   }

   Region getRegion();

   BlockVector3 getDimensions();

   BlockVector3 getOrigin();

   void setOrigin(BlockVector3 origin);

   default boolean hasBiomes() {
      return false;
   }

   default Clipboard transform(Transform transform) throws WorldEditException {
      return ClipboardTransformBaker.bakeTransform(this, transform);
   }

   void removeEntity(Entity entity);

   default int getWidth() {
      return this.getDimensions().x();
   }

   default int getHeight() {
      return this.getDimensions().y();
   }

   default int getLength() {
      return this.getDimensions().z();
   }

   default int getArea() {
      return this.getWidth() * this.getLength();
   }

   default int getVolume() {
      return this.getWidth() * this.getHeight() * this.getLength();
   }

   default Iterator<BlockVector3> iterator(Order order) {
      return order.create(this.getRegion());
   }

   @Nonnull
   @Override
   default Iterator<BlockVector3> iterator() {
      return this.getRegion().iterator();
   }

   default Iterator<BlockVector2> iterator2d() {
      return Regions.asFlatRegion(this.getRegion()).asFlatRegion().iterator();
   }

   default URI getURI() {
      return null;
   }

   @Override
   default <T extends Filter> T apply(Region region, T filter, boolean full) {
      return region.equals(this.getRegion()) ? this.apply(this, filter) : this.apply(region, filter);
   }

   @Override
   default void close() {
   }

   @Override
   default void flush() {
   }

   default EditSession paste(World world, BlockVector3 to) {
      return this.paste(world, to, true, true, null);
   }

   default void save(File file, ClipboardFormat format) throws IOException {
      Preconditions.checkNotNull(file);
      Preconditions.checkNotNull(format);
      if (!file.exists()) {
         File parent = file.getParentFile();
         if (parent != null) {
            parent.mkdirs();
         }

         file.createNewFile();
      }

      this.save(new FileOutputStream(file), format);
   }

   default void save(OutputStream stream, ClipboardFormat format) throws IOException {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(format);

      try (ClipboardWriter writer = format.getWriter(stream)) {
         writer.write(this);
      }
   }

   default EditSession paste(World world, BlockVector3 to, boolean allowUndo, boolean pasteAir, @Nullable Transform transform) {
      return this.paste(world, to, allowUndo, pasteAir, true, transform);
   }

   default EditSession paste(World world, BlockVector3 to, boolean allowUndo, boolean pasteAir, boolean pasteEntities, @Nullable Transform transform) {
      Preconditions.checkNotNull(world);
      Preconditions.checkNotNull(to);
      if (!(world instanceof EditSession editSession)) {
         EditSessionBuilder builder = WorldEdit.getInstance()
            .newEditSessionBuilder()
            .world(world)
            .checkMemory(false)
            .allowedRegionsEverywhere()
            .limitUnlimited();
         if (allowUndo) {
            editSession = builder.build();
         } else {
            editSession = builder.changeSetNull().fastMode(true).build();
         }
      }

      Extent extent = this;
      Mask sourceMask = editSession.getSourceMask();
      if (transform != null && !transform.isIdentity()) {
         extent = new BlockTransformExtent(this, transform);
      } else if (sourceMask == null) {
         this.paste(editSession, to, pasteAir, pasteEntities, this.hasBiomes());
         editSession.flushQueue();
         return editSession;
      }

      ForwardExtentCopy copy = new ForwardExtentCopy(extent, this.getRegion(), this.getOrigin(), editSession, to);
      if (transform != null && !transform.isIdentity()) {
         copy.setTransform(transform);
      }

      copy.setCopyingEntities(pasteEntities);
      if (sourceMask != null) {
         new MaskTraverser(sourceMask).reset(extent);
         copy.setSourceMask(sourceMask);
         editSession.setSourceMask(null);
      }

      if (!pasteAir) {
         copy.setSourceMask(new ExistingBlockMask(this));
      }

      try {
         Operations.completeLegacy(copy);
      } finally {
         editSession.close();
      }

      return editSession;
   }

   default void paste(Extent extent, BlockVector3 to, boolean pasteAir, @Nullable Transform transform) {
      if (extent instanceof World) {
         EditSessionBuilder builder = WorldEdit.getInstance()
            .newEditSessionBuilder()
            .world((World)extent)
            .checkMemory(false)
            .allowedRegionsEverywhere()
            .limitUnlimited()
            .changeSetNull();
         extent = builder.build();
      }

      Extent source = this;
      if (transform != null && !transform.isIdentity()) {
         source = new BlockTransformExtent(this, transform);
      }

      ForwardExtentCopy copy = new ForwardExtentCopy(source, this.getRegion(), this.getOrigin(), extent, to);
      if (transform != null) {
         copy.setTransform(transform);
      }

      copy.setCopyingBiomes(this.hasBiomes());
      if (extent instanceof EditSession editSession) {
         Mask sourceMask = editSession.getSourceMask();
         if (sourceMask != null) {
            new MaskTraverser(sourceMask).reset(extent);
            copy.setSourceMask(sourceMask);
            editSession.setSourceMask(null);
         }
      }

      if (!pasteAir) {
         copy.setSourceMask(new ExistingBlockMask(this));
      }

      Operations.completeBlindly(copy);
   }

   default void paste(Extent extent, BlockVector3 to, boolean pasteAir) {
      this.paste(extent, to, pasteAir, false, false);
   }

   default void paste(Extent extent, BlockVector3 to, boolean pasteAir, boolean pasteEntities, boolean pasteBiomes) {
      boolean close = false;
      if (extent instanceof World) {
         close = true;
         EditSessionBuilder builder = WorldEdit.getInstance()
            .newEditSessionBuilder()
            .world((World)extent)
            .checkMemory(false)
            .allowedRegionsEverywhere()
            .limitUnlimited()
            .changeSetNull();
         extent = builder.build();
      }

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
            extent.setBiome(xx, yy, zz, pos.getBiome(this));
         }

         if (pasteAir || !block.getBlockType().getMaterial().isAir()) {
            extent.setBlock(xx, yy, zz, block);
         }
      }

      int entityOffsetX = to.x() - origin.x();
      int entityOffsetY = to.y() - origin.y();
      int entityOffsetZ = to.z() - origin.z();
      if (pasteEntities) {
         for (Entity entity : this.getEntities()) {
            if (entity.getState() == null || !entity.getState().getType().id().equals("minecraft:player")) {
               Location pos = entity.getLocation();
               Location newPos = new Location(
                  pos.getExtent(), pos.x() + entityOffsetX, pos.y() + entityOffsetY, pos.z() + entityOffsetZ, pos.getYaw(), pos.getPitch()
               );
               extent.createEntity(newPos, entity.getState());
            }
         }
      }

      if (close) {
         ((EditSession)extent).close();
      }
   }
}
