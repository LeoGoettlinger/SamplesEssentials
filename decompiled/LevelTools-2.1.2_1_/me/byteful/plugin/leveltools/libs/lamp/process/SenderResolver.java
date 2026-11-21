package me.byteful.plugin.leveltools.libs.lamp.process;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;

public interface SenderResolver {
   boolean isCustomType(Class<?> var1);

   @NotNull
   Object getSender(@NotNull Class<?> var1, @NotNull CommandActor var2, @NotNull ExecutableCommand var3);
}
