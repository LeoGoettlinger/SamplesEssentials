package com.fastasyncworldedit.core.history.changeset;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.extent.HistoryExtent;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.NbtUtils;
import com.fastasyncworldedit.core.util.TaskManager;
import com.google.common.util.concurrent.Futures;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.change.EntityCreate;
import com.sk89q.worldedit.history.change.EntityRemove;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.jetbrains.annotations.ApiStatus.Internal;

public abstract class AbstractChangeSet implements ChangeSet, IBatchProcessor {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final World world;
   private final AtomicInteger lastException = new AtomicInteger();
   private final Semaphore workerSemaphore = new Semaphore(1, false);
   private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
   protected volatile boolean closed;

   public AbstractChangeSet(World world) {
      this.world = world;
   }

   public World getWorld() {
      return this.world;
   }

   public void closeAsync() {
      if (!this.closed) {
         TaskManager.taskManager().async(() -> {
            try {
               this.close();
            } catch (IOException var2) {
               LOGGER.catching(var2);
            }
         });
      }
   }

   @Override
   public void flush() {
      try {
         this.drainQueue(true);
      } catch (Exception var2) {
         LOGGER.catching(var2);
      }
   }

   @Override
   public void close() throws IOException {
      if (!this.closed) {
         this.flush();
         this.closed = true;
      }
   }

   public abstract void add(int x, int y, int z, int combinedFrom, int combinedTo);

   @Override
   public Iterator<Change> backwardIterator() {
      return this.getIterator(false);
   }

   @Override
   public Iterator<Change> forwardIterator() {
      return this.getIterator(true);
   }

   @Override
   public Extent construct(Extent child) {
      return new HistoryExtent(child, this);
   }

   @Override
   public final synchronized IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      int bx = chunk.getX() << 4;
      int bz = chunk.getZ() << 4;
      Map<BlockVector3, FaweCompoundTag> tilesFrom = get.tiles();
      Map<BlockVector3, FaweCompoundTag> tilesTo = set.tiles();
      if (!tilesFrom.isEmpty()) {
         for (Entry<BlockVector3, FaweCompoundTag> entry : tilesFrom.entrySet()) {
            BlockVector3 pos = entry.getKey();
            BlockState fromBlock = get.getBlock(pos.x() & 15, pos.y(), pos.z() & 15);
            BlockState toBlock = set.getBlock(pos.x() & 15, pos.y(), pos.z() & 15);
            if (fromBlock != toBlock || tilesTo.containsKey(pos)) {
               this.addTileRemove(NbtUtils.withPosition(entry.getValue(), entry.getKey().x(), entry.getKey().y(), entry.getKey().z()));
            }
         }
      }

      if (!tilesTo.isEmpty()) {
         for (Entry<BlockVector3, FaweCompoundTag> entryx : tilesTo.entrySet()) {
            BlockVector3 pos = entryx.getKey();
            this.addTileCreate(NbtUtils.withPosition(entryx.getValue(), pos.x() + bx, pos.y(), pos.z() + bz));
         }
      }

      Set<UUID> entRemoves = set.getEntityRemoves();
      if (!entRemoves.isEmpty()) {
         for (UUID uuid : entRemoves) {
            FaweCompoundTag found = get.entity(uuid);
            if (found != null) {
               this.addEntityRemove(found);
            }
         }
      }

      Collection<FaweCompoundTag> ents = set.entities();
      if (!ents.isEmpty()) {
         for (FaweCompoundTag tag : ents) {
            this.addEntityCreate(tag);
         }
      }

      for (int layer = get.getMinSectionPosition(); layer <= get.getMaxSectionPosition(); layer++) {
         if (set.hasSection(layer)) {
            char[] blocksGet = get.load(layer);
            if (blocksGet == null) {
               blocksGet = FaweCache.INSTANCE.EMPTY_CHAR_4096;
            }

            char[] blocksSet = Objects.requireNonNull(set.loadIfPresent(layer));
            int by = layer << 4;
            int y = 0;

            for (int index = 0; y < 16; y++) {
               int yy = y + by;

               for (int z = 0; z < 16; z++) {
                  int zz = z + bz;

                  for (int x = 0; x < 16; index++) {
                     int combinedTo = blocksSet[index];
                     if (combinedTo != 0) {
                        int xx = bx + x;
                        int from = blocksGet[index];
                        if (from == 0) {
                           from = 1;
                        }

                        this.add(xx, yy, zz, from, combinedTo);
                     }

                     x++;
                  }
               }
            }
         }
      }

