package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;

public class ChunkSectionType1_21_5 extends ChunkSectionType1_18 {
   public ChunkSectionType1_21_5(int globalPaletteBlockBits, int globalPaletteBiomeBits) {
      super(new PaletteType1_21_5(PaletteType.BLOCKS, globalPaletteBlockBits), new PaletteType1_21_5(PaletteType.BIOMES, globalPaletteBiomeBits));
   }
}
