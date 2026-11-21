package com.viaversion.viaversion.api.type.types.chunk;

import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSectionImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class ChunkSectionType1_18 extends Type<ChunkSection> {
   protected final PaletteTypeBase blockPaletteType;
   protected final PaletteTypeBase biomePaletteType;

   public ChunkSectionType1_18(int globalPaletteBlockBits, int globalPaletteBiomeBits) {
      super(ChunkSection.class);
      this.blockPaletteType = new PaletteType1_18(PaletteType.BLOCKS, globalPaletteBlockBits);
      this.biomePaletteType = new PaletteType1_18(PaletteType.BIOMES, globalPaletteBiomeBits);
   }

   protected ChunkSectionType1_18(PaletteTypeBase blockPaletteType, PaletteTypeBase biomePaletteType) {
      super(ChunkSection.class);
      this.blockPaletteType = blockPaletteType;
      this.biomePaletteType = biomePaletteType;
   }

   public ChunkSection read(ByteBuf buffer) {
      ChunkSection chunkSection = new ChunkSectionImpl();
      chunkSection.setNonAirBlocksCount(buffer.readShort());
      chunkSection.addPalette(PaletteType.BLOCKS, this.blockPaletteType.read(buffer));
      chunkSection.addPalette(PaletteType.BIOMES, this.biomePaletteType.read(buffer));
      return chunkSection;
   }

   public void write(ByteBuf buffer, ChunkSection section) {
      buffer.writeShort(section.getNonAirBlocksCount());
      this.blockPaletteType.write(buffer, section.palette(PaletteType.BLOCKS));
      this.biomePaletteType.write(buffer, section.palette(PaletteType.BIOMES));
   }

   public int serializedSize(Chunk chunk) {
      int length = 0;

      for (ChunkSection section : chunk.getSections()) {
         length += 2
            + this.blockPaletteType.serializedSize(section.palette(PaletteType.BLOCKS))
            + this.biomePaletteType.serializedSize(section.palette(PaletteType.BIOMES));
      }

      return length;
   }
}
