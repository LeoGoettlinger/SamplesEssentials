package me.byteful.plugin.leveltools.libs.lamp.command;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.autocomplete.SuggestionProvider;
import me.byteful.plugin.leveltools.libs.lamp.command.trait.CommandAnnotationHolder;
import me.byteful.plugin.leveltools.libs.lamp.command.trait.PermissionHolder;
import me.byteful.plugin.leveltools.libs.lamp.process.ParameterResolver;
import me.byteful.plugin.leveltools.libs.lamp.process.ParameterValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface CommandParameter extends Comparable<CommandParameter>, PermissionHolder, CommandAnnotationHolder {
   @NotNull
   String getName();

   @Nullable
   String getDescription();

   int getMethodIndex();

   int getCommandIndex();

   @NotNull
   Class<?> getType();

   @NotNull
   Type getFullType();

   @NotNull
   @Unmodifiable
   List<String> getDefaultValue();

   boolean consumesAllString();

   Parameter getJavaParameter();

   @NotNull
   SuggestionProvider getSuggestionProvider();

   @NotNull
   @Unmodifiable
   List<ParameterValidator<Object>> getValidators();

   boolean isOptional();

   boolean isLastInMethod();

   boolean isSwitch();

   @NotNull
   String getSwitchName();

   boolean isFlag();

   @NotNull
   String getFlagName();

   boolean getDefaultSwitch();

   @NotNull
   <T> ParameterResolver<T> getResolver();

   @NotNull
   CommandHandler getCommandHandler();

   @NotNull
   ExecutableCommand getDeclaringCommand();

   @NotNull
   @Override
   CommandPermission getPermission();
}
