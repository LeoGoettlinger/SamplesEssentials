package com.artillexstudios.axtrade.libs.lamp.bukkit.core;

import com.artillexstudios.axtrade.libs.lamp.bukkit.BukkitCommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.process.SenderResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

enum BukkitSenderResolver implements SenderResolver {
   INSTANCE;

   @Override
   public boolean isCustomType(Class<?> type) {
      return CommandSender.class.isAssignableFrom(type);
   }

   @NotNull
   @Override
   public Object getSender(@NotNull Class<?> customSenderType, @NotNull CommandActor actor, @NotNull ExecutableCommand command) {
      BukkitCommandActor bActor = (BukkitCommandActor)actor;
      if (Player.class.isAssignableFrom(customSenderType)) {
         return bActor.requirePlayer();
      } else {
         return ConsoleCommandSender.class.isAssignableFrom(customSenderType) ? bActor.requireConsole() : bActor.getSender();
      }
   }
}
