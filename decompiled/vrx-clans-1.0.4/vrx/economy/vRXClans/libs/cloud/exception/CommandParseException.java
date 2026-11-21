package vrx.economy.vRXClans.libs.cloud.exception;

import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;

@API(
   status = API.Status.STABLE
)
public class CommandParseException extends IllegalArgumentException {
   private final Object commandSender;
   private final List<CommandComponent<?>> currentChain;

   @API(
      status = API.Status.INTERNAL,
      consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
   )
   protected CommandParseException(@NonNull final Object commandSender, @NonNull final List<CommandComponent<?>> currentChain) {
      this.commandSender = commandSender;
      this.currentChain = currentChain;
   }

   @NonNull
   public Object commandSender() {
      return this.commandSender;
   }

   @NonNull
   public List<CommandComponent<?>> currentChain() {
      return Collections.unmodifiableList(this.currentChain);
   }
}
