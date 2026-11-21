package de.oliver.cloud.bean;

import de.oliver.cloud.Command;
import de.oliver.cloud.CommandFactory;
import de.oliver.cloud.CommandManager;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.execution.CommandExecutionHandler;
import de.oliver.cloud.meta.CommandMeta;
import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public abstract class CommandBean<C> implements CommandExecutionHandler<C>, CommandFactory<C> {
   protected CommandBean() {
   }

   @NonNull
   @Override
   public List<Command<? extends C>> createCommands(@NonNull final CommandManager<C> commandManager) {
      Command.Builder<C> builder = commandManager.commandBuilder(this.properties().name(), this.properties().aliases(), this.meta()).handler(this);
      return Collections.singletonList(this.configure(builder).build());
   }

   @NonNull
   protected CommandMeta meta() {
      return CommandMeta.builder().build();
   }

   @NonNull
   protected abstract CommandProperties properties();

   @NonNull
   protected abstract Command.Builder<? extends C> configure(@NonNull Command.Builder<C> builder);

   @Override
   public void execute(@NonNull final CommandContext<C> commandContext) {
   }
}
