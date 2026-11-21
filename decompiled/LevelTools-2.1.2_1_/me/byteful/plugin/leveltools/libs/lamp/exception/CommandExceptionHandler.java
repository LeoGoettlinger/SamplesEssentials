package me.byteful.plugin.leveltools.libs.lamp.exception;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import org.jetbrains.annotations.NotNull;

public interface CommandExceptionHandler {
   void handleException(@NotNull Throwable var1, @NotNull CommandActor var2);
}
