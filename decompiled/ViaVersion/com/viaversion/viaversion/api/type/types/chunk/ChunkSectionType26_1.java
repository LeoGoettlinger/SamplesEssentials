package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import io.netty.buffer.ByteBuf;

public class ChunkSectionType26_1 extends ChunkSectionType1_18 {
   public ChunkSectionType26_1(int globalPaletteBlockBits, int globalPaletteBiomeBits) {
      super(new PaletteType1_21_5(PaletteType.BLOCKS, globalPaletteBlockBits), new PaletteType1_21_5(PaletteType.BIOMES, globalPaletteBiomeBits));
   }

   @Override
   public ChunkSection read(ByteBuf buffer) {
      ChunkSection chunkSection = new ChunkSectionImpl();
      chunkSection.setNonAirBlocksCount(buffer.readShort());
      chunkSection.setFluidCount(buffer.readShort());
      chunkSection.addPalette(PaletteType.BLOCKS, this.blockPaletteType.read(buffer));
      chunkSection.addPalette(PaletteType.BIOMES, this.biomePaletteType.read(buffer));
      return chunkSection;
   }

   @Override
   public void write(ByteBuf buffer, ChunkSection section) {
      buffer.writeShort(section.getNonAirBlocksCount());
      buffer.writeShort(section.getFluidCount());
      this.blockPaletteType.write(buffer, section.palette(PaletteType.BLOCKS));
      this.biomePaletteType.write(buffer, section.palette(PaletteType.BIOMES));
   }

   @Override
   public int serializedSize(Chunk chunk) {
      int length = 0;

      for (ChunkSection section : chunk.getSections()) {
         length += 4
            + this.blockPaletteType.serializedSize(section.palette(PaletteType.BLOCKS))
            + this.biomePaletteType.serializedSize(section.palette(PaletteType.BIOMES));
      }

      return length;
   }
}
