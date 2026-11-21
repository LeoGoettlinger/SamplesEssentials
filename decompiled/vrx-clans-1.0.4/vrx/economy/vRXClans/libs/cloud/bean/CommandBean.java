package vrx.economy.vRXClans.libs.cloud.bean;

import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.Command;
import vrx.economy.vRXClans.libs.cloud.CommandFactory;
import vrx.economy.vRXClans.libs.cloud.CommandManager;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.execution.CommandExecutionHandler;
import vrx.economy.vRXClans.libs.cloud.meta.CommandMeta;

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
