package com.sk89q.worldedit.util.collection;

import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap.BasicEntry;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;

class Int2BaseBlockMap extends AbstractInt2ObjectMap<BaseBlock> {
   private final Int2IntMap commonMap = new Int2IntOpenHashMap(64, 0.9F);
   private final Int2ObjectMap<BaseBlock> uncommonMap;

   Int2BaseBlockMap() {
      this.commonMap.defaultReturnValue(BlockStateIdAccess.invalidId());
      this.uncommonMap = new Int2ObjectOpenHashMap(1, 0.75F);
   }

   private static int optimizedInternalId(BaseBlock block) {
      return block.getNbtReference() != null ? BlockStateIdAccess.invalidId() : BlockStateIdAccess.getBlockStateId(block.toImmutableState());
   }

   private static BaseBlock assumeAsBlock(int id) {
      if (!BlockStateIdAccess.isValidInternalId(id)) {
         return null;
      } else {
         BlockState state = BlockStateIdAccess.getBlockStateById(id);
         if (state == null) {
            throw new IllegalStateException("No state for ID " + id);
         } else {
            return state.toBaseBlock();
         }
      }
   }

   public int size() {
      return this.commonMap.size() + this.uncommonMap.size();
   }

   public ObjectSet<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock>> int2ObjectEntrySet() {
      return new AbstractObjectSet<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock>>() {
         @Nonnull
         public ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock>> iterator() {
            return new ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock>>() {
               private final ObjectIterator<Entry> commonIter = Int2IntMaps.fastIterator(Int2BaseBlockMap.this.commonMap);
               private final ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock>> uncommonIter = Int2ObjectMaps.fastIterator(
                  Int2BaseBlockMap.this.uncommonMap
               );
               private boolean lastNextFromCommon = false;

               public boolean hasNext() {
                  return this.commonIter.hasNext() || this.uncommonIter.hasNext();
               }

               public it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock> next() {
                  if (this.commonIter.hasNext()) {
                     Entry e = (Entry)this.commonIter.next();
                     this.lastNextFromCommon = true;
                     return new BasicEntry(e.getIntKey(), Int2BaseBlockMap.assumeAsBlock(e.getIntValue()));
                  } else if (this.uncommonIter.hasNext()) {
                     this.lastNextFromCommon = false;
                     return (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock>)this.uncommonIter.next();
                  } else {
                     throw new NoSuchElementException();
                  }
               }

               public void remove() {
                  if (this.lastNextFromCommon) {
                     this.commonIter.remove();
                  } else {
                     this.uncommonIter.remove();
                  }
               }
            };
         }

         public int size() {
            return Int2BaseBlockMap.this.size();
         }
      };
   }

   public BaseBlock get(int key) {
      int oldId = this.commonMap.get(key);
      return !BlockStateIdAccess.isValidInternalId(oldId) ? (BaseBlock)this.uncommonMap.get(key) : assumeAsBlock(oldId);
   }

   public boolean containsKey(int k) {
      return this.commonMap.containsKey(k) || this.uncommonMap.containsKey(k);
   }

   public boolean containsValue(Object v) {
      BaseBlock block = (BaseBlock)v;
      int internalId = optimizedInternalId(block);
      return !BlockStateIdAccess.isValidInternalId(internalId) ? this.uncommonMap.containsValue(block) : this.commonMap.containsValue(internalId);
   }

   public BaseBlock put(int key, BaseBlock value) {
      int internalId = optimizedInternalId(value);
      if (!BlockStateIdAccess.isValidInternalId(internalId)) {
         BaseBlock old = (BaseBlock)this.uncommonMap.put(key, value);
         if (old == null) {
            int oldId = this.commonMap.remove(key);
            return assumeAsBlock(oldId);
         } else {
            return old;
         }
      } else {
         int oldId = this.commonMap.put(key, internalId);
         return BlockStateIdAccess.isValidInternalId(oldId) ? assumeAsBlock(oldId) : (BaseBlock)this.uncommonMap.remove(key);
      }
   }

   public BaseBlock remove(int key) {
      int removed = this.commonMap.remove(key);
      return !BlockStateIdAccess.isValidInternalId(removed) ? (BaseBlock)this.uncommonMap.remove(key) : assumeAsBlock(removed);
   }

   public void replaceAll(BiFunction<? super Integer, ? super BaseBlock, ? extends BaseBlock> function) {
      ObjectIterator<Entry> iter = Int2IntMaps.fastIterator(this.commonMap);

      while (iter.hasNext()) {
         Entry next = (Entry)iter.next();
         BaseBlock value = function.apply(next.getIntKey(), assumeAsBlock(next.getIntValue()));
         int internalId = optimizedInternalId(value);
         if (!BlockStateIdAccess.isValidInternalId(internalId)) {
            this.uncommonMap.put(next.getIntKey(), value);
            iter.remove();
         } else {
            next.setValue(internalId);
         }
      }

      iter = Int2ObjectMaps.fastIterator(this.uncommonMap);

      while (iter.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock> next = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<BaseBlock>)iter.next();
         BaseBlock value = function.apply(next.getIntKey(), (BaseBlock)next.getValue());
         int internalId = optimizedInternalId(value);
         if (!BlockStateIdAccess.isValidInternalId(internalId)) {
            next.setValue(value);
         } else {
            this.commonMap.put(next.getIntKey(), internalId);
            iter.remove();
         }
      }
   }
}
