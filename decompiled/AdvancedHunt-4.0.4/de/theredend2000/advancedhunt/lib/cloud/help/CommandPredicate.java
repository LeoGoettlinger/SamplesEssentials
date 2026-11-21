package de.theredend2000.advancedhunt.lib.cloud.help;

import de.theredend2000.advancedhunt.lib.cloud.Command;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface CommandPredicate<C> extends Predicate<Command<C>> {
   @NonNull
   static <C> CommandPredicate<C> acceptAll() {
      return cmd -> true;
   }

   boolean test(@NonNull Command<C> command);
}
