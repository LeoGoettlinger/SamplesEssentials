package com.artillexstudios.axafkzone.libs.lamp.bukkit;

import com.artillexstudios.axafkzone.libs.lamp.CommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.core.BukkitActor;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.exception.SenderNotConsoleException;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.exception.SenderNotPlayerException;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
