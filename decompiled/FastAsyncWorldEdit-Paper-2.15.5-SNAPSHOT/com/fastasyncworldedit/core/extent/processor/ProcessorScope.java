package com.fastasyncworldedit.core.extent.processor;

public enum ProcessorScope {
   ADDING_BLOCKS(0),
   CHANGING_BLOCKS(1),
   REMOVING_BLOCKS(2),
   CUSTOM(3),
   READING_BLOCKS(5),
   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   READING_SET_BLOCKS(4);

   private final int value;

   private ProcessorScope(int value) {
      this.value = value;
   }

   public int intValue() {
      return this.value;
   }

   public static ProcessorScope valueOf(int value) {
      return switch (value) {
         case 0 -> ADDING_BLOCKS;
         case 1 -> CHANGING_BLOCKS;
         case 2 -> REMOVING_BLOCKS;
         default -> CUSTOM;
         case 4 -> READING_SET_BLOCKS;
         case 5 -> READING_BLOCKS;
      };
   }
}
