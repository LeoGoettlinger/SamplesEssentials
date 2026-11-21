package de.oliver.cloud.services.type;

import de.oliver.cloud.services.State;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface ConsumerService<Context> extends SideEffectService<Context>, Consumer<Context> {
   static void interrupt() throws ConsumerService.PipeBurst {
      throw new ConsumerService.PipeBurst();
   }

   @NonNull
   @Override
   default State handle(@NonNull final Context context) {
      try {
         this.accept(context);
      } catch (ConsumerService.PipeBurst var3) {
         return State.ACCEPTED;
      }

      return State.REJECTED;
   }

   @Override
   void accept(@NonNull Context context);

   public static final class PipeBurst extends RuntimeException {
      private PipeBurst() {
      }

      @Override
      public synchronized Throwable fillInStackTrace() {
         return this;
      }

      @Override
      public synchronized Throwable initCause(final Throwable cause) {
         return this;
      }
   }
}
