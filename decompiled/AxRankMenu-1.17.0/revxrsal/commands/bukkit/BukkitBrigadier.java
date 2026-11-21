package revxrsal.commands.bukkit;

import com.mojang.brigadier.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.brigadier.ArgumentTypeResolver;
import revxrsal.commands.bukkit.brigadier.MinecraftArgumentType;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;

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
