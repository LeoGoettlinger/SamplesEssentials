package me.byteful.plugin.leveltools.libs.lamp.command;

import java.util.Map;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.command.trait.PermissionHolder;
import me.byteful.plugin.leveltools.libs.lamp.core.CommandPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public interface CommandCategory extends PermissionHolder, Comparable<CommandCategory> {
   @NotNull
   String getName();

   @NotNull
   CommandPath getPath();

   @NotNull
   CommandHandler getCommandHandler();

   @Nullable
   CommandCategory getParent();

   @Nullable
   ExecutableCommand getDefaultAction();

   @NotNull
   @Override
   CommandPermission getPermission();

   boolean isSecret();

   boolean isEmpty();

   @NotNull
   @UnmodifiableView
   Map<CommandPath, CommandCategory> getCategories();

   @NotNull
   @UnmodifiableView
   Map<CommandPath, ExecutableCommand> getCommands();
}
