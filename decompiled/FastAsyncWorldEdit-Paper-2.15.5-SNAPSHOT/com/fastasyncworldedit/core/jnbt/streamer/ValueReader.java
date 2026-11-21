package com.fastasyncworldedit.core.jnbt.streamer;

import java.io.IOException;

public interface ValueReader<T> extends StreamReader<T> {
   @Override
   void apply(int index, T value) throws IOException;

   default void applyInt(int index, int value) throws IOException {
      this.apply(index, (T)value);
   }

   default void applyLong(int index, long value) throws IOException {
      this.apply(index, (T)value);
   }

   default void applyFloat(int index, float value) throws IOException {
      this.apply(index, (T)value);
   }

   default void applyDouble(int index, double value) throws IOException {
      this.apply(index, (T)value);
   }
}
