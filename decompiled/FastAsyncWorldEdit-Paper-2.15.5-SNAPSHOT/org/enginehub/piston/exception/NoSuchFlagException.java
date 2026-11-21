package org.enginehub.piston.exception;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.stream.Collectors;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandParseResult;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.part.CommandFlag;
import org.enginehub.piston.util.HelpGenerator;
import org.enginehub.piston.util.StreamHelper;

public class NoSuchFlagException extends UsageException {
   private final char requestedFlag;

   private static String getAllFlags(ImmutableList<Command> commands) {
      return StreamHelper.cast(((Command)Iterables.getLast(commands)).getParts().stream(), CommandFlag.class)
         .map(f -> String.valueOf(f.getName()))
         .collect(Collectors.joining());
   }

   private static Component getMessage(CommandParseResult parseResult, char requestedFlag) {
      TextComponent.Builder message = TextComponent.builder("");
      message.append(TextComponent.of("Flag '"))
         .append(ColorConfig.mainText().wrap(String.valueOf(requestedFlag)))
         .append(TextComponent.of("' is not a valid flag for "));
      message.append(HelpGenerator.create(parseResult).getFullName());
      String allFlags = getAllFlags(parseResult.getExecutionPath());
      if (allFlags.isEmpty()) {
         message.append(TextComponent.of(", as it does not have any flags"));
      } else {
         message.append(TextComponent.of(". Options: ")).append(ColorConfig.mainText().wrap(allFlags));
      }

      return message.build();
   }

   public NoSuchFlagException(CommandParseResult parseResult, char requestedFlag) {
      super(getMessage(parseResult, requestedFlag), parseResult);
      this.requestedFlag = requestedFlag;
   }

   public char getRequestedFlag() {
      return this.requestedFlag;
   }
}
