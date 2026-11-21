package org.enginehub.piston;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.enginehub.piston.converter.ArgumentConverterStore;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.CommandExecutionException;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.suggestion.Suggestion;

public interface CommandManager extends ArgumentConverterStore {
   Command.Builder newCommand(String var1);

   void register(Command var1);

   default void register(String name, Consumer<Command.Builder> registrationProcess) {
      Command.Builder builder = this.newCommand(name);
      registrationProcess.accept(builder);
      this.register(builder.build());
   }

   default void registerManager(CommandManager manager) {
      manager.getAllCommands().forEach(this::register);
      this.registerConverters(manager);
   }

   Stream<Command> getAllCommands();

   default boolean containsCommand(String name) {
      return this.getCommand(name).isPresent();
   }

   Optional<Command> getCommand(String var1);

   ImmutableSet<Suggestion> getSuggestions(InjectedValueAccess var1, List<String> var2);

   CommandParseResult parse(InjectedValueAccess var1, List<String> var2);

   default int execute(InjectedValueAccess context, List<String> args) {
      CommandParseResult parse = this.parse(context, args);

      try {
         return parse.getPrimaryCommand().getAction().run(parse.getParameters());
      } catch (CommandException var5) {
         throw var5;
      } catch (Exception var6) {
         throw new CommandExecutionException(var6, parse.getExecutionPath());
      }
   }
}
