package de.theredend2000.advancedhunt.lib.cloud.paper.util.sender;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

class GenericSource implements Source {
   private final CommandSourceStack commandSourceStack;

   GenericSource(@NonNull final CommandSourceStack commandSourceStack) {
      this.commandSourceStack = commandSourceStack;
   }

   @NonNull
   @Override
   public final CommandSourceStack stack() {
      return this.commandSourceStack;
   }

   @NonNull
   @Override
   public CommandSender source() {
      return this.commandSourceStack.getSender();
   }
}
