package com.sk89q.worldedit;

import com.fastasyncworldedit.core.configuration.Caption;

public class MissingWorldException extends WorldEditException {
   public MissingWorldException() {
      super(Caption.of("worldedit.error.missing-world"));
   }
}
