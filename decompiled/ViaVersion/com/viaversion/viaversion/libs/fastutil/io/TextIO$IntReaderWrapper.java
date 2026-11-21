package com.viaversion.viaversion.libs.fastutil.io;

import com.viaversion.viaversion.libs.fastutil.ints.IntIterator;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.NoSuchElementException;

final class TextIO$IntReaderWrapper implements IntIterator {
   private final BufferedReader reader;
   private boolean toAdvance = true;
   private String s;
   private int next;

   public TextIO$IntReaderWrapper(BufferedReader reader) {
      this.reader = reader;
   }

   @Override
   public boolean hasNext() {
      if (!this.toAdvance) {
         return this.s != null;
      } else {
         this.toAdvance = false;

         try {
            this.s = this.reader.readLine();
         } catch (EOFException var2) {
         } catch (IOException var3) {
            throw new RuntimeException(var3);
         }

         if (this.s == null) {
            return false;
         } else {
            this.next = Integer.parseInt(this.s.trim());
            return true;
         }
      }
   }

   @Override
   public int nextInt() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         this.toAdvance = true;
         return this.next;
      }
   }
}
