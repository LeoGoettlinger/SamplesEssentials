package com.fastasyncworldedit.core.internal.simd;

import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

public interface VectorizedMask {
   default void processChunks(IChunk chunk, IChunkGet get, IChunkSet set) {
      VectorFacade setFassade = new VectorFacade(set);
      VectorFacade getFassade = new VectorFacade(get);

      for (int layer = get.getMinSectionPosition(); layer <= get.getMaxSectionPosition(); layer++) {
         setFassade.setLayer(layer);
         getFassade.setLayer(layer);
         char[] sectionSet = set.loadIfPresent(layer);
         if (sectionSet != null) {
            setFassade.setData(sectionSet);
            this.processSection(layer, setFassade, getFassade);
         }
      }
   }

   default void processSection(int layer, VectorFacade set, VectorFacade get) {
      VectorSpecies<Short> species = ShortVector.SPECIES_PREFERRED;

      for (int i = 0; i < 4096; i += species.length()) {
         set.setIndex(i);
         get.setIndex(i);
         this.processVector(set, get, species);
      }
   }

   default void processVector(VectorFacade set, VectorFacade get, VectorSpecies<Short> species) {
      ShortVector s = set.getOrZero(species);
      s = s.blend(0L, this.compareVector(set, get, species).not());
      set.setOrIgnore(s);
   }

   VectorMask<Short> compareVector(VectorFacade set, VectorFacade get, VectorSpecies<Short> species);
}
