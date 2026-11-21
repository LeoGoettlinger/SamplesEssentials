package com.sk89q.worldedit.util.collection;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BitMath;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockMap<V> extends AbstractMap<BlockVector3, V> {
   private static final long BITS_24 = BitMath.mask(24);
   private static final long BITS_20 = BitMath.mask(20);
   private static final int BITS_8 = BitMath.mask(8);
   private static final int BITS_6 = BitMath.mask(6);
   private static final long GROUP_X = BITS_20;
   private static final long GROUP_Z = BITS_20 << 20;
   private static final long GROUP_Y = BITS_24 << 40;
   private static final int INNER_X = BITS_6;
   private static final int INNER_Z = BITS_6 << 6;
   private static final int INNER_Y = BITS_8 << 12;
   private final Long2ObjectMap<Int2ObjectMap<V>> maps = new Long2ObjectOpenHashMap(4, 0.75F);
   private final Supplier<Int2ObjectMap<V>> subMapSupplier;
   private Set<Entry<BlockVector3, V>> entrySet;
   private Collection<V> values;

   public static <V> BlockMap<V> create() {
      return create(() -> new Int2ObjectOpenHashMap(64, 0.9F));
   }

   public static BlockMap<BaseBlock> createForBaseBlock() {
      return create(Int2BaseBlockMap::new);
   }

   private static <V> BlockMap<V> create(Supplier<Int2ObjectMap<V>> subMapSupplier) {
      return new BlockMap<>(subMapSupplier);
   }

   public static <V> BlockMap<V> copyOf(Map<? extends BlockVector3, ? extends V> source) {
      return new BlockMap<>(Int2ObjectOpenHashMap::new, source);
   }

   private static long toGroupKey(BlockVector3 location) {
      return location.x() >>> 6 & BITS_20 | (location.z() >>> 6 & BITS_20) << 20 | (location.y() >>> 8 & BITS_24) << 40;
   }

   private static int toInnerKey(BlockVector3 location) {
      return location.x() & BITS_6 | (location.z() & BITS_6) << 6 | (location.y() & BITS_8) << 12;
   }

   private static BlockVector3 reconstructLocation(long group, int inner) {
      int groupX = (int)((group & GROUP_X) << 6);
      int x = BitMath.fixSign(groupX | inner & INNER_X, 26);
      int groupZ = (int)((group & GROUP_Z) >>> 14);
      int z = BitMath.fixSign(groupZ | (inner & INNER_Z) >>> 6, 26);
      int groupY = (int)((group & GROUP_Y) >>> 32);
      int y = groupY | (inner & INNER_Y) >>> 12;
      return BlockVector3.at(x, y, z);
   }

   private BlockMap(Supplier<Int2ObjectMap<V>> subMapSupplier) {
      this.subMapSupplier = subMapSupplier;
   }

   private BlockMap(Supplier<Int2ObjectMap<V>> subMapSupplier, Map<? extends BlockVector3, ? extends V> source) {
      this.subMapSupplier = subMapSupplier;
      this.putAll(source);
   }

   private Int2ObjectMap<V> getOrCreateMap(long groupKey) {
      return (Int2ObjectMap<V>)this.maps.computeIfAbsent(groupKey, k -> this.subMapSupplier.get());
   }

   private Int2ObjectMap<V> getOrEmptyMap(long groupKey) {
      return (Int2ObjectMap<V>)this.maps.getOrDefault(groupKey, Int2ObjectMaps.emptyMap());
   }

   private <R> R cleanlyModifyMap(long groupKey, Function<Int2ObjectMap<V>, R> func) {
      Int2ObjectMap<V> map = (Int2ObjectMap<V>)this.maps.get(groupKey);
      if (map != null) {
         R result = func.apply(map);
         if (map.isEmpty()) {
            this.maps.remove(groupKey);
         }

         return result;
      } else {
         map = this.subMapSupplier.get();
         R result = func.apply(map);
         if (!map.isEmpty()) {
            this.maps.put(groupKey, map);
         }

         return result;
      }
   }

   public V put(BlockVector3 key, V value) {
      return (V)this.getOrCreateMap(toGroupKey(key)).put(toInnerKey(key), value);
   }

   @Override
   public V getOrDefault(Object key, V defaultValue) {
      BlockVector3 vec = (BlockVector3)key;
      return (V)this.getOrEmptyMap(toGroupKey(vec)).getOrDefault(toInnerKey(vec), defaultValue);
   }

   @Override
   public void forEach(BiConsumer<? super BlockVector3, ? super V> action) {
      this.maps.forEach((groupKey, m) -> m.forEach((innerKey, block) -> action.accept(reconstructLocation(groupKey, innerKey), (V)block)));
   }

   @Override
   public void replaceAll(BiFunction<? super BlockVector3, ? super V, ? extends V> function) {
      this.maps.forEach((groupKey, m) -> m.replaceAll((innerKey, block) -> function.apply(reconstructLocation(groupKey, innerKey), (V)block)));
   }

   public V putIfAbsent(BlockVector3 key, V value) {
      return (V)this.getOrCreateMap(toGroupKey(key)).putIfAbsent(toInnerKey(key), value);
   }

   @Override
   public boolean remove(Object key, Object value) {
      BlockVector3 vec = (BlockVector3)key;
      return this.cleanlyModifyMap(toGroupKey(vec), map -> map.remove(toInnerKey(vec), value));
   }

   public boolean replace(BlockVector3 key, V oldValue, V newValue) {
      return this.cleanlyModifyMap(toGroupKey(key), map -> map.replace(toInnerKey(key), oldValue, newValue));
   }

   public V replace(BlockVector3 key, V value) {
      return (V)this.getOrCreateMap(toGroupKey(key)).replace(toInnerKey(key), value);
   }

   public V computeIfAbsent(BlockVector3 key, Function<? super BlockVector3, ? extends V> mappingFunction) {
      return this.cleanlyModifyMap(toGroupKey(key), map -> (V)map.computeIfAbsent(toInnerKey(key), ik -> mappingFunction.apply(key)));
   }

   public V computeIfPresent(BlockVector3 key, BiFunction<? super BlockVector3, ? super V, ? extends V> remappingFunction) {
      return this.cleanlyModifyMap(toGroupKey(key), map -> (V)map.computeIfPresent(toInnerKey(key), (ik, block) -> remappingFunction.apply(key, (V)block)));
   }

   public V compute(BlockVector3 key, BiFunction<? super BlockVector3, ? super V, ? extends V> remappingFunction) {
      return this.cleanlyModifyMap(toGroupKey(key), map -> (V)map.compute(toInnerKey(key), (ik, block) -> remappingFunction.apply(key, (V)block)));
   }

   public V merge(BlockVector3 key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      return this.cleanlyModifyMap(toGroupKey(key), map -> (V)map.merge(toInnerKey(key), value, remappingFunction));
   }

   @Override
   public Set<Entry<BlockVector3, V>> entrySet() {
      Set<Entry<BlockVector3, V>> es = this.entrySet;
      if (es == null) {
         this.entrySet = es = new AbstractSet<Entry<BlockVector3, V>>() {
            @Override
            public Iterator<Entry<BlockVector3, V>> iterator() {
               return new Iterator<Entry<BlockVector3, V>>() {
                  private final ObjectIterator<it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<Int2ObjectMap<V>>> primaryIterator = Long2ObjectMaps.fastIterator(
                     BlockMap.this.maps
                  );
                  private it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<Int2ObjectMap<V>> currentPrimaryEntry;
                  private ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>> secondaryIterator;
                  private boolean finished;
                  private BlockMap<V>.LazyEntry next;

                  @Override
                  public boolean hasNext() {
                     if (this.finished) {
                        return false;
                     } else {
                        if (this.next == null) {
                           BlockMap<V>.LazyEntry proposedNext = this.computeNext();
                           if (proposedNext == null) {
                              this.finished = true;
                              return false;
                           }

                           this.next = proposedNext;
                        }

                        return true;
                     }
                  }

                  private BlockMap<V>.LazyEntry computeNext() {
                     if (this.secondaryIterator == null || !this.secondaryIterator.hasNext()) {
                        if (!this.primaryIterator.hasNext()) {
                           return null;
                        }

                        this.currentPrimaryEntry = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<Int2ObjectMap<V>>)this.primaryIterator.next();
                        this.secondaryIterator = Int2ObjectMaps.fastIterator((Int2ObjectMap)this.currentPrimaryEntry.getValue());
                        Preconditions.checkState(this.secondaryIterator.hasNext(), "Should not have an empty map entry, it should have been removed!");
                     }

                     it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V> next = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<V>)this.secondaryIterator.next();
                     return BlockMap.this.new LazyEntry(this.currentPrimaryEntry.getLongKey(), next.getIntKey(), next.getValue());
                  }

                  public Entry<BlockVector3, V> next() {
                     if (!this.hasNext()) {
                        throw new NoSuchElementException();
                     } else {
                        BlockMap<V>.LazyEntry tmp = this.next;
                        this.next = null;
                        return tmp;
                     }
                  }

                  @Override
                  public void remove() {
                     this.secondaryIterator.remove();
                     if (((Int2ObjectMap)this.currentPrimaryEntry.getValue()).isEmpty()) {
                        this.primaryIterator.remove();
                     }
                  }
               };
            }

            @Override
            public int size() {
               return BlockMap.this.size();
            }
         };
      }

      return es;
   }

   @Override
   public boolean containsValue(Object value) {
      return this.maps.values().stream().anyMatch(m -> m.containsValue(value));
   }

   @Override
   public boolean containsKey(Object key) {
      BlockVector3 vec = (BlockVector3)key;
      Int2ObjectMap<V> activeMap = (Int2ObjectMap<V>)this.maps.get(toGroupKey(vec));
      return activeMap == null ? false : activeMap.containsKey(toInnerKey(vec));
   }

   @Override
   public V get(Object key) {
      BlockVector3 vec = (BlockVector3)key;
      Int2ObjectMap<V> activeMap = (Int2ObjectMap<V>)this.maps.get(toGroupKey(vec));
      return (V)(activeMap == null ? null : activeMap.get(toInnerKey(vec)));
   }

   @Override
   public V remove(Object key) {
      BlockVector3 vec = (BlockVector3)key;
      long groupKey = toGroupKey(vec);
      Int2ObjectMap<V> activeMap = (Int2ObjectMap<V>)this.maps.get(groupKey);
      if (activeMap == null) {
         return null;
      } else {
         V removed = (V)activeMap.remove(toInnerKey(vec));
         if (activeMap.isEmpty()) {
            this.maps.remove(groupKey);
         }

         return removed;
      }
   }

   @Override
   public void putAll(Map<? extends BlockVector3, ? extends V> m) {
      if (m instanceof BlockMap) {
         ((BlockMap)m).maps.forEach((groupKey, map) -> this.getOrCreateMap(groupKey).putAll(map));
      } else {
         super.putAll(m);
      }
   }

   @Override
   public void clear() {
      this.maps.clear();
   }

   @Override
   public int size() {
      return this.maps.values().stream().mapToInt(Map::size).sum();
   }

   @Override
   public Collection<V> values() {
      Collection<V> vs = this.values;
      if (vs == null) {
         this.values = vs = new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
               return BlockMap.this.maps.values().stream().flatMap(m -> m.values().stream()).iterator();
            }

            @Override
            public int size() {
               return BlockMap.this.size();
            }
         };
      }

      return vs;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return o instanceof BlockMap<V> other ? this.maps.equals(other.maps) : super.equals(o);
      }
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   private final class LazyEntry implements Entry<BlockVector3, V> {
      private final long groupKey;
      private final int innerKey;
      private BlockVector3 lazyKey;
      private V value;

      private LazyEntry(long groupKey, int innerKey, V value) {
         this.groupKey = groupKey;
         this.innerKey = innerKey;
         this.value = value;
      }

      public BlockVector3 getKey() {
         BlockVector3 result = this.lazyKey;
         if (result == null) {
            this.lazyKey = result = BlockMap.reconstructLocation(this.groupKey, this.innerKey);
         }

         return result;
      }

      @Override
      public V getValue() {
         return this.value;
      }

      @Override
      public V setValue(V value) {
         this.value = value;
         return (V)BlockMap.this.getOrCreateMap(this.groupKey).put(this.innerKey, value);
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof Entry<?, ?> e)) {
            return false;
         } else {
            return o instanceof BlockMap<V>.LazyEntry otherE
               ? otherE.groupKey == this.groupKey && otherE.innerKey == this.innerKey && Objects.equals(this.value, e.getValue())
               : Objects.equals(this.getKey(), e.getKey()) && Objects.equals(this.value, e.getValue());
         }
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.getKey()) ^ Objects.hashCode(this.value);
      }

      @Override
      public String toString() {
         return this.getKey() + "=" + this.getValue();
      }
   }
}
