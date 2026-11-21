package vrx.economy.vRXClans.libs.cloud.exception.handling;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface ExceptionHandler<C, T extends Throwable> {
   @NonNull
   static <C, T extends Throwable> ExceptionHandler<C, T> noopHandler() {
      return ctx -> {};
   }

   @NonNull
   static <C, T extends Throwable> ExceptionHandler<C, T> passThroughHandler() {
      return ctx -> {
         throw ctx.exception();
      };
   }

   @NonNull
   static <C, T extends Throwable> ExceptionHandler<C, T> passThroughHandler(@NonNull final Consumer<ExceptionContext<C, T>> consumer) {
      return ctx -> {
         consumer.accept(ctx);
         throw ctx.exception();
      };
   }

   @NonNull
   static <C, T extends Throwable> ExceptionHandler<C, T> unwrappingHandler(@NonNull final Predicate<Throwable> predicate) {
      return ctx -> {
         Throwable cause = ctx.exception().getCause();
         if (cause != null && predicate.test(cause)) {
            throw cause;
         } else {
            throw ctx.exception();
         }
      };
   }

   @NonNull
   static <C, T extends Throwable> ExceptionHandler<C, T> unwrappingHandler(@NonNull final Class<? extends Throwable> causeClass) {
      return unwrappingHandler(causeClass::isInstance);
   }

   @NonNull
   static <C, T extends Throwable> ExceptionHandler<C, T> unwrappingHandler() {
      return unwrappingHandler(throwable -> true);
   }

   void handle(@NonNull ExceptionContext<C, T> context) throws Throwable;
}
