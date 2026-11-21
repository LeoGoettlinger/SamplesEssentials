package org.enginehub.piston.util;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.Iterator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import org.enginehub.piston.Command;
import org.enginehub.piston.config.ColorConfig;
import org.enginehub.piston.part.CommandPart;
import org.enginehub.piston.part.NoArgCommandFlag;
import org.enginehub.piston.part.SubCommandPart;

public class PartHelper {
   public static void appendUsage(Stream<CommandPart> parts, TextComponent.Builder output) {
      Builder<Component> other = Stream.builder();
      SortedSet<Character> flags = new TreeSet<>();
      Iterator<CommandPart> iterator = parts.iterator();
      Builder<Component> postOSC = Stream.builder();
      SubCommandPart optionalSubCommand = null;

      while (iterator.hasNext()) {
         CommandPart part = iterator.next();
         if (part instanceof NoArgCommandFlag) {
            Preconditions.checkState(optionalSubCommand == null, "All flags should come before sub-commands.");
            flags.add(((NoArgCommandFlag)part).getName());
         } else if (part instanceof SubCommandPart && !part.isRequired()) {
            optionalSubCommand = (SubCommandPart)part;
         } else {
            (optionalSubCommand == null ? other : postOSC).add(part.getTextRepresentation());
         }
      }

      Stream<Component> flagsString = Optional.of(flags)
         .filter(x -> !x.isEmpty())
         .map(f -> f.stream().map(String::valueOf).collect(Collectors.joining("")))
         .map(text -> ColorConfig.partWrapping().wrap(TextComponent.of("["), ColorConfig.mainText().wrap("-" + text), TextComponent.of("]")))
         .map(Stream::of)
         .orElse(Stream.empty());
      Stream<Component> afterFlags = optionalSubCommand == null
         ? other.build()
         : Stream.concat(other.build(), buildOptionalMerging(optionalSubCommand, postOSC.build()));
      Iterator<Component> usages = Stream.concat(flagsString, afterFlags).iterator();

      while (usages.hasNext()) {
         output.append(usages.next());
         if (usages.hasNext()) {
            output.append(TextComponent.of(" "));
         }
      }
   }

   private static Stream<Component> buildOptionalMerging(SubCommandPart optionalSubCommand, Stream<Component> postComponents) {
      return Stream.of(
         ColorConfig.partWrapping()
            .wrap(
               TextComponent.of("<"),
               optionalSubCommand.getCommands().stream().map(Command::getName).map(ColorConfig.mainText()::wrap).collect(ComponentHelper.joiningWithBar()),
               TextComponent.of("|"),
               postComponents.collect(ComponentHelper.joiningTexts(TextComponent.empty(), TextComponent.of(" "), TextComponent.empty())),
               TextComponent.of(">")
            )
      );
   }
}
