package com.fastasyncworldedit.core.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.RichParser;
import com.fastasyncworldedit.core.extent.clipboard.MultiClipboardHolder;
import com.fastasyncworldedit.core.function.pattern.RandomFullClipboardPattern;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class RandomFullClipboardPatternParser extends RichParser<Pattern> {
   public RandomFullClipboardPatternParser(WorldEdit worldEdit) {
      super(worldEdit, "#fullcopy");
   }

   @Override
   protected Stream<String> getSuggestions(String argumentInput, int index, ParserContext context) {
      switch (index) {
         case 0:
            if (!argumentInput.equals("#") && !argumentInput.equals("#c")) {
               if ("#copy".startsWith(argumentInput.toLowerCase(Locale.ROOT))) {
                  return Stream.of("#copy");
               }

               if ("#clipboard".startsWith(argumentInput.toLowerCase(Locale.ROOT))) {
                  return Stream.of("#clipboard");
               }

               return Stream.empty();
            }

            return Stream.of("#copy", "#clipboard");
         case 1:
         case 2:
            return SuggestionHelper.suggestBoolean(argumentInput);
         default:
            return Stream.empty();
      }
   }

   protected Pattern parseFromInput(@Nonnull String[] arguments, ParserContext context) throws InputParseException {
      if (arguments.length != 0 && arguments.length <= 3) {
         try {
            boolean rotate = arguments.length >= 2 && Boolean.parseBoolean(arguments[1]);
            boolean flip = arguments.length == 3 && Boolean.parseBoolean(arguments[2]);
            List<ClipboardHolder> clipboards;
            if (!"#copy".startsWith(arguments[0].toLowerCase(Locale.ROOT)) && !"#clipboard".startsWith(arguments[0].toLowerCase(Locale.ROOT))) {
               Actor player = context.requireActor();
               MultiClipboardHolder multi = ClipboardFormats.loadAllFromInput(player, arguments[0], ClipboardFormats.findByAlias("fast"), false);
               if (multi == null) {
                  multi = ClipboardFormats.loadAllFromInput(player, arguments[0], ClipboardFormats.findByAlias("mcedit"), false);
               }

               if (multi == null) {
                  multi = ClipboardFormats.loadAllFromInput(player, arguments[0], ClipboardFormats.findByAlias("sponge"), false);
               }

               if (multi == null) {
                  throw new InputParseException(Caption.of("fawe.error.parse.no-clipboard-source", arguments[0]));
               }

               clipboards = multi.getHolders();
            } else {
               ClipboardHolder clipboard = context.requireSession().getExistingClipboard();
               if (clipboard == null) {
                  throw new InputParseException(Caption.of("fawe.error.parse.no-clipboard", this.getPrefix()));
               }

               clipboards = Collections.singletonList(clipboard);
            }

            return new RandomFullClipboardPattern(clipboards, rotate, flip);
         } catch (IOException var8) {
            throw new InputParseException(TextComponent.of(var8.getMessage()), var8);
         }
      } else {
         throw new InputParseException(
            Caption.of("fawe.error.command.syntax", TextComponent.of(this.getPrefix() + "[pattern] (e.g. " + this.getPrefix() + "[#copy][true][false])"))
         );
      }
   }
}
