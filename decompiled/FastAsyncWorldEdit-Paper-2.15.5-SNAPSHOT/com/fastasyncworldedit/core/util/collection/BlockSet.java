package com.fastasyncworldedit.core.util.collection;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.AbstractRegion;
import com.sk89q.worldedit.regions.RegionOperationException;
import java.util.Iterator;
import java.util.Set;

public abstract class BlockSet extends AbstractRegion {
   private final int chunkOffsetX;
   private final int chunkOffsetZ;
   private final int blockOffsetX;
   private final int blockOffsetZ;

   public BlockSet(int offsetX, int offsetZ) {
      super(null);
      this.chunkOffsetX = offsetX;
      this.chunkOffsetZ = offsetZ;
      this.blockOffsetX = offsetX << 4;
      this.blockOffsetZ = offsetZ << 4;
   }

   @Override
   public boolean contains(Object o) {
      try {
         return this.contains((BlockVector3)o);
      } catch (ClassCastException var3) {
         var3.printStackTrace();
         return false;
      }
   }

   @Override
   public boolean contains(BlockVector3 obj) {
      return this.contains(obj.x(), obj.y(), obj.z());
   }

   protected final int lowestBit(long bitBuffer) {
      long lowBit = Long.lowestOneBit(bitBuffer);
      return Long.bitCount(lowBit - 1L);
   }

   protected final int highestBit(long bitBuffer) {
      long lowBit = Long.highestOneBit(bitBuffer);
      return Long.bitCount(lowBit - 1L);
   }

   @Override
   public boolean isGlobal() {
      return false;
   }

   public final int getBlockOffsetX() {
      return this.blockOffsetX;
   }

   public int getBlockOffsetZ() {
      return this.blockOffsetZ;
   }

   public int getChunkOffsetX() {
      return this.chunkOffsetX;
   }

   public int getChunkOffsetZ() {
      return this.chunkOffsetZ;
   }

   public boolean add(BlockVector3 p) {
      return this.add(p.x(), p.y(), p.z());
   }

   public boolean remove(BlockVector3 p) {
      return this.remove(p.x(), p.y(), p.z());
   }

   @Override
   public boolean remove(Object o) {
      try {
         return this.remove((BlockVector3)o);
      } catch (ClassCastException var3) {
         var3.printStackTrace();
         return false;
      }
   }

   @Override
   public abstract boolean contains(int x, int y, int z);

   public abstract boolean add(int x, int y, int z);

   public abstract void set(int x, int y, int z);

   public abstract void clear(int x, int y, int z);

   public abstract boolean remove(int x, int y, int z);

   @Override
   public abstract Iterator<BlockVector3> iterator();

   @Override
   public abstract Set<BlockVector2> getChunks();

   @Override
   public abstract Set<BlockVector3> getChunkCubes();

   @Override
   public void expand(BlockVector3... changes) throws RegionOperationException {
   }

   @Override
   public void contract(BlockVector3... changes) throws RegionOperationException {
   }
}
