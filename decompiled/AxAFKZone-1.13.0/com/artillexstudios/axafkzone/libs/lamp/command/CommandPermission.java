package com.artillexstudios.axafkzone.libs.lamp.command;

import org.jetbrains.annotations.NotNull;

public interface CommandPermission {
   CommandPermission ALWAYS_TRUE = actor -> true;

   boolean canExecute(@NotNull CommandActor var1);
}
