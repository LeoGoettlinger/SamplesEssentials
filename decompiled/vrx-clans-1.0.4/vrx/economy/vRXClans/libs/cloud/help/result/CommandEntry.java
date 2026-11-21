package vrx.economy.vRXClans.libs.cloud.help.result;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import vrx.economy.vRXClans.libs.cloud.Command;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface CommandEntry<C> extends Comparable<CommandEntry<C>> {
   @NonNull
   static <C> CommandEntry<C> of(@NonNull final Command<C> command, @NonNull final String syntax) {
      return CommandEntryImpl.of(command, syntax);
   }

   @NonNull
   Command<C> command();

   @NonNull
   String syntax();

   default int compareTo(@NonNull final CommandEntry<C> other) {
      return this.syntax().compareTo(other.syntax());
   }
}
