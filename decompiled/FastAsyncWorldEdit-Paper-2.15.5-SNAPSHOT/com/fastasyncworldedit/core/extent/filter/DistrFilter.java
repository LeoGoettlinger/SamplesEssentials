package com.fastasyncworldedit.core.extent.filter;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.function.mask.ABlockMask;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DistrFilter extends ForkedFilter<DistrFilter> {
   private final int[] counter = new int[BlockTypesCache.states.length];

   public DistrFilter() {
      super(null);
   }

   private DistrFilter(DistrFilter root) {
      super(root);
   }

   public DistrFilter init() {
      return new DistrFilter(this);
   }

   public void join(DistrFilter filter) {
      for (int i = 0; i < filter.counter.length; i++) {
         this.counter[i] = this.counter[i] + filter.counter[i];
      }
   }

   @Override
   public final void applyBlock(FilterBlock block) {
      int ordinal = block.getOrdinal();
      if (ordinal == 0) {
         ordinal = 1;
      }

      this.counter[ordinal]++;
   }

   public int getTotal(ABlockMask mask) {
      int total = 0;

      for (int i = 0; i < this.counter.length; i++) {
         int value = this.counter[i];
         if (value != 0 && mask.test(BlockTypesCache.states[i])) {
            total += value;
         }
      }

      return total;
   }

   public int getTotal() {
      return Arrays.stream(this.counter).sum();
   }

   public List<Countable<BlockState>> getDistribution() {
      List<Countable<BlockState>> distribution = new ArrayList<>();

      for (int i = 0; i < this.counter.length; i++) {
         int count = this.counter[i];
         if (count != 0) {
            distribution.add(new Countable<>(BlockTypesCache.states[i], count));
         }
      }

      Collections.sort(distribution);
      return distribution;
   }

   public List<Countable<BlockType>> getTypeDistribution() {
      List<Countable<BlockType>> distribution = new ArrayList<>();
      int[] typeCounter = new int[BlockTypesCache.values.length];

      for (int i = 0; i < this.counter.length; i++) {
         int count = this.counter[i];
         if (count != 0) {
            BlockState state = BlockTypesCache.states[i];
            typeCounter[state.getBlockType().getInternalId()] += count;
         }
      }

      for (int ix = 0; ix < typeCounter.length; ix++) {
         int count = typeCounter[ix];
         if (count != 0) {
            distribution.add(new Countable<>(BlockTypesCache.values[ix], count));
         }
      }

      Collections.sort(distribution);
      return distribution;
   }

   public void print(Actor actor, long size) {
      for (Countable<BlockState> c : this.getDistribution()) {
         String name = c.getID().toString();
         String str = String.format("%-7s (%.3f%%) %s", c.getAmount(), (double)c.getAmount() / size * 100.0, name);
         actor.printInfo(TextComponent.of(str));
      }
   }
}
