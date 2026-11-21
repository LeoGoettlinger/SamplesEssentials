package vrx.economy.vRXClans.libs.cloud.services;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class PipelineException extends RuntimeException {
   public PipelineException(@NonNull final Exception cause) {
      super(cause);
   }

   public PipelineException(@NonNull final String message, @NonNull final Exception cause) {
      super(message, cause);
   }
}
