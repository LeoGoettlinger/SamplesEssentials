package com.sk89q.worldedit;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

public class UnknownDirectionException extends WorldEditException {
   private final String dir;

   public UnknownDirectionException(String dir) {
      super(Caption.of("worldedit.error.unknown-direction", TextComponent.of(dir)));
      this.dir = dir;
   }

   public String getDirection() {
      return this.dir;
   }
}
