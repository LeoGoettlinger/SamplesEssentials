package me.byteful.plugin.leveltools.libs.lamp.command;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.command.trait.CommandAnnotationHolder;
import me.byteful.plugin.leveltools.libs.lamp.command.trait.PermissionHolder;
import me.byteful.plugin.leveltools.libs.lamp.core.CommandPath;
import me.byteful.plugin.leveltools.libs.lamp.process.ResponseHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

public interface ExecutableCommand extends CommandAnnotationHolder, PermissionHolder, Comparable<ExecutableCommand> {
   @NotNull
   String getName();

   @Range(
      from = 0L,
      to = Long.MAX_VALUE
   )
   int getId();

   @NotNull
   String getUsage();

   @Nullable
   String getDescription();

   @NotNull
   CommandPath getPath();

   @Nullable
   CommandCategory getParent();

   @NotNull
   @Unmodifiable
   List<CommandParameter> getParameters();

   @NotNull
   @Unmodifiable
   Map<Integer, CommandParameter> getValueParameters();

   @NotNull
   CommandHandler getCommandHandler();

   @NotNull
   <T> ResponseHandler<T> getResponseHandler();

   void execute(@NotNull CommandActor var1, @Nullable Collection<String> var2);

   void execute(@NotNull CommandActor var1, @Nullable String... var2);

   boolean isSecret();
}
