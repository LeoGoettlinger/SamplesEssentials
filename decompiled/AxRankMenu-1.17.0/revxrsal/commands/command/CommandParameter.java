package revxrsal.commands.command;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.command.trait.PermissionHolder;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;

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
