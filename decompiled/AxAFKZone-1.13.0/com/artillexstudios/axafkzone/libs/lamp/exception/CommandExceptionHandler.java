package com.artillexstudios.axafkzone.libs.lamp.exception;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
import org.jetbrains.annotations.NotNull;

public interface CommandExceptionHandler {
   void handleException(@NotNull Throwable var1, @NotNull CommandActor var2);
}
