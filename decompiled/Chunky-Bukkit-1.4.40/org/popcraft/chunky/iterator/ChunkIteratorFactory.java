package org.popcraft.chunky.iterator;

import org.popcraft.chunky.Selection;

public final class ChunkIteratorFactory {
   private ChunkIteratorFactory() {
   }

   public static ChunkIterator getChunkIterator(Selection selection, long count) {
      if (selection.pattern().getType().equals("world")) {
         return new WorldChunkIterator(selection);
      } else {
         String shape = selection.shape();
         if (!"rectangle".equals(shape) && !"ellipse".equals(shape) && !"oval".equals(shape)) {
            String var4 = selection.pattern().getType();

            return (ChunkIterator)(switch (var4) {
               case "loop" -> new Loop2ChunkIterator(selection, count);
               case "spiral" -> new SpiralChunkIterator(selection, count);
               case "csv" -> new CsvChunkIterator(selection, count);
               case "concentric" -> new ConcentricChunkIterator(selection, count);
               default -> new RegionChunkIterator(selection, count);
            });
         } else {
            return new Loop2ChunkIterator(selection, count);
         }
      }
   }

   public static ChunkIterator getChunkIterator(Selection selection) {
      return getChunkIterator(selection, 0L);
   }
}
