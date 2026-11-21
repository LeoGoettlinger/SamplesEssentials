package com.viaversion.viaversion.libs.fastutil.io;

import com.viaversion.viaversion.libs.fastutil.longs.LongIterator;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;

final class BinIO$LongDataInputWrapper implements LongIterator {
   private final DataInput dataInput;
   private boolean toAdvance = true;
   private boolean endOfProcess = false;
   private long next;

   public BinIO$LongDataInputWrapper(DataInput dataInput) {
      this.dataInput = dataInput;
   }

   @Override
   public boolean hasNext() {
      if (!this.toAdvance) {
         return !this.endOfProcess;
      } else {
         this.toAdvance = false;

         try {
            this.next = this.dataInput.readLong();
         } catch (EOFException var2) {
            this.endOfProcess = true;
         } catch (IOException var3) {
            throw new RuntimeException(var3);
         }

         return !this.endOfProcess;
      }
   }

   @Override
   public long nextLong() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         this.toAdvance = true;
         return this.next;
      }
   }
}