      BiomeType[][] biomes = set.getBiomes();
      if (biomes != null) {
         for (int layerx = get.getMinSectionPosition(); layerx <= get.getMaxSectionPosition(); layerx++) {
            if (set.hasBiomes(layerx)) {
               BiomeType[] biomeSection = biomes[layerx - set.getMinSectionPosition()];
               int index = 0;
               int yy = layerx << 4;

               for (int y = 0; y < 16; y += 4) {
                  for (int z = 0; z < 16; z += 4) {
                     for (int x = 0; x < 16; index++) {
                        BiomeType newBiome = biomeSection[index];
                        if (newBiome != null) {
                           BiomeType oldBiome = get.getBiomeType(x, yy + y, z);
                           if (oldBiome != newBiome) {
                              this.addBiomeChange(bx + x, yy + y, bz + z, oldBiome, newBiome);
                           }
                        }

                        x += 4;
                     }
                  }
               }
            }
         }
      }

      return set;
   }

   @Override
   public void postProcess(final IChunk chunk, final IChunkGet get, final IChunkSet set) {
      this.addWriteTask(() -> this.processSet(chunk, get, set));
   }

   @Override
   public Future<?> postProcessSet(final IChunk chunk, final IChunkGet get, final IChunkSet set) {
      return this.addWriteTask(() -> this.processSet(chunk, get, set));
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.READING_BLOCKS;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   public void addTileCreate(CompoundTag tag) {
      this.addTileCreate(adapt(tag));
   }

   @Nonnull
   private static FaweCompoundTag adapt(CompoundTag tag) {
      return FaweCompoundTag.of(tag.toLinTag());
   }

   public abstract void addTileCreate(FaweCompoundTag tag);

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   public void addTileRemove(CompoundTag tag) {
      this.addTileRemove(adapt(tag));
   }

   public abstract void addTileRemove(FaweCompoundTag tag);

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   public void addEntityRemove(CompoundTag tag) {
      this.addEntityRemove(adapt(tag));
   }

   public abstract void addEntityRemove(FaweCompoundTag tag);

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   public void addEntityCreate(CompoundTag tag) {
      this.addEntityCreate(adapt(tag));
   }

   public abstract void addEntityCreate(FaweCompoundTag tag);

   public abstract void addBiomeChange(int x, int y, int z, BiomeType from, BiomeType to);

   public Iterator<Change> getIterator(BlockBag blockBag, int mode, boolean redo) {
      return this.getIterator(redo);
   }

   @Internal
   public abstract ChangeExchangeCoordinator getCoordinatedChanges(BlockBag blockBag, int mode, boolean dir);

   public abstract Iterator<Change> getIterator(boolean redo);

   public EditSession toEditSession(Actor actor) {
      return this.toEditSession(actor, null);
   }

   public EditSession toEditSession(Actor actor, Region[] regions) {
      EditSessionBuilder builder = WorldEdit.getInstance()
         .newEditSessionBuilder()
         .world(this.world)
         .checkMemory(false)
         .changeSetNull()
         .fastMode(false)
         .limitUnprocessed(actor)
         .actor(actor);
      if (!actor.getLimit().RESTRICT_HISTORY_TO_REGIONS) {
         builder = builder.allowedRegionsEverywhere();
      }

      EditSession editSession = builder.build();
      editSession.setSize(1);
      return editSession;
   }

   public void add(EntityCreate change) {
      LinCompoundTag tag = change.state.getNbt();

      assert tag != null;

      this.addEntityCreate(FaweCompoundTag.of(NbtUtils.withEntityInfo(tag, change.getEntity())));
   }

   public void add(EntityRemove change) {
      LinCompoundTag tag = change.state.getNbt();

      assert tag != null;

      this.addEntityRemove(FaweCompoundTag.of(NbtUtils.withEntityInfo(tag, change.getEntity())));
   }

   @Override
   public void add(Change change) {
      if (change.getClass() == BlockChange.class) {
         this.add((BlockChange)change);
      } else if (change.getClass() == EntityCreate.class) {
         this.add((EntityCreate)change);
      } else if (change.getClass() == EntityRemove.class) {
         this.add((EntityRemove)change);
      } else {
         LOGGER.error("Unknown change: {}", change.getClass());
      }
   }

   public void add(BlockChange change) {
      try {
         BlockVector3 loc = change.position();
         BaseBlock from = change.previous();
         BaseBlock to = change.current();
         this.add(loc, from, to);
      } catch (Exception var5) {
         LOGGER.catching(var5);
      }
   }

   @Override
   public boolean isEmpty() {
      return this.queue.isEmpty() && this.workerSemaphore.availablePermits() == 1 && this.longSize() == 0L;
   }

   public void add(BlockVector3 loc, BaseBlock from, BaseBlock to) {
      int x = loc.x();
      int y = loc.y();
      int z = loc.z();
      this.add(x, y, z, from, to);
   }

   public void add(int x, int y, int z, BaseBlock from, BaseBlock to) {
      try {
         LinCompoundTag nbt = from.getNbt();
         if (nbt != null) {
            this.addTileRemove(FaweCompoundTag.of(NbtUtils.withPosition(nbt, x, y, z)));
         }

         nbt = to.getNbt();
         if (nbt != null) {
            this.addTileCreate(FaweCompoundTag.of(NbtUtils.withPosition(nbt, x, y, z)));
         }

         int combinedFrom = from.getOrdinal();
         int combinedTo = to.getOrdinal();
         this.add(x, y, z, combinedFrom, combinedTo);
      } catch (Exception var9) {
         LOGGER.catching(var9);
      }
   }

   public void add(int x, int y, int z, int combinedFrom, BaseBlock to) {
      try {
         LinCompoundTag nbt = to.getNbt();
         if (nbt != null) {
            this.addTileCreate(FaweCompoundTag.of(NbtUtils.withPosition(nbt, x, y, z)));
         }

         int combinedTo = to.getInternalId();
         this.add(x, y, z, combinedFrom, combinedTo);
      } catch (Exception var8) {
         LOGGER.catching(var8);
      }
   }

   public Future<?> addWriteTask(Runnable writeTask) {
      return this.addWriteTask(writeTask, Fawe.isMainThread());
   }

   public Future<?> addWriteTask(final Runnable writeTask, final boolean completeNow) {
      Runnable wrappedTask = () -> {
         try {
            writeTask.run();
         } catch (Throwable var5) {
            if (completeNow) {
               throw var5;
            }

            int hash = var5.getMessage().hashCode();
            if (this.lastException.getAndSet(hash) != hash) {
               LOGGER.catching(var5);
            }
         }
      };
      if (completeNow) {
         wrappedTask.run();
         return Futures.immediateVoidFuture();
      } else {
         CompletableFuture<?> task = new CompletableFuture();
         this.queue.add(() -> {
            wrappedTask.run();
            task.complete(null);
         });
         this.triggerWorker();
         return task;
      }
   }

   private void triggerWorker() {
      if (this.workerSemaphore.availablePermits() != 0) {
         Fawe.instance().getQueueHandler().async(() -> this.drainQueue(false));
      }
   }

   private void drainQueue(boolean ignoreRunningState) {
      if (!this.workerSemaphore.tryAcquire()) {
         if (!ignoreRunningState) {
            return;
         }

         try {
            this.workerSemaphore.acquire();
         } catch (InterruptedException var6) {
            Thread.currentThread().interrupt();
         }
      }

      Runnable next;
      try {
         while ((next = this.queue.poll()) != null) {
            next.run();
         }
      } finally {
         this.workerSemaphore.release();
      }
   }
}
