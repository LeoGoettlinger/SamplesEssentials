package com.fastasyncworldedit.core;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.internal.exception.FaweBlockBagException;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.math.BitArray;
import com.fastasyncworldedit.core.math.BitArrayUnstretched;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.math.MutableVector3;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.Pool;
import com.fastasyncworldedit.core.queue.Trimable;
import com.fastasyncworldedit.core.queue.implementation.QueuePool;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.collection.CleanableThreadLocal;
import com.fastasyncworldedit.core.util.task.FaweBasicThreadFactory;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongArrayTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.Logger;

public enum FaweCache implements Trimable {
   INSTANCE;

   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static final FaweCache IMP = INSTANCE;
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   public final int BLOCKS_PER_LAYER = 4096;
   public final char[] EMPTY_CHAR_4096 = new char[4096];
   private final IdentityHashMap<Class<? extends IChunkSet>, Pool<? extends IChunkSet>> REGISTERED_POOLS = new IdentityHashMap<>();
   public static final FaweBlockBagException BLOCK_BAG = new FaweBlockBagException();
   public static final FaweException MANUAL = new FaweException(Caption.of("fawe.cancel.reason.manual"), FaweException.Type.MANUAL, false);
   public static final FaweException NO_REGION = new FaweException(Caption.of("fawe.cancel.reason.no.region"), FaweException.Type.NO_REGION, false);
   public static final FaweException OUTSIDE_REGION = new FaweException(
      Caption.of("fawe.cancel.reason.outside.region"), FaweException.Type.OUTSIDE_REGION, true
   );
   public static final FaweException OUTSIDE_SAFE_REGION = new FaweException(
      Caption.of("fawe.cancel.reason.outside.safe.region"), FaweException.Type.OUTSIDE_REGION
   );
   public static final FaweException MAX_CHECKS = new FaweException(Caption.of("fawe.cancel.reason.max.checks"), FaweException.Type.MAX_CHECKS, true);
   public static final FaweException MAX_FAILS = new FaweException(Caption.of("fawe.cancel.reason.max.fails"), FaweException.Type.MAX_CHECKS, true);
   public static final FaweException MAX_CHANGES = new FaweException(Caption.of("fawe.cancel.reason.max.changes"), FaweException.Type.MAX_CHANGES, false);
   public static final FaweException LOW_MEMORY = new FaweException(Caption.of("fawe.cancel.reason.low.memory"), FaweException.Type.LOW_MEMORY, false);
   public static final FaweException MAX_ENTITIES = new FaweException(Caption.of("fawe.cancel.reason.max.entities"), FaweException.Type.MAX_ENTITIES, true);
   public static final FaweException MAX_TILES = new FaweException(Caption.of("fawe.cancel.reason.max.tiles", FaweException.Type.MAX_TILES, true));
   public static final FaweException MAX_ITERATIONS = new FaweException(
      Caption.of("fawe.cancel.reason.max.iterations"), FaweException.Type.MAX_ITERATIONS, true
   );
   public static final FaweException PLAYER_ONLY = new FaweException(Caption.of("fawe.cancel.reason.player-only"), FaweException.Type.PLAYER_ONLY, false);
   public static final FaweException ACTOR_REQUIRED = new FaweException(
      Caption.of("fawe.cancel.reason.actor-required"), FaweException.Type.ACTOR_REQUIRED, false
   );
   public final CleanableThreadLocal<AtomicBoolean> CHUNK_FLAG = new CleanableThreadLocal<>(AtomicBoolean::new);
   public final CleanableThreadLocal<byte[]> BYTE_BUFFER_8192 = new CleanableThreadLocal<>(() -> new byte[8192]);
   public final CleanableThreadLocal<int[]> BLOCK_TO_PALETTE = new CleanableThreadLocal<>(() -> {
      int[] result = new int[BlockTypesCache.states.length];
      Arrays.fill(result, Integer.MAX_VALUE);
      return result;
   });
   public final CleanableThreadLocal<char[]> SECTION_BITS_TO_CHAR = new CleanableThreadLocal<>(() -> new char[4096]);
   public final CleanableThreadLocal<int[]> PALETTE_TO_BLOCK = new CleanableThreadLocal<>(() -> new int[65536]);
   public final CleanableThreadLocal<char[]> PALETTE_TO_BLOCK_CHAR = new CleanableThreadLocal<>(() -> new char[65536], a -> Arrays.fill(a, '\uffff'));
   public final CleanableThreadLocal<long[]> BLOCK_STATES = new CleanableThreadLocal<>(() -> new long[2048]);
   public final CleanableThreadLocal<int[]> SECTION_BLOCKS = new CleanableThreadLocal<>(() -> new int[4096]);
   public final CleanableThreadLocal<int[]> INDEX_STORE = new CleanableThreadLocal<>(() -> new int[256]);
   private final CleanableThreadLocal<FaweCache.Palette> PALETTE_CACHE = new CleanableThreadLocal<>(FaweCache.Palette::new);
   public CleanableThreadLocal<MutableBlockVector3> MUTABLE_BLOCKVECTOR3 = new CleanableThreadLocal<>(MutableBlockVector3::new);
   public CleanableThreadLocal<MutableVector3> MUTABLE_VECTOR3 = new CleanableThreadLocal<MutableVector3>(MutableVector3::new) {
      public MutableVector3 init() {
         return new MutableVector3();
      }
   };

