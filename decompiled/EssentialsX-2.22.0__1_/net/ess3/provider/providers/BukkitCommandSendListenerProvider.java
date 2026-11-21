package net.ess3.provider.providers;

import java.util.function.Predicate;
import net.ess3.provider.CommandSendListenerProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class BukkitCommandSendListenerProvider extends CommandSendListenerProvider {
   public BukkitCommandSendListenerProvider(CommandSendListenerProvider.Filter commandFilter) {
      super(commandFilter);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onCommandSend(PlayerCommandSendEvent event) {
      Predicate<String> filter = this.filter(event.getPlayer());
      event.getCommands().removeIf(filter);
   }
}
