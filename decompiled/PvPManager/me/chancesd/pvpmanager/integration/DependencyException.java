package me.chancesd.pvpmanager.integration;

public class DependencyException extends RuntimeException {
   private static final long serialVersionUID = 7591203082159266797L;
   private final Hook hook;

   public DependencyException(String message, Hook hook) {
      this(message, null, hook);
   }

   public DependencyException(String message, Throwable cause, Hook hook) {
      super(message, cause);
      this.hook = hook;
   }

   public Hook getHook() {
      return this.hook;
   }
}
