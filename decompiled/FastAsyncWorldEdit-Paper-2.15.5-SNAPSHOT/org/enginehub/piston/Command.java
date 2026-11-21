package org.enginehub.piston;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.util.formatting.text.Component;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.suggestion.SuggestionProvider;
import org.enginehub.piston.util.HelpGenerator;

public interface Command {
   String getName();

   ImmutableList<String> getAliases();

   Component getDescription();

   Optional<Component> getFooter();

   ImmutableList<CommandPart> getParts();

   Command.Condition getCondition();

   Command.Action getAction();

   SuggestionProvider getSuggester();

   Command.Builder toBuilder();

   default Component getUsage() {
      return HelpGenerator.create(ImmutableList.of(this)).getUsage();
   }

   default Component getFullHelp() {
      return HelpGenerator.create(ImmutableList.of(this)).getFullHelp();
   }

   public interface Action {
      Command.Action NULL_ACTION = params -> 0;

      int run(CommandParameters var1) throws Exception;
   }

   public interface Builder {
      Command.Builder name(String var1);

      Command.Builder aliases(Collection<String> var1);

      Command.Builder description(Component var1);

      Command.Builder footer(@Nullable Component var1);

      Command.Builder parts(Collection<CommandPart> var1);

      Command.Builder addPart(CommandPart var1);

      Command.Builder addParts(CommandPart... var1);

      Command.Builder addParts(Iterable<CommandPart> var1);

      Command.Builder action(Command.Action var1);

      Command.Builder condition(Command.Condition var1);

      Command.Builder suggester(SuggestionProvider var1);

      Command build();
   }

   public interface Condition {
      Command.Condition TRUE = p -> true;
      Command.Condition FALSE = p -> false;

      boolean satisfied(InjectedValueAccess var1);

      default Command.Condition and(Command.Condition other) {
         return c -> this.satisfied(c) && other.satisfied(c);
      }

      default Command.Condition or(Command.Condition other) {
         return c -> this.satisfied(c) || other.satisfied(c);
      }

      default Command.Condition not() {
         return c -> !this.satisfied(c);
      }

      default <T extends Command.Condition> Optional<T> as(Class<T> type) {
         return type.isInstance(this) ? Optional.of(type.cast(this)) : Optional.empty();
      }
   }
}
