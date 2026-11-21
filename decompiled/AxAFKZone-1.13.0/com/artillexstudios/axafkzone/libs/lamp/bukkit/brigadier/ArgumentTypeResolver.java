package com.artillexstudios.axafkzone.libs.lamp.bukkit.brigadier;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import com.mojang.brigadier.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ArgumentTypeResolver {
   @Nullable
   ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter);

   @NotNull
   static ArgumentTypeResolver forType(Class<?> type, ArgumentType<?> argumentType) {
      return parameter -> parameter.getType() == type ? argumentType : null;
   }

   @NotNull
   static ArgumentTypeResolver forHierarchyType(Class<?> type, ArgumentType<?> argumentType) {
      return parameter -> parameter.getType() != type && !parameter.getType().isAssignableFrom(type) ? null : argumentType;
   }
}
