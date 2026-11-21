package de.theredend2000.advancedhunt.lib.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.ConsoleCommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ConsoleSource extends GenericSource {
   ConsoleSource(final CommandSourceStack commandSourceStack) {
      super(commandSourceStack);
   }

   @NonNull
   public ConsoleCommandSender source() {
      return (ConsoleCommandSender)super.source();
   }
}
