package com.sk89q.worldedit.command.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.enginehub.piston.Command;
import org.enginehub.piston.inject.InjectedValueAccess;

public final class SubCommandPermissionCondition extends PermissionCondition {
   private final Command.Condition aggregate;

   private SubCommandPermissionCondition(Set<String> perms, Command.Condition aggregate) {
      super(perms);
      this.aggregate = aggregate;
   }

   @Override
   public boolean satisfied(InjectedValueAccess context) {
      return this.aggregate.satisfied(context);
   }

   public static class Generator {
      private final List<Command> subCommands;

      public Generator(Collection<? extends Command> subCommands) {
         this.subCommands = ImmutableList.copyOf(subCommands);
      }

      public Command.Condition build() {
         List<Command.Condition> conditions = this.subCommands.stream().map(Command::getCondition).collect(Collectors.toList());
         List<Optional<PermissionCondition>> permConds = conditions.stream().map(c -> c.as(PermissionCondition.class)).collect(Collectors.toList());
         if (permConds.stream().anyMatch(o -> !o.isPresent())) {
            return new PermissionCondition(ImmutableSet.of());
         } else {
            Set<String> perms = permConds.stream().map(Optional::get).flatMap(cond -> cond.getPermissions().stream()).collect(Collectors.toSet());
            Command.Condition aggregate = permConds.stream()
               .map(Optional::get)
               .map(c -> (Command.Condition)c)
               .reduce(Command.Condition::or)
               .orElse(Command.Condition.TRUE);
            return new SubCommandPermissionCondition(perms, aggregate);
         }
      }
   }
}
