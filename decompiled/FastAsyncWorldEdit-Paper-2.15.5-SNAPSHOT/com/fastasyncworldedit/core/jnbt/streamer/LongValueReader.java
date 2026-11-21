package com.fastasyncworldedit.core.jnbt.streamer;

import java.io.IOException;

public interface LongValueReader extends ValueReader<Long> {
   @Override
   void applyLong(int index, long value) throws IOException;

   default void apply(int index, Long value) throws IOException {
      this.applyLong(index, value);
   }
}
