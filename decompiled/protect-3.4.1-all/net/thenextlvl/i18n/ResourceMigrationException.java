package net.thenextlvl.i18n;

public class ResourceMigrationException extends RuntimeException {
   public ResourceMigrationException(String message) {
      super(message);
   }

   public ResourceMigrationException(String message, Throwable cause) {
      super(message, cause);
   }
}
