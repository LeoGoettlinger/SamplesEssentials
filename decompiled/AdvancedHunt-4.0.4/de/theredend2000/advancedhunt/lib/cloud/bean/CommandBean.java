package de.theredend2000.advancedhunt.lib.cloud.bean;

import de.theredend2000.advancedhunt.lib.cloud.Command;
import de.theredend2000.advancedhunt.lib.cloud.CommandFactory;
import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.execution.CommandExecutionHandler;
import de.theredend2000.advancedhunt.lib.cloud.meta.CommandMeta;
import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
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
