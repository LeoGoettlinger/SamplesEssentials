package com.artillexstudios.axtrade.libs.lamp.command;

import com.artillexstudios.axtrade.libs.lamp.CommandHandler;
import com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProvider;
import com.artillexstudios.axtrade.libs.lamp.command.trait.CommandAnnotationHolder;
import com.artillexstudios.axtrade.libs.lamp.command.trait.PermissionHolder;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterValidator;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
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
