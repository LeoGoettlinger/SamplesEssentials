package revxrsal.commands.bukkit.core;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.exception.ArgumentParseException;
import revxrsal.commands.util.Strings;

public final class BukkitCommandExecutor implements TabExecutor {
   private final BukkitHandler handler;

   public BukkitCommandExecutor(BukkitHandler handler) {
      this.handler = handler;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      BukkitCommandActor actor = new BukkitActor(sender, this.handler);

      try {
         ArgumentStack arguments = ArgumentStack.parse(args);
         arguments.addFirst(Strings.stripNamespace(command.getName()));
         this.handler.dispatch(actor, arguments);
      } catch (Throwable var7) {
         this.handler.getExceptionHandler().handleException(var7, actor);
      }

      return true;
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      try {
         BukkitCommandActor actor = new BukkitActor(sender, this.handler);
         ArgumentStack arguments = ArgumentStack.parseForAutoCompletion(args);
         arguments.addFirst(Strings.stripNamespace(command.getName()));
         return this.handler.getAutoCompleter().complete(actor, arguments);
      } catch (ArgumentParseException var7) {
         return Collections.emptyList();
      }
   }
}
