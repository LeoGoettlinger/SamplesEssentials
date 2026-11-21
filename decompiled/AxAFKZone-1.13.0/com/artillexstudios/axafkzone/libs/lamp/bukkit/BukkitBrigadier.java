package com.artillexstudios.axafkzone.libs.lamp.bukkit;

import com.artillexstudios.axafkzone.libs.lamp.bukkit.brigadier.ArgumentTypeResolver;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.brigadier.MinecraftArgumentType;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import com.mojang.brigadier.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;

public interface BukkitBrigadier {
   void registerArgumentTypeResolver(@NotNull ArgumentTypeResolver resolver);

   void registerArgumentTypeResolver(int priority, @NotNull ArgumentTypeResolver resolver);

   void bind(@NotNull Class<?> type, @NotNull ArgumentTypeResolver resolver);

   void bind(@NotNull Class<?> type, @NotNull ArgumentType<?> argumentType);

   void bind(@NotNull Class<?> type, @NotNull MinecraftArgumentType argumentType);

   @NotNull
   ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter);

   @NotNull
   CommandActor wrapSource(@NotNull Object commandSource);

   void disableNativePlayerCompletion();

   boolean isNativePlayerCompletionEnabled();

   void showEnumsAsNativeLiterals(boolean show);

   boolean isShowEnumsAsNativeLiterals();

   void register();

   @NotNull
   BukkitCommandHandler getCommandHandler();
}
