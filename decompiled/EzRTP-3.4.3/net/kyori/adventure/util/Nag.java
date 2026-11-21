package net.kyori.adventure.util;

public abstract class Nag extends RuntimeException {
   private static final long serialVersionUID = -695562541413409498L;

   public static void print(final Nag nag) {
      nag.printStackTrace();
   }

   protected Nag(final String message) {
      super(message);
   }
}
