package com.viaversion.nbt.limiter;

final class TagLimiterImpl implements TagLimiter {
   private final int maxBytes;
   private final int maxLevels;
   private final int maxTags;
   private long bytes;
   private int tags;

   TagLimiterImpl(int maxBytes, int maxLevels, int maxTags) {
      this.maxBytes = maxBytes;
      this.maxLevels = maxLevels;
      this.maxTags = maxTags;
   }

   @Override
   public void countBytes(long bytes) {
      this.bytes += bytes;
      if (this.bytes >= this.maxBytes) {
         throw new IllegalArgumentException("NBT data larger than expected (capped at " + this.maxBytes + ")");
      }
   }

   @Override
   public void countTag() {
      if (++this.tags >= this.maxTags) {
         throw new IllegalArgumentException("More NBT tags than expected (capped at " + this.maxTags + ")");
      }
   }

   @Override
   public void checkLevel(int nestedLevel) {
      if (nestedLevel >= this.maxLevels) {
         throw new IllegalArgumentException("Nesting level higher than expected (capped at " + this.maxLevels + ")");
      }
   }

   @Override
   public int maxBytes() {
      return this.maxBytes;
   }

   @Override
   public int maxLevels() {
      return this.maxLevels;
   }

   @Override
   public int maxTags() {
      return this.maxTags;
   }

   @Override
   public long bytes() {
      return this.bytes;
   }

   @Override
   public int tags() {
      return this.tags;
   }

   @Override
   public void reset() {
      this.bytes = 0L;
      this.tags = 0;
   }
}
