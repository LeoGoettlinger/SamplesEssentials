package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;

public class ChunkBiomesType1_21_5 extends ChunkBiomesType1_19_4 {
   public ChunkBiomesType1_21_5(int ySectionCount, int globalPaletteBiomeBits) {
      super(ySectionCount, new PaletteType1_21_5(PaletteType.BIOMES, globalPaletteBiomeBits));
   }
}
