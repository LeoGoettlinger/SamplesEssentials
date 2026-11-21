package me.byteful.plugin.leveltools.libs.lamp.bukkit;

import com.mojang.brigadier.arguments.ArgumentType;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.brigadier.ArgumentTypeResolver;
import me.byteful.plugin.leveltools.libs.lamp.bukkit.brigadier.MinecraftArgumentType;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
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
