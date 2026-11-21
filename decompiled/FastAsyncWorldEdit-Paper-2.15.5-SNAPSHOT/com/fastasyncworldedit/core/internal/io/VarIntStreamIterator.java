package com.fastasyncworldedit.core.internal.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator.OfInt;

public class VarIntStreamIterator implements OfInt {
   private final InputStream parent;
   private final int limit;
   private int index;
   private boolean hasNextInt;
   private int nextInt;

   public VarIntStreamIterator(final InputStream parent, int limit) {
      this.parent = parent;
      this.limit = limit;
   }

   @Override
   public boolean hasNext() {
      if (this.hasNextInt) {
         return true;
      } else if (this.index >= this.limit) {
         return false;
      } else {
         try {
            this.nextInt = this.readNextInt();
         } catch (IOException var2) {
            throw new RuntimeException(var2);
         }

         return this.hasNextInt = true;
      }
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

   private int readNextInt() throws IOException {
      int value = 0;

      for (int bitsRead = 0; this.index < this.limit; bitsRead += 7) {
         byte next = (byte)this.parent.read();
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
}
