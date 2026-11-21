package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.math.BlockVector3ChunkMap;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NbtValued;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;

public class StripNBTExtent extends AbstractDelegateExtent implements IBatchProcessor {
   private final Set<String> strip;

   public StripNBTExtent(Extent extent, Set<String> strip) {
      super(extent);
      this.strip = new HashSet<>(strip).stream().map(String::toLowerCase).collect(Collectors.toUnmodifiableSet());
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      return super.setBlock(location, this.stripBlockNBT(block));
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) throws WorldEditException {
      return super.setBlock(x, y, z, this.stripBlockNBT(block));
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      return super.createEntity(location, this.stripEntityNBT(entity));
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      return super.createEntity(location, this.stripEntityNBT(entity), uuid);
   }

   public <B extends BlockStateHolder<B>> B stripBlockNBT(B block) {
      if (!(block instanceof BaseBlock localBlock)) {
         return block;
      } else {
         LinCompoundTag nbt = localBlock.getNbt();
         if (nbt == null) {
            return block;
         } else {
            LinCompoundTag.Builder nbtBuilder = nbt.toBuilder();

            for (String key : this.strip) {
               nbtBuilder.remove(key);
            }

            return (B)localBlock.toBaseBlock(nbtBuilder.build());
         }
      }
   }

   public <T extends NbtValued> T stripEntityNBT(T entity) {
      LinCompoundTag nbt = entity.getNbt();
      if (nbt == null) {
         return entity;
      } else {
         LinCompoundTag.Builder nbtBuilder = nbt.toBuilder();

         for (String key : this.strip) {
            nbtBuilder.remove(key);
         }

         entity.setNbt(nbtBuilder.build());
         return entity;
      }
   }

   @Override
   public IChunkSet processSet(final IChunk chunk, final IChunkGet get, final IChunkSet set) {
      Map<BlockVector3, FaweCompoundTag> tiles = set.tiles();
      Collection<FaweCompoundTag> entities = set.entities();
      if (tiles.isEmpty() && entities.isEmpty()) {
         return set;
      } else {
         boolean isBv3ChunkMap = tiles instanceof BlockVector3ChunkMap;

         for (Entry<BlockVector3, FaweCompoundTag> entry : tiles.entrySet()) {
            FaweCompoundTag original = entry.getValue();
            FaweCompoundTag result = this.stripNbt(original);
            if (original != result) {
               if (isBv3ChunkMap) {
                  tiles.put(entry.getKey(), result);
               } else {
                  entry.setValue(result);
               }
            }
         }

         Set<FaweCompoundTag> stripped = new HashSet<>();
         Iterator<FaweCompoundTag> iterator = entities.iterator();

         while (iterator.hasNext()) {
            FaweCompoundTag original = iterator.next();
            FaweCompoundTag result = this.stripNbt(original);
            if (original != result) {
               iterator.remove();
               stripped.add(result);
            }
         }

         entities.addAll(stripped);
         return set;
      }
   }

   private FaweCompoundTag stripNbt(FaweCompoundTag compoundTag) {
      LinCompoundTag.Builder builder = LinCompoundTag.builder();
      boolean stripped = false;

      for (Entry<String, LinTag<?>> entry : compoundTag.linTag().value().entrySet()) {
         String k = entry.getKey();
         LinTag<?> v = entry.getValue();
         if (this.strip.contains(k.toLowerCase(Locale.ROOT))) {
            stripped = true;
         } else {
            builder.put(k, v);
         }
      }

      return stripped ? FaweCompoundTag.of(builder.build()) : compoundTag;
   }

   @Nullable
   @Override
   public Extent construct(final Extent child) {
      if (this.getExtent() != child) {
         new ExtentTraverser<>(this).setNext(child);
      }

      return this;
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.CHANGING_BLOCKS;
   }
}
