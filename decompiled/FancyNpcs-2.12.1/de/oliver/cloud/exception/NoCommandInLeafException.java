package de.oliver.cloud.exception;

import de.oliver.cloud.component.CommandComponent;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public final class NoCommandInLeafException extends IllegalStateException {
   private final CommandComponent<?> commandComponent;

   @API(
      status = Status.INTERNAL,
      consumers = {"de.oliver.cloud.*"}
   )
   public NoCommandInLeafException(@NonNull final CommandComponent<?> commandComponent) {
      super(String.format("Leaf node '%s' does not have associated owning command", commandComponent.name()));
      this.commandComponent = commandComponent;
   }

   @NonNull
   public CommandComponent<?> commandComponent() {
      return this.commandComponent;
   }
}
