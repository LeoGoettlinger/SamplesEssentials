package revxrsal.commands.bukkit;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.core.BukkitActor;
import revxrsal.commands.bukkit.exception.SenderNotConsoleException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.command.CommandActor;

public interface BukkitCommandActor extends CommandActor {
   @NotNull
   CommandSender getSender();

   boolean isPlayer();

   boolean isConsole();

   @Nullable
   Player getAsPlayer();

   @NotNull
   Player requirePlayer() throws SenderNotPlayerException;

   @NotNull
   ConsoleCommandSender requireConsole() throws SenderNotConsoleException;

   @NotNull
   Audience audience();

   void reply(@NotNull ComponentLike component);

   BukkitCommandHandler getCommandHandler();

   @NotNull
   static BukkitCommandActor wrap(@NotNull CommandSender sender, @NotNull CommandHandler handler) {
      return new BukkitActor(sender, handler);
   }
}
