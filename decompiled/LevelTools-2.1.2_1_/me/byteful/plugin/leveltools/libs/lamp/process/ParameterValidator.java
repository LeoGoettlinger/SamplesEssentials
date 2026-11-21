package me.byteful.plugin.leveltools.libs.lamp.process;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

public interface ParameterValidator<T> {
   void validate(T var1, @NotNull CommandParameter var2, @NotNull CommandActor var3);
}
