package me.byteful.plugin.leveltools.libs.lamp.process;

import java.util.List;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public interface CommandCondition {
   void test(@NotNull CommandActor var1, @NotNull ExecutableCommand var2, @NotNull @Unmodifiable List<String> var3);
}
