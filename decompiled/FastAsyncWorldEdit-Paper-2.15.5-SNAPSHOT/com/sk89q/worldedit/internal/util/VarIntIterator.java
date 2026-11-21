package com.sk89q.worldedit.internal.util;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator.OfInt;

public class VarIntIterator implements OfInt {
   private final byte[] source;
   private int index;
   private boolean hasNextInt;
   private int nextInt;

   public VarIntIterator(byte[] source) {
      this.source = source;
   }

   @Override
   public boolean hasNext() {
      if (this.hasNextInt) {
         return true;
      } else if (this.index >= this.source.length) {
         return false;
      } else {
         this.nextInt = this.readNextInt();
         return this.hasNextInt = true;
      }
   }

   private int readNextInt() {
      int value = 0;

      for (int bitsRead = 0; this.index < this.source.length; bitsRead += 7) {
         byte next = this.source[this.index];
         this.index++;
         value |= (next & 127) << bitsRead;
         if (bitsRead > 35) {
            throw new IllegalStateException("VarInt too big (probably corrupted data)");
         }

         if ((next & 128) == 0) {
            return value;
         }
      }

      throw new IllegalStateException("Ran out of bytes while reading VarInt (probably corrupted data)");
   }

   @Override
   public int nextInt() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         this.hasNextInt = false;
         return this.nextInt;
      }
   }
}