   @Override
   public synchronized boolean trim(boolean aggressive) {
      this.CHUNK_FLAG.clean();
      this.BYTE_BUFFER_8192.clean();
      this.BLOCK_TO_PALETTE.clean();
      this.PALETTE_TO_BLOCK.clean();
      this.BLOCK_STATES.clean();
      this.SECTION_BLOCKS.clean();
      this.PALETTE_CACHE.clean();
      this.PALETTE_TO_BLOCK_CHAR.clean();
      this.INDEX_STORE.clean();
      this.MUTABLE_VECTOR3.clean();
      this.MUTABLE_BLOCKVECTOR3.clean();
      this.SECTION_BITS_TO_CHAR.clean();

      for (Entry<Class<? extends IChunkSet>, Pool<? extends IChunkSet>> entry : this.REGISTERED_POOLS.entrySet()) {
         Pool<? extends IChunkSet> pool = entry.getValue();
         pool.clear();
      }

      return false;
   }

   public synchronized <T extends IChunkSet> Pool<T> registerPool(Class<T> clazz, Supplier<T> cache, boolean buffer) {
      Preconditions.checkNotNull(cache);
      Pool<T> pool;
      if (buffer) {
         pool = new QueuePool<>(cache);
      } else {
         pool = cache::get;
      }

      Pool<? extends IChunkSet> previous = this.REGISTERED_POOLS.putIfAbsent(clazz, pool);
      if (previous != null) {
         throw new IllegalStateException("Previous key");
      } else {
         return pool;
      }
   }

   public <T, V> LoadingCache<T, V> createCache(Supplier<V> withInitial) {
      return CacheBuilder.newBuilder().build(new CacheLoader<T, V>() {
         public V load(@Nonnull T key) {
            return withInitial.get();
         }
      });
   }

   public <T, V> LoadingCache<T, V> createCache(Function<T, V> withInitial) {
      return CacheBuilder.newBuilder().build(new CacheLoader<T, V>() {
         public V load(@Nonnull T key) {
            return withInitial.apply(key);
         }
      });
   }

   public <V> LongFunction<V> createMainThreadSafeCache(Supplier<V> withInitial) {
      return new LongFunction<V>() {
         private final LoadingCache<Long, V> loadingCache = Fawe.isMainThread() ? null : FaweCache.INSTANCE.createCache(withInitial);

         @Override
         public V apply(final long input) {
            return (V)(this.loadingCache != null ? this.loadingCache.getUnchecked(input) : withInitial.get());
         }
      };
   }

   public FaweCache.Palette toPalette(int layerOffset, char[] blocks) {
      return this.toPalette(layerOffset, null, blocks);
   }

   public FaweCache.Palette toPalette(int layerOffset, int[] blocks) {
      return this.toPalette(layerOffset, blocks, null);
   }

