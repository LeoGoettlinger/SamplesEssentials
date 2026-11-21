package me.clip.placeholderapi.libs.kyori.adventure.nbt;

import java.io.IOException;

interface BinaryTagScope extends AutoCloseable {
   @Override
   void close() throws IOException;

   public static final class NoOp implements BinaryTagScope {
      static final BinaryTagScope.NoOp INSTANCE = new BinaryTagScope.NoOp();

      private NoOp() {
      }

      @Override
      public void close() {
      }
   }
}
