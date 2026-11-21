package com.fastasyncworldedit.core.internal.simd;

import com.fastasyncworldedit.core.queue.IBlocks;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorSpecies;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class VectorFacade {
   private final IBlocks blocks;
   private int layer;
   private int index;
   private char[] data;

   VectorFacade(final IBlocks blocks) {
      this.blocks = blocks;
   }

   public ShortVector get(VectorSpecies<Short> species) {
      if (this.data == null) {
         this.load();
      }

      return ShortVector.fromCharArray(species, this.data, this.index);
   }

   public ShortVector getOrZero(VectorSpecies<Short> species) {
      return this.data == null ? ShortVector.zero(species) : ShortVector.fromCharArray(species, this.data, this.index);
   }

   public void setOrIgnore(ShortVector vector) {
      if (this.data == null) {
         if (vector.eq((short)0).allTrue()) {
            return;
         }

         this.load();
      }

      vector.intoCharArray(this.data, this.index);
   }

   private void load() {
      this.data = this.blocks.load(this.layer);
   }

   public void setLayer(int layer) {
      this.layer = layer;
      this.data = null;
   }

   public void setIndex(int index) {
      this.index = index;
   }

   public void setData(char[] data) {
      this.data = data;
   }
}
