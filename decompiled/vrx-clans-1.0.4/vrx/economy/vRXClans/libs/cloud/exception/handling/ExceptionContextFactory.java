package vrx.economy.vRXClans.libs.cloud.exception.handling;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;

@API(
   status = API.Status.INTERNAL
)
public final class ExceptionContextFactory<C> {
   private final ExceptionController<C> controller;

   public ExceptionContextFactory(@NonNull final ExceptionController<C> controller) {
      this.controller = controller;
   }

   @NonNull
   public <T extends Throwable> ExceptionContext<C, T> createContext(@NonNull final CommandContext<C> context, @NonNull final T exception) {
      return new ExceptionContext.ExceptionContextImpl<>(exception, context, this.controller);
   }
}
