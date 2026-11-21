package com.artillexstudios.axtrade.libs.lamp.process;

import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import org.jetbrains.annotations.NotNull;

public interface ParameterValidator<T> {
   void validate(T var1, @NotNull CommandParameter var2, @NotNull CommandActor var3);
}
