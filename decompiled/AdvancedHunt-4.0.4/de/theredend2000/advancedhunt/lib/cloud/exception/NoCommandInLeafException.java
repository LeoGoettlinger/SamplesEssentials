package de.theredend2000.advancedhunt.lib.cloud.exception;

import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public final class NoCommandInLeafException extends IllegalStateException {
   private final CommandComponent<?> commandComponent;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
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
