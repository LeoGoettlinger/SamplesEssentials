package com.artillexstudios.axtrade.libs.lamp.process;

import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;

public interface SenderResolver {
   boolean isCustomType(Class<?> var1);

   @NotNull
   Object getSender(@NotNull Class<?> var1, @NotNull CommandActor var2, @NotNull ExecutableCommand var3);
}
