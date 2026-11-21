package vrx.economy.vRXClans.libs.cloud.description;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface CommandDescription extends Describable {
   @NonNull
   static CommandDescription empty() {
      return CommandDescriptionImpl.of(Description.empty(), Description.empty());
   }

   @NonNull
   static CommandDescription commandDescription(@NonNull final Description description, @NonNull final Description verboseDescription) {
      return CommandDescriptionImpl.of(description, verboseDescription);
   }

   @NonNull
   static CommandDescription commandDescription(@NonNull final Description description) {
      return CommandDescriptionImpl.of(description, description);
   }

   @NonNull
   static CommandDescription commandDescription(@NonNull final String description, @NonNull final String verboseDescription) {
      return CommandDescriptionImpl.of(Description.of(description), Description.of(verboseDescription));
   }

   @NonNull
   static CommandDescription commandDescription(@NonNull final String description) {
      return CommandDescriptionImpl.of(Description.of(description), Description.of(description));
   }

   @NonNull
   @Override
   Description description();

   @NonNull
   Description verboseDescription();

   default boolean isEmpty() {
      return this.description().equals(Description.empty());
   }
}
