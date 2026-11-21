package com.viaversion.nbt.limiter;

public interface TagLimiter {
   int DEFAULT_MAX_BYTES = 2097152;
   int DEFAULT_MAX_NESTING_LEVEL = 512;
   int DEFAULT_MAX_TAGS = 262144;

   static TagLimiter create(int maxBytes, int maxLevels) {
      return create(maxBytes, maxLevels, 262144);
   }

   static TagLimiter create(int maxBytes, int maxLevels, int maxTags) {
      return new TagLimiterImpl(maxBytes, maxLevels, maxTags);
   }

   static TagLimiter noop() {
      return NoopTagLimiter.INSTANCE;
   }

   void countBytes(long var1);

   void countTag();

   void checkLevel(int var1);

   default void countByte() {
      this.countBytes(1L);
   }

   default void countShort() {
      this.countBytes(2L);
   }

   default void countInt() {
      this.countBytes(4L);
   }

   default void countFloat() {
      this.countBytes(4L);
   }

   default void countLong() {
      this.countBytes(8L);
   }

   default void countDouble() {
      this.countBytes(8L);
   }

   int maxBytes();

   int maxLevels();

   int maxTags();

   long bytes();

   int tags();

   void reset();
}
