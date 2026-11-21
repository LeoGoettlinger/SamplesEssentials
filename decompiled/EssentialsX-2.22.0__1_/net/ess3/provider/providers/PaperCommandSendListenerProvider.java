package net.ess3.provider.providers;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Collection;
import java.util.function.Predicate;
import net.ess3.provider.CommandSendListenerProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PaperCommandSendListenerProvider extends CommandSendListenerProvider {
   public PaperCommandSendListenerProvider(CommandSendListenerProvider.Filter commandFilter) {
      super(commandFilter);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onAsyncCommandSend(AsyncPlayerSendCommandsEvent<?> event) {
      if (event.isAsynchronous() || !event.hasFiredAsync()) {
         Collection<? extends CommandNode<?>> children = event.getCommandNode().getChildren();
         Predicate<String> filter = this.filter(event.getPlayer());
         children.removeIf(node -> filter.test(node.getName()));
      }
   }
}
