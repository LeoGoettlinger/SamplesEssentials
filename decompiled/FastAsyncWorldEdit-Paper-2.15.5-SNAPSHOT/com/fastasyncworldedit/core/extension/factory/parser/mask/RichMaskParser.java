package com.fastasyncworldedit.core.extension.factory.parser.mask;

import com.fastasyncworldedit.core.command.SuggestInputParseException;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.FaweParser;
import com.fastasyncworldedit.core.function.mask.BlockMaskBuilder;
import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.MaskUnion;
import com.sk89q.worldedit.internal.command.CommandArgParser;
import com.sk89q.worldedit.internal.util.Substring;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.inject.MemoizingValueAccess;
import org.enginehub.piston.suggestion.Suggestion;

public class RichMaskParser extends FaweParser<Mask> {
   public RichMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
      if (input.isEmpty()) {
         throw new SuggestInputParseException(
            Caption.of("fawe.error.no-input-provided"), () -> Stream.of("#", ",", "&").map(n -> n + ":").collect(Collectors.toList())
         );
      } else {
         Extent extent = context.getExtent();
         if (extent == null) {
            extent = Request.request().getExtent();
         }

         List<List<Mask>> masks = new ArrayList<>();
         masks.add(new ArrayList<>());
         CommandLocals locals = new CommandLocals();
         Actor actor = context.getActor();
         if (actor != null) {
            locals.put(Actor.class, actor);
         }

         try {
            for (Entry<FaweParser.ParseEntry, List<String>> entry : parse(input)) {
               FaweParser.ParseEntry pe = entry.getKey();
               String command = pe.getInput();
               String full = pe.getFull();
               Mask mask = null;
               if (command.isEmpty()) {
                  mask = this.parseFromInput(StringMan.join(entry.getValue(), ','), context);
               } else if (!this.worldEdit.getMaskFactory().containsAlias(command)) {
                  char char0 = command.charAt(0);
                  boolean charMask = input.length() > 1 && input.charAt(1) != '[';
                  if (charMask && input.charAt(0) == '=') {
                     mask = this.parseFromInput(char0 + "[" + input.substring(1) + "]", context);
                  }

                  if (char0 == '#' && command.length() > 1 && command.charAt(1) != '#') {
                     throw new SuggestInputParseException(
                        new NoMatchException(
                           Caption.of(
                              "fawe.error.parse.unknown-mask",
                              full,
                              TextComponent.of("https://intellectualsites.github.io/fastasyncworldedit-documentation/patterns/patterns")
                                 .clickEvent(ClickEvent.openUrl("https://intellectualsites.github.io/fastasyncworldedit-documentation/patterns/patterns"))
                           )
                        ),
                        () -> full.length() == 1
                           ? new ArrayList<>(this.worldEdit.getMaskFactory().getSuggestions("", context))
                           : new ArrayList<>(this.worldEdit.getMaskFactory().getSuggestions(command.toLowerCase(Locale.ROOT), context))
                     );
                  }

                  if (charMask) {
                     switch (char0) {
                        case '!':
                        case '$':
                        case '%':
                        case '<':
                        case '>':
                           input = input.substring(input.indexOf(char0) + 1);
                           mask = this.parseFromInput(char0 + "[" + input + "]", context);
                           if (mask != null) {
                              return mask;
                           }
                           break;
                        case '#':
                           if (input.charAt(1) == '#') {
                              mask = this.worldEdit.getMaskFactory().parseWithoutRich(full, context);
                           }
                           break;
                        case '/':
                        case '\\':
                        case '{':
                        case '|':
                        case '~':
                           String value = command.substring(1) + (entry.getValue().isEmpty() ? "" : "[" + StringMan.join(entry.getValue(), "][") + "]");
                           if (value.contains(":")) {
                              if (value.charAt(0) == ':') {
                                 value = value.replaceFirst(":", "");
                              }

                              value = value.replaceAll(":", "][");
                           }

                           mask = this.parseFromInput(char0 + "[" + value + "]", context);
                     }
                  }

                  if (mask == null) {
                     if (command.startsWith("[")) {
                        int end = command.lastIndexOf(93);
                        mask = this.parseFromInput(command.substring(1, end == -1 ? command.length() : end), context);
                     } else {
                        BlockMaskBuilder builder = new BlockMaskBuilder(context);

                        try {
                           builder.addRegex(full);
                        } catch (InputParseException var21) {
                           builder.clear();
                           context.setPreferringWildcard(false);
                           context.setRestricted(false);
                           BaseBlock block = this.worldEdit.getBlockFactory().parseFromInput(full, context);
                           builder.add(block);
                        } catch (PatternSyntaxException var22) {
                           throw new SuggestInputParseException(
                              new NoMatchException(
                                 Caption.of(
                                    "fawe.error.parse.unknown-mask",
                                    full,
                                    TextComponent.of("https://intellectualsites.github.io/fastasyncworldedit-documentation/masks/masks")
                                       .clickEvent(ClickEvent.openUrl("https://intellectualsites.github.io/fastasyncworldedit-documentation/masks/masks"))
                                 )
                              ),
                              () -> full.length() == 1
                                 ? new ArrayList<>(this.worldEdit.getMaskFactory().getSuggestions("", context))
                                 : new ArrayList<>(this.worldEdit.getMaskFactory().getSuggestions(command.toLowerCase(Locale.ROOT), context))
                           );
                        }

                        mask = builder.build(extent);
                     }
                  }
               } else {
                  List<String> args = entry.getValue();

                  try {
                     mask = this.worldEdit.getMaskFactory().parseWithoutRich(full, context);
                  } catch (SuggestInputParseException var19) {
                     throw var19;
                  } catch (Throwable var20) {
                     throw SuggestInputParseException.of(
                        var20,
                        full,
                        () -> {
                           try {
                              String cmdArgs = args.isEmpty() ? "" : " " + StringMan.join(args, " ");
                              List<Substring> split = CommandArgParser.forArgString(cmdArgs).parseArgs().toList();
                              List<String> argStrings = split.stream().map(Substring::getSubstring).collect(Collectors.toList());
                              MemoizingValueAccess access = this.getPlatform().initializeInjectedValues(() -> cmdArgs, actor, null, true);
                              List<String> suggestions = this.getPlatform()
                                 .getCommandManager()
                                 .getSuggestions(access, argStrings)
                                 .stream()
                                 .map(Suggestion::getSuggestion)
                                 .collect(Collectors.toUnmodifiableList());
                              List<String> result = new ArrayList<>();
                              if (suggestions.size() <= 2) {
                                 for (int i = 0; i < suggestions.size(); i++) {
                                    String suggestion = suggestions.get(i);
                                    if (suggestion.indexOf(32) != 0) {
                                       String[] splitSuggestion = suggestion.split(" ");
                                       suggestion = "[" + StringMan.join((Object[])splitSuggestion, "][") + "]";
                                       result.set(i, suggestion);
                                    }
                                 }
                              }

                              return result;
                           } catch (Throwable var12x) {
                              var12x.printStackTrace();
                              throw new InputParseException(TextComponent.of(var12x.getMessage()));
                           }
                        }
                     );
                  }
               }

               if (pe.isAnd()) {
                  masks.add(new ArrayList<>());
               }

               masks.get(masks.size() - 1).add(mask);
            }
         } catch (InputParseException var23) {
            throw var23;
         } catch (Throwable var24) {
            var24.printStackTrace();
            throw new InputParseException(TextComponent.of(var24.getMessage()), var24);
         }

         List<Mask> maskUnions = new ArrayList<>();

         for (List<Mask> maskList : masks) {
            if (maskList.size() == 1) {
               maskUnions.add(maskList.get(0));
            } else if (maskList.size() != 0) {
               maskUnions.add(new MaskUnion(maskList));
            }
         }

         if (maskUnions.size() == 1) {
            return maskUnions.get(0);
         } else {
            return maskUnions.size() != 0 ? new MaskIntersection(maskUnions) : null;
         }
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return Collections.emptyList();
   }
}