   private FaweCache.Palette toPalette(int layerOffset, int[] blocksInts, char[] blocksChars) {
      int[] blockToPalette = this.BLOCK_TO_PALETTE.get();
      int[] paletteToBlock = this.PALETTE_TO_BLOCK.get();
      long[] blockStates = this.BLOCK_STATES.get();
      int[] blocksCopy = this.SECTION_BLOCKS.get();

      try {
         int num_palette = 0;
         int blockIndexStart = layerOffset << 12;
         int blockIndexEnd = blockIndexStart + 4096;
         if (blocksChars != null) {
            int i = blockIndexStart;

            for (int j = 0; i < blockIndexEnd; j++) {
               int ordinal = blocksChars[i];
               int palette = blockToPalette[ordinal];
               if (palette == Integer.MAX_VALUE) {
                  palette = num_palette;
                  blockToPalette[ordinal] = num_palette;
                  paletteToBlock[num_palette] = ordinal;
                  num_palette++;
               }

               blocksCopy[j] = palette;
               i++;
            }
         } else {
            if (blocksInts == null) {
               throw new IllegalArgumentException();
            }

            int i = blockIndexStart;

            for (int j = 0; i < blockIndexEnd; j++) {
               int ordinal = blocksInts[i];
               int palette = blockToPalette[ordinal];
               if (palette == Integer.MAX_VALUE) {
                  palette = num_palette;
                  blockToPalette[ordinal] = num_palette;
                  paletteToBlock[num_palette] = ordinal;
                  num_palette++;
               }

               blocksCopy[j] = palette;
               i++;
            }
         }

         for (int i = 0; i < num_palette; i++) {
            blockToPalette[paletteToBlock[i]] = Integer.MAX_VALUE;
         }

         int bitsPerEntry = MathMan.log2nlz(num_palette - 1);
         if (!Settings.settings().PROTOCOL_SUPPORT_FIX && num_palette == 1) {
            bitsPerEntry = Math.max(bitsPerEntry, 1);
         } else {
            bitsPerEntry = Math.max(bitsPerEntry, 4);
         }

         int blockBitArrayEnd = bitsPerEntry * 4096 >> 6;
         if (num_palette == 1) {
            blockStates[0] = 0L;
            blockBitArrayEnd = 1;
         } else {
            BitArray bitArray = new BitArray(bitsPerEntry, 4096, blockStates);
            bitArray.fromRaw(blocksCopy);
         }

         FaweCache.Palette palette = this.PALETTE_CACHE.get();
         palette.bitsPerEntry = bitsPerEntry;
         palette.paletteToBlockLength = num_palette;
         palette.paletteToBlock = paletteToBlock;
         palette.blockStatesLength = blockBitArrayEnd;
         palette.blockStates = blockStates;
         return palette;
      } catch (Throwable var15) {
         var15.printStackTrace();
         Arrays.fill(blockToPalette, Integer.MAX_VALUE);
         throw var15;
      }
   }

   public FaweCache.Palette toPaletteUnstretched(int layerOffset, char[] blocks) {
      return this.toPaletteUnstretched(layerOffset, null, blocks);
   }

