package vrx.economy.vRXClans.libs.cloud.bukkit;

import io.leangen.geantyref.TypeToken;
import java.util.concurrent.Executor;
import org.apiguardian.api.API;
import org.bukkit.command.CommandSender;
import vrx.economy.vRXClans.libs.cloud.key.CloudKey;

public final class BukkitCommandContextKeys {
   public static final CloudKey<CommandSender> BUKKIT_COMMAND_SENDER = CloudKey.of("BukkitCommandSender", TypeToken.get(CommandSender.class));
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static final CloudKey<Executor> SENDER_SCHEDULER_EXECUTOR = CloudKey.of("SenderSchedulerExecutor", Executor.class);

   private BukkitCommandContextKeys() {
   }
}
