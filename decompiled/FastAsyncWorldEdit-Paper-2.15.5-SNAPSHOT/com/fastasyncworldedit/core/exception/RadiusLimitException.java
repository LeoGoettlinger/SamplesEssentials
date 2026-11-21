package com.fastasyncworldedit.core.exception;

import com.sk89q.worldedit.WorldEditException;

public class RadiusLimitException extends WorldEditException {
   private final int maxRadius;

   public RadiusLimitException(int maxRadius) {
      this.maxRadius = maxRadius;
   }

   public int getMaxRadius() {
      return this.maxRadius;
   }
}
