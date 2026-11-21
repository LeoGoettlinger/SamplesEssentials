package de.theredend2000.advancedhunt.lib.cloud.paper.util.sender;

import de.theredend2000.advancedhunt.lib.cloud.SenderMapper;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PaperSimpleSenderMapper implements SenderMapper<CommandSourceStack, Source> {
   @NonNull
   public static PaperSimpleSenderMapper simpleSenderMapper() {
      return new PaperSimpleSenderMapper();
   }

   PaperSimpleSenderMapper() {
   }

   @NonNull
   public Source map(@NonNull final CommandSourceStack base) {
      CommandSender commandSender = base.getSender();
      if (commandSender instanceof ConsoleCommandSender) {
         return new ConsoleSource(base);
      } else if (commandSender instanceof Player) {
         return new PlayerSource(base);
      } else {
         return (Source)(commandSender instanceof Entity ? new EntitySource(base) : new GenericSource(base));
      }
   }

   @NonNull
   public CommandSourceStack reverse(@NonNull final Source mapped) {
      return mapped.stack();
   }
}
