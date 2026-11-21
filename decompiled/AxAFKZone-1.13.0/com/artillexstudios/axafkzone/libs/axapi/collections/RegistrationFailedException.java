package com.artillexstudios.axafkzone.libs.axapi.collections;

public class RegistrationFailedException extends Exception {
   private final Object key;
   private final RegistrationFailedException.Cause cause;

   public RegistrationFailedException(Object key, RegistrationFailedException.Cause cause) {
      this.key = key;
      this.cause = cause;
   }

   public RegistrationFailedException(String message, Object key, RegistrationFailedException.Cause cause) {
      super(message);
      this.key = key;
      this.cause = cause;
   }

   public RegistrationFailedException(String message, Throwable throwable, Object key, RegistrationFailedException.Cause cause) {
      super(message, throwable);
      this.key = key;
      this.cause = cause;
   }

   public RegistrationFailedException(Throwable throwable, Object key, RegistrationFailedException.Cause cause) {
      super(throwable);
      this.key = key;
      this.cause = cause;
   }

   public Object key() {
      return this.key;
   }

   public RegistrationFailedException.Cause cause() {
      return this.cause;
   }

   public static enum Cause {
      ALREADY_PRESENT,
      NOT_PRESENT;
   }
}
