package org.enginehub.piston.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import java.util.stream.IntStream;
import org.enginehub.piston.Command;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.ArgAcceptingCommandPart;
import org.enginehub.piston.part.ArgConsumingCommandPart;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.part.SubCommandPart;

class CommandInfo {
   final ImmutableList<ArgConsumingCommandPart> arguments;
   final ImmutableList<ArgAcceptingCommandPart> defaultProvided;
   final ImmutableMap<Character, CommandFlag> flags;
   final ImmutableTable<SubCommandPart, String, Command> subCommandTable;
   final int requiredParts;

   static CommandInfo from(Command command) {
      Builder<ArgConsumingCommandPart> arguments = ImmutableList.builder();
      Builder<ArgAcceptingCommandPart> defaultProvided = ImmutableList.builder();
      com.google.common.collect.ImmutableMap.Builder<Character, CommandFlag> flags = ImmutableMap.builder();
      com.google.common.collect.ImmutableTable.Builder<SubCommandPart, String, Command> subCommandTable = ImmutableTable.builder();
      boolean seenRequiredSubCommand = false;
      boolean seenOptionalArg = false;
      boolean middleOptionalArg = false;
      ImmutableList<CommandPart> parts = command.getParts();
      int requiredParts = 0;

      for (int i = 0; i < parts.size(); i++) {
         CommandPart part = (CommandPart)parts.get(i);
         if (!(part instanceof ArgAcceptingCommandFlag) && !(part instanceof NoArgCommandFlag)) {
            if (part instanceof CommandArgument) {
               if (part.isRequired() && seenOptionalArg) {
                  middleOptionalArg = true;
               }

               if (!part.isRequired()) {
                  seenOptionalArg = true;
               }

               arguments.add((ArgConsumingCommandPart)part);
            } else {
               if (!(part instanceof SubCommandPart)) {
                  throw new IllegalStateException("Unknown part implementation " + part);
               }

               if (part.isRequired()) {
                  Preconditions.checkState(i + 1 >= parts.size(), "Required sub-command must be last part.");
                  seenRequiredSubCommand = true;
               }

               SubCommandPart subCommandPart = (SubCommandPart)part;
               UnmodifiableIterator var13 = subCommandPart.getCommands().iterator();

               while (var13.hasNext()) {
                  Command cmd = (Command)var13.next();
                  subCommandTable.put(subCommandPart, cmd.getName(), cmd);
                  UnmodifiableIterator var15 = cmd.getAliases().iterator();

                  while (var15.hasNext()) {
                     String alias = (String)var15.next();
                     subCommandTable.put(subCommandPart, alias, cmd);
                  }
               }

               arguments.add(subCommandPart);
            }
         } else {
            CommandFlag flag = (CommandFlag)part;
            flags.put(flag.getName(), flag);
         }

         if (part.isRequired()) {
            requiredParts++;
         }

         if (part instanceof ArgAcceptingCommandPart argPart && argPart.getDefaults().size() > 0) {
            defaultProvided.add(argPart);
         }
      }

      Preconditions.checkState(!seenRequiredSubCommand || !middleOptionalArg, "Cannot have middle-filled optionals and sub-commands");
      ImmutableList<ArgConsumingCommandPart> commandArguments = arguments.build();
      int[] indexes = IntStream.range(0, commandArguments.size())
         .filter(idx -> commandArguments.get(idx) instanceof CommandArgument)
         .filter(idx -> ((CommandArgument)commandArguments.get(idx)).isVariable())
         .toArray();
      Preconditions.checkArgument(indexes.length <= 1, "Too many variable arguments");
      if (indexes.length > 0) {
         int varargIndex = indexes[0];
         Preconditions.checkArgument(varargIndex == commandArguments.size() - 1, "Variable argument must be the last argument");
      }

      return new CommandInfo(commandArguments, defaultProvided.build(), flags.build(), subCommandTable.build(), requiredParts);
   }

   CommandInfo(
      ImmutableList<ArgConsumingCommandPart> arguments,
      ImmutableList<ArgAcceptingCommandPart> defaultProvided,
      ImmutableMap<Character, CommandFlag> flags,
      ImmutableTable<SubCommandPart, String, Command> subCommandTable,
      int requiredParts
   ) {
      this.arguments = arguments;
      this.defaultProvided = defaultProvided;
      this.flags = flags;
      this.subCommandTable = subCommandTable;
      this.requiredParts = requiredParts;
   }
}
