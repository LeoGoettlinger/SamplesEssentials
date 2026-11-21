package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.pattern.ClipboardPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class ClipboardPatternParser extends InputParser<Pattern> implements AliasedParser {
   private final List<String> aliases = ImmutableList.of("#clipboard", "#clipboard@");

   public ClipboardPatternParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      if (input.isEmpty()) {
         return Stream.of("#clipboard");
      } else {
         String[] offsetParts = input.split("@", 2);
         String firstLower = offsetParts[0].toLowerCase(Locale.ROOT);
         boolean isClip = "#clipboard".startsWith(firstLower);
         boolean isCopy = "#copy".startsWith(firstLower);
         if (isClip || isCopy) {
            if (offsetParts.length != 2) {
               if (isClip) {
                  return Stream.of("#clipboard", "#clipboard@[x,y,z]");
               }

               return Stream.of("#copy", "#copy@[x,y,z]");
            }

            String coords = offsetParts[1];
            if (coords.isEmpty()) {
               return Stream.of(input + "[x,y,z]");
            }
         }

         return Stream.empty();
      }
   }

   public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
      String[] offsetParts = input.split("@", 2);
      if (!offsetParts[0].equalsIgnoreCase("#clipboard") && !offsetParts[0].equalsIgnoreCase("#copy")) {
         return null;
      } else {
         LocalSession session = context.requireSession();
         BlockVector3 offset = BlockVector3.ZERO;
         if (offsetParts.length == 2) {
            String coords = offsetParts[1];
            if (coords.length() < 7 || coords.charAt(0) != '[' || coords.charAt(coords.length() - 1) != ']') {
               throw new InputParseException(Caption.of("worldedit.error.parser.clipboard.missing-offset"));
            }

            String[] offsetSplit = coords.substring(1, coords.length() - 1).split(",");
            if (offsetSplit.length != 3) {
               throw new InputParseException(Caption.of("worldedit.error.parser.clipboard.missing-coordinates"));
            }

            offset = BlockVector3.at(Integer.parseInt(offsetSplit[0]), Integer.parseInt(offsetSplit[1]), Integer.parseInt(offsetSplit[2]));
         }

         if (session != null) {
            try {
               ClipboardHolder holder = session.getClipboard();
               Clipboard clipboard = holder.getClipboard();
               return new ClipboardPattern(clipboard, offset);
            } catch (EmptyClipboardException var8) {
               throw new InputParseException(Caption.of("worldedit.error.empty-clipboard"));
            }
         } else {
            throw new InputParseException(Caption.of("worldedit.error.missing-session"));
         }
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }
}
