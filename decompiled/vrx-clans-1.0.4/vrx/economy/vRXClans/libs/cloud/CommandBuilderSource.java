package vrx.economy.vRXClans.libs.cloud;

import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.description.Description;
import vrx.economy.vRXClans.libs.cloud.meta.CommandMeta;

@API(
   status = API.Status.STABLE
)
public interface CommandBuilderSource<C> {
   @NonNull
   default Command.Builder<C> commandBuilder(
      @NonNull final String name, @NonNull final Collection<String> aliases, @NonNull final Description description, @NonNull final CommandMeta meta
   ) {
      return this.decorateBuilder(Command.newBuilder(name, meta, description, aliases.toArray(new String[0])));
   }

   @NonNull
   default Command.Builder<C> commandBuilder(@NonNull final String name, @NonNull final Collection<String> aliases, @NonNull final CommandMeta meta) {
      return this.decorateBuilder(Command.newBuilder(name, meta, Description.empty(), aliases.toArray(new String[0])));
   }

   @NonNull
   default Command.Builder<C> commandBuilder(
      @NonNull final String name, @NonNull final CommandMeta meta, @NonNull final Description description, final String... aliases
   ) {
      return this.decorateBuilder(Command.newBuilder(name, meta, description, aliases));
   }

   @NonNull
   default Command.Builder<C> commandBuilder(@NonNull final String name, @NonNull final CommandMeta meta, final String... aliases) {
      return this.decorateBuilder(Command.newBuilder(name, meta, Description.empty(), aliases));
   }

   @NonNull
   default Command.Builder<C> commandBuilder(@NonNull final String name, @NonNull final Description description, final String... aliases) {
      return this.decorateBuilder(Command.newBuilder(name, this.createDefaultCommandMeta(), description, aliases));
   }

   @NonNull
   default Command.Builder<C> commandBuilder(@NonNull final String name, final String... aliases) {
      return this.decorateBuilder(Command.newBuilder(name, this.createDefaultCommandMeta(), Description.empty(), aliases));
   }

   @NonNull
   CommandMeta createDefaultCommandMeta();

   @API(
      status = API.Status.INTERNAL
   )
   @NonNull
   Command.Builder<C> decorateBuilder(@NonNull Command.Builder<C> builder);
}