   private FaweCache.Palette toPaletteUnstretched(int layerOffset, int[] blocksInts, char[] blocksChars) {
      int[] blockToPalette = this.BLOCK_TO_PALETTE.get();
      int[] paletteToBlock = this.PALETTE_TO_BLOCK.get();
      long[] blockStates = this.BLOCK_STATES.get();
      int[] blocksCopy = this.SECTION_BLOCKS.get();

      try {
         int num_palette = 0;
         int blockIndexStart = layerOffset << 12;
         int blockIndexEnd = blockIndexStart + 4096;
         if (blocksChars != null) {
            int i = blockIndexStart;

            for (int j = 0; i < blockIndexEnd; j++) {
               int ordinal = blocksChars[i];
               int palette = blockToPalette[ordinal];
               if (palette == Integer.MAX_VALUE) {
                  palette = num_palette;
                  blockToPalette[ordinal] = num_palette;
                  paletteToBlock[num_palette] = ordinal;
                  num_palette++;
               }

               blocksCopy[j] = palette;
               i++;
            }
         } else {
            if (blocksInts == null) {
               throw new IllegalArgumentException();
            }

            int i = blockIndexStart;

            for (int j = 0; i < blockIndexEnd; j++) {
               int ordinal = blocksInts[i];
               int palette = blockToPalette[ordinal];
               if (palette == Integer.MAX_VALUE) {
                  palette = num_palette;
                  blockToPalette[ordinal] = num_palette;
                  paletteToBlock[num_palette] = ordinal;
                  num_palette++;
               }

               blocksCopy[j] = palette;
               i++;
            }
         }

         for (int i = 0; i < num_palette; i++) {
            blockToPalette[paletteToBlock[i]] = Integer.MAX_VALUE;
         }

         int bitsPerEntry = MathMan.log2nlz(num_palette - 1);
         if (!Settings.settings().PROTOCOL_SUPPORT_FIX && num_palette == 1) {
            bitsPerEntry = Math.max(bitsPerEntry, 1);
         } else {
            bitsPerEntry = Math.max(bitsPerEntry, 4);
         }

         int blocksPerLong = MathMan.floorZero(64.0 / bitsPerEntry);
         int blockBitArrayEnd = MathMan.ceilZero(4096.0F / blocksPerLong);
         if (num_palette == 1) {
            blockStates[0] = 0L;
            blockBitArrayEnd = 1;
         } else {
            BitArrayUnstretched bitArray = new BitArrayUnstretched(bitsPerEntry, 4096, blockStates);
            bitArray.fromRaw(blocksCopy);
         }

         FaweCache.Palette palette = this.PALETTE_CACHE.get();
         palette.bitsPerEntry = bitsPerEntry;
         palette.paletteToBlockLength = num_palette;
         palette.paletteToBlock = paletteToBlock;
         palette.blockStatesLength = blockBitArrayEnd;
         palette.blockStates = blockStates;
         return palette;
      } catch (Throwable var15) {
         var15.printStackTrace();
         Arrays.fill(blockToPalette, Integer.MAX_VALUE);
         throw var15;
      }
   }

   public Map<String, Object> asMap(Object... pairs) {
      HashMap<String, Object> map = new HashMap<>(pairs.length >> 1);

      for (int i = 0; i < pairs.length; i += 2) {
         String key = (String)pairs[i];
         Object value = pairs[i + 1];
         map.put(key, value);
      }

      return map;
   }

   public ShortTag asTag(short value) {
      return new ShortTag(value);
   }

   public IntTag asTag(int value) {
      return new IntTag(value);
   }

   public DoubleTag asTag(double value) {
      return new DoubleTag(value);
   }

   public ByteTag asTag(byte value) {
      return new ByteTag(value);
   }

   public FloatTag asTag(float value) {
      return new FloatTag(value);
   }

   public LongTag asTag(long value) {
      return new LongTag(value);
   }

   public ByteArrayTag asTag(byte[] value) {
      return new ByteArrayTag(value);
   }

   public IntArrayTag asTag(int[] value) {
      return new IntArrayTag(value);
   }

   public LongArrayTag asTag(long[] value) {
      return new LongArrayTag(value);
   }

   public StringTag asTag(String value) {
      return new StringTag(value);
   }

   public CompoundTag asTag(Map<String, Object> value) {
      HashMap<String, Tag<?, ?>> map = new HashMap<>();

      for (Entry<String, Object> entry : value.entrySet()) {
         Object child = entry.getValue();
         Tag tag = this.asTag(child);
         map.put(entry.getKey(), tag);
      }

      return new CompoundTag(map);
   }

