package net.ess3.provider;

import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class CommandSendListenerProvider implements Provider, Listener {
   private final CommandSendListenerProvider.Filter commandFilter;

   protected CommandSendListenerProvider(CommandSendListenerProvider.Filter commandFilter) {
      this.commandFilter = commandFilter;
   }

   protected final Predicate<String> filter(final Player player) {
      return this.commandFilter.apply(player);
   }

   public interface Filter extends Function<Player, Predicate<String>> {
   }
}
