package com.fastasyncworldedit.core.jnbt.streamer;

import java.io.IOException;

public interface IntValueReader extends ValueReader<Integer> {
   @Override
   void applyInt(int index, int value) throws IOException;

   default void apply(int index, Integer value) throws IOException {
      this.applyInt(index, value);
   }
}
