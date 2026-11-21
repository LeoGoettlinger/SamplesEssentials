package de.theredend2000.advancedhunt.lib.xseries.profiles.exceptions;

public final class MojangAPIRetryException extends MojangAPIException {
   private final MojangAPIRetryException.Reason reason;

   public MojangAPIRetryException(MojangAPIRetryException.Reason reason, String message) {
      super(message);
      this.reason = reason;
   }

   public MojangAPIRetryException(MojangAPIRetryException.Reason reason, String message, Throwable cause) {
      super(message, cause);
      this.reason = reason;
   }

   public MojangAPIRetryException.Reason getReason() {
      return this.reason;
   }

   public static enum Reason {
      CONNECTION_RESET,
      CONNECTION_TIMEOUT,
      RATELIMITED;
   }
}
