package org.enginehub.piston.impl;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.util.formatting.text.Component;
import java.util.Collection;
import javax.annotation.Nullable;
import org.enginehub.piston.Command;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.suggestion.DefaultSuggestionProvider;
import org.enginehub.piston.suggestion.SuggestionProvider;

@AutoValue
abstract class CommandImpl implements Command {
   static CommandImpl.Builder builder(String name) {
      return new AutoValue_CommandImpl.Builder()
         .footer(null)
         .condition(Command.Condition.TRUE)
         .name(name)
         .aliases(ImmutableList.of())
         .parts(ImmutableList.of())
         .action(Command.Action.NULL_ACTION)
         .suggester(DefaultSuggestionProvider.getInstance());
   }

   public abstract CommandImpl.Builder toBuilder();

   @com.google.auto.value.AutoValue.Builder
   interface Builder extends Command.Builder {
      CommandImpl.Builder name(String var1);

      CommandImpl.Builder aliases(Collection<String> var1);

      CommandImpl.Builder description(Component var1);

      CommandImpl.Builder footer(@Nullable Component var1);

      CommandImpl.Builder parts(Collection<CommandPart> var1);

      CommandImpl.Builder action(Command.Action var1);

      CommandImpl.Builder condition(Command.Condition var1);

      CommandImpl.Builder suggester(SuggestionProvider var1);

      com.google.common.collect.ImmutableList.Builder<CommandPart> partsBuilder();

      default CommandImpl.Builder addPart(CommandPart part) {
         this.partsBuilder().add(part);
         return this;
      }

      default CommandImpl.Builder addParts(CommandPart... parts) {
         this.partsBuilder().add(parts);
         return this;
      }

      default CommandImpl.Builder addParts(Iterable<CommandPart> parts) {
         this.partsBuilder().addAll(parts);
         return this;
      }

      CommandImpl autoBuild();

      default CommandImpl build() {
         CommandImpl auto = this.autoBuild();
         Preconditions.checkState(auto.getName().length() > 0, "command name must not be empty");
         return auto;
      }
   }
}
