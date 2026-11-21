package vrx.economy.vRXClans.libs.cloud.help;

import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.Command;

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
