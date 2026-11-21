package de.theredend2000.advancedhunt.lib.cloud.description;

import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
@Generated(
   from = "CommandDescription",
   generator = "Immutables"
)
@Immutable
final class CommandDescriptionImpl implements CommandDescription {
   @NonNull
   private final Description description;
   @NonNull
   private final Description verboseDescription;

   private CommandDescriptionImpl(@NonNull Description description, @NonNull Description verboseDescription) {
      this.description = Objects.requireNonNull(description, "description");
      this.verboseDescription = Objects.requireNonNull(verboseDescription, "verboseDescription");
   }

   private CommandDescriptionImpl(CommandDescriptionImpl original, @NonNull Description description, @NonNull Description verboseDescription) {
      this.description = description;
      this.verboseDescription = verboseDescription;
   }

   @NonNull
   @Override
   public Description description() {
      return this.description;
   }

   @NonNull
   @Override
   public Description verboseDescription() {
      return this.verboseDescription;
   }

   public final CommandDescriptionImpl withDescription(Description value) {
      if (this.description == value) {
         return this;
      } else {
         Description newValue = Objects.requireNonNull(value, "description");
         return new CommandDescriptionImpl(this, newValue, this.verboseDescription);
      }
   }

   public final CommandDescriptionImpl withVerboseDescription(Description value) {
      if (this.verboseDescription == value) {
         return this;
      } else {
         Description newValue = Objects.requireNonNull(value, "verboseDescription");
         return new CommandDescriptionImpl(this, this.description, newValue);
      }
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof CommandDescriptionImpl && this.equalTo(0, (CommandDescriptionImpl)another);
   }

   private boolean equalTo(int synthetic, CommandDescriptionImpl another) {
      return this.description.equals(another.description) && this.verboseDescription.equals(another.verboseDescription);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.description.hashCode();
      return h + (h << 5) + this.verboseDescription.hashCode();
   }

   @Override
   public String toString() {
      return "CommandDescription{description=" + this.description + ", verboseDescription=" + this.verboseDescription + "}";
   }

   public static CommandDescriptionImpl of(@NonNull Description description, @NonNull Description verboseDescription) {
      return new CommandDescriptionImpl(description, verboseDescription);
   }

   public static CommandDescriptionImpl copyOf(CommandDescription instance) {
      return instance instanceof CommandDescriptionImpl ? (CommandDescriptionImpl)instance : of(instance.description(), instance.verboseDescription());
   }
}
