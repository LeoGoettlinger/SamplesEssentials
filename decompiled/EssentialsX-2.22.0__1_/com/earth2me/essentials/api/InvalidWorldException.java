package com.earth2me.essentials.api;

import net.ess3.api.TranslatableException;

@Deprecated
public class InvalidWorldException extends TranslatableException {
   private final String world;

   public InvalidWorldException(final String world) {
      super("invalidWorld");
      this.world = world;
   }

   public String getWorld() {
      return this.world;
   }
}
