package com.artillexstudios.axtrade.libs.lamp.process;

import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public interface CommandCondition {
   void test(@NotNull CommandActor var1, @NotNull ExecutableCommand var2, @NotNull @Unmodifiable List<String> var3);
}
