package net.ess3.api;

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