   public Tag asTag(Object value) {
      if (value instanceof Integer) {
         return this.asTag(((Integer)value).intValue());
      } else if (value instanceof Short) {
         return this.asTag(((Short)value).shortValue());
      } else if (value instanceof Double) {
         return this.asTag(((Double)value).doubleValue());
      } else if (value instanceof Byte) {
         return this.asTag(((Byte)value).byteValue());
      } else if (value instanceof Float) {
         return this.asTag(((Float)value).floatValue());
      } else if (value instanceof Long) {
         return this.asTag(((Long)value).longValue());
      } else if (value instanceof String) {
         return this.asTag((String)value);
      } else if (value instanceof Map) {
         return this.asTag((Map<String, Object>)value);
      } else if (value instanceof Collection) {
         return this.asTag((Collection)value);
      } else if (value instanceof Object[]) {
         return this.asTag((Object[])value);
      } else if (value instanceof byte[]) {
         return this.asTag((byte[])value);
      } else if (value instanceof int[]) {
         return this.asTag((int[])value);
      } else if (value instanceof long[]) {
         return this.asTag((long[])value);
      } else if (value instanceof Tag) {
         return (Tag)value;
      } else if (value instanceof Boolean) {
         return this.asTag((byte)((Boolean)value ? 1 : 0));
      } else {
         LOGGER.error("Invalid nbt: {}", value);
         return null;
      }
   }

   public ListTag asTag(Object... values) {
      Class<? extends Tag> clazz = null;
      List<Tag> list = new ArrayList<>(values.length);

      for (Object value : values) {
         Tag tag = this.asTag(value);
         if (clazz == null) {
            clazz = (Class<? extends Tag>)tag.getClass();
         }

         list.add(tag);
      }

      if (clazz == null) {
         clazz = EndTag.class;
      }

      return new ListTag(clazz, list);
   }

   public ListTag asTag(Collection values) {
      Class<? extends Tag> clazz = null;
      List<Tag> list = new ArrayList<>(values.size());

      for (Object value : values) {
         Tag tag = this.asTag(value);
         if (clazz == null) {
            clazz = (Class<? extends Tag>)tag.getClass();
         }

         list.add(tag);
      }

      if (clazz == null) {
         clazz = EndTag.class;
      }

      return new ListTag(clazz, list);
   }

   public ThreadPoolExecutor newBlockingExecutor() {
      return this.newBlockingExecutor("FAWE Blocking Executor - %d");
   }

   public ThreadPoolExecutor newBlockingExecutor(String name) {
      return this.newBlockingExecutor(name, LOGGER);
   }

   public ThreadPoolExecutor newBlockingExecutor(String name, Logger logger) {
      int nThreads = Settings.settings().QUEUE.PARALLEL_THREADS;
      LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
      return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, queue, new FaweBasicThreadFactory(name), new CallerRunsPolicy()) {
         private final boolean[] faweExceptionReasonsUsed = new boolean[FaweException.Type.values().length];
         private int lastException = Integer.MIN_VALUE;
         private int count = 0;

         @Override
         protected synchronized void afterExecute(Runnable runnable, Throwable throwable) {
            super.afterExecute(runnable, throwable);
            if (throwable == null && runnable instanceof Future<?> future) {
               try {
                  if (future.isDone()) {
                     future.get();
                  }
               } catch (CancellationException var4) {
                  throwable = var4;
               } catch (ExecutionException var5) {
                  throwable = var5.getCause();
               } catch (InterruptedException var6) {
                  Thread.currentThread().interrupt();
               }
            }

            if (throwable != null) {
               if (throwable instanceof FaweException) {
                  this.handleFaweException((FaweException)throwable);
               } else if (throwable.getCause() instanceof FaweException) {
                  this.handleFaweException((FaweException)throwable.getCause());
               } else {
                  int hash = throwable.getMessage() != null ? throwable.getMessage().hashCode() : 0;
                  if (hash != this.lastException) {
                     this.lastException = hash;
                     logger.catching(throwable);
                     this.count = 0;
                  } else if (this.count < Settings.settings().QUEUE.PARALLEL_THREADS) {
                     logger.warn(throwable.getMessage());
                     this.count++;
                  }
               }
            }
         }

         private void handleFaweException(FaweException e) {
            FaweException.Type type = e.getType();
            if (e.getType() == FaweException.Type.OTHER) {
               logger.catching(e);
            } else if (!this.faweExceptionReasonsUsed[type.ordinal()]) {
               this.faweExceptionReasonsUsed[type.ordinal()] = true;
               logger.warn("FaweException: " + e.getMessage());
            }
         }
      };
   }

   public static final class Palette {
      public int bitsPerEntry;
      public int paletteToBlockLength;
      public int[] paletteToBlock;
      public int blockStatesLength;
      public long[] blockStates;
   }
}
