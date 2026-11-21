package vrx.economy.vRXClans.libs.cloud.exception;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;

@API(
   status = API.Status.STABLE
)
public final class NoCommandInLeafException extends IllegalStateException {
   private final CommandComponent<?> commandComponent;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
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
