package com.fastasyncworldedit.core.jnbt.streamer;

import java.io.IOException;

public interface InfoReader extends StreamReader<Integer> {
   void apply(int length, int type) throws IOException;

   default void apply(int i, Integer value) throws IOException {
      this.apply(i, value.intValue());
   }
}
