package vrx.economy.vRXClans.libs.cloud.execution;

import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(
   status = API.Status.INTERNAL,
   consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
)
@Generated(
   from = "CommandResult",
   generator = "Immutables"
)
@Immutable
final class CommandResultImpl<C> implements CommandResult<C> {
   @NonNull
   private final CommandContext<C> commandContext;

   private CommandResultImpl(@NonNull CommandContext<C> commandContext) {
      this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
   }

   private CommandResultImpl(CommandResultImpl<C> original, @NonNull CommandContext<C> commandContext) {
      this.commandContext = commandContext;
   }

   @NonNull
   @Override
   public CommandContext<C> commandContext() {
      return this.commandContext;
   }

   public final CommandResultImpl<C> withCommandContext(CommandContext<C> value) {
      if (this.commandContext == value) {
         return this;
      } else {
         CommandContext<C> newValue = Objects.requireNonNull(value, "commandContext");
         return new CommandResultImpl<>(this, newValue);
      }
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof CommandResultImpl && this.equalTo(0, (CommandResultImpl<?>)another);
   }

   private boolean equalTo(int synthetic, CommandResultImpl<?> another) {
      return this.commandContext.equals(another.commandContext);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      return h + (h << 5) + this.commandContext.hashCode();
   }

   @Override
   public String toString() {
      return "CommandResult{commandContext=" + this.commandContext + "}";
   }

   public static <C> CommandResultImpl<C> of(@NonNull CommandContext<C> commandContext) {
      return new CommandResultImpl<>(commandContext);
   }

   public static <C> CommandResultImpl<C> copyOf(CommandResult<C> instance) {
      return instance instanceof CommandResultImpl ? (CommandResultImpl)instance : of(instance.commandContext());
   }
}
