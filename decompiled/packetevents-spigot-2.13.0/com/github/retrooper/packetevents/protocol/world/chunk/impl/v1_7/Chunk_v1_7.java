package com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_7;

import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.ByteArray3d;
import com.github.retrooper.packetevents.protocol.world.chunk.NibbleArray3d;

public class Chunk_v1_7 implements BaseChunk {
   private final ByteArray3d blocks;
   private NibbleArray3d metadata;
   private NibbleArray3d blocklight;
   private NibbleArray3d skylight;
   private NibbleArray3d extendedBlocks;

   public Chunk_v1_7(boolean skylight, boolean extended) {
      this(
         new ByteArray3d(4096),
         new NibbleArray3d(4096),
         new NibbleArray3d(4096),
         skylight ? new NibbleArray3d(4096) : null,
         extended ? new NibbleArray3d(4096) : null
      );
   }

   public Chunk_v1_7(ByteArray3d blocks, NibbleArray3d metadata, NibbleArray3d blocklight, NibbleArray3d skylight, NibbleArray3d extendedBlocks) {
      this.blocks = blocks;
      this.metadata = metadata;
      this.blocklight = blocklight;
      this.skylight = skylight;
      this.extendedBlocks = extendedBlocks;
   }

   @Override
   public int getBlockId(int x, int y, int z) {
      int blockId = (this.blocks.get(x, y, z) & 0xFF) << 4;
      blockId |= this.metadata.get(x, y, z) & 15;
      if (this.extendedBlocks != null) {
         blockId |= (this.extendedBlocks.get(x, y, z) & 15) << 8;
      }

      return blockId;
   }

   @Override
   public void set(int x, int y, int z, int combinedID) {
      this.blocks.set(x, y, z, combinedID >> 4 & 0xFF);
      this.metadata.set(x, y, z, combinedID & 15);
      if (this.extendedBlocks != null) {
         this.extendedBlocks.set(x, y, z, combinedID >> 8 & 15);
      }
   }

   @Override
   public boolean isEmpty() {
      for (byte block : this.blocks.getData()) {
         if (block != 0) {
            return false;
         }
      }

      return true;
   }

   public ByteArray3d getBlocks() {
      return this.blocks;
   }

   public NibbleArray3d getMetadata() {
      return this.metadata;
   }

   public NibbleArray3d getBlockLight() {
      return this.blocklight;
   }

   public NibbleArray3d getSkyLight() {
      return this.skylight;
   }

   public NibbleArray3d getExtendedBlocks() {
      return this.extendedBlocks;
   }
}
