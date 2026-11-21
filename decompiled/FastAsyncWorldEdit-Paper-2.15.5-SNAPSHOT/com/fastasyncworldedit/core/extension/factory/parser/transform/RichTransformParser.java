package com.fastasyncworldedit.core.extension.factory.parser.transform;

import com.fastasyncworldedit.core.command.SuggestInputParseException;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.FaweParser;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.extent.transform.MultiTransform;
import com.fastasyncworldedit.core.extent.transform.RandomTransform;
import com.fastasyncworldedit.core.math.random.TrueRandom;
import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class RichTransformParser extends FaweParser<ResettableExtent> {
   public RichTransformParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   public ResettableExtent parseFromInput(String input, ParserContext context) throws InputParseException {
      if (input.isEmpty()) {
         return null;
      } else {
         List<Double> unionChances = new ArrayList<>();
         List<Double> intersectionChances = new ArrayList<>();
         List<ResettableExtent> intersection = new ArrayList<>();
         List<ResettableExtent> union = new ArrayList<>();
         CommandLocals locals = new CommandLocals();
         Actor actor = context != null ? context.getActor() : null;
         if (actor != null) {
            locals.put(Actor.class, actor);
         }

         try {
            for (Entry<FaweParser.ParseEntry, List<String>> entry : parse(input)) {
               FaweParser.ParseEntry pe = entry.getKey();
               String command = pe.getInput();
               double chance = 1.0;
               ResettableExtent transform;
               if (command.isEmpty()) {
                  transform = this.parseFromInput(StringMan.join(entry.getValue(), ','), context);
               } else if (!this.worldEdit.getTransformFactory().containsAlias(command)) {
                  int percentIndex = command.indexOf(37);
                  if (percentIndex == -1) {
                     throw new NoMatchException(
                        Caption.of(
                           "fawe.error.parse.unknown-transform",
                           pe.getFull(),
                           TextComponent.of("https://intellectualsites.github.io/fastasyncworldedit-documentation/transforms/transforms")
                              .clickEvent(ClickEvent.openUrl("https://intellectualsites.github.io/fastasyncworldedit-documentation/transforms/transforms"))
                        )
                     );
                  }

                  chance = Expression.compile(command.substring(0, percentIndex)).evaluate();
                  command = command.substring(percentIndex + 1);
                  if (!entry.getValue().isEmpty()) {
                     if (!command.isEmpty()) {
                        command = command + " ";
                     }

                     command = command + StringMan.join(entry.getValue(), " ");
                  }

                  transform = this.parseFromInput(command, context);
               } else {
                  try {
                     transform = this.worldEdit.getTransformFactory().parseWithoutRich(pe.getFull(), context);
                  } catch (SuggestInputParseException var22) {
                     throw var22;
                  } catch (Throwable var23) {
                     throw new NoMatchException(
                        Caption.of(
                           "fawe.error.parse.unknown-transform",
                           pe.getFull(),
                           TextComponent.of("https://intellectualsites.github.io/fastasyncworldedit-documentation/transforms/transforms")
                              .clickEvent(ClickEvent.openUrl("https://intellectualsites.github.io/fastasyncworldedit-documentation/transforms/transforms"))
                        )
                     );
                  }
               }

               if (pe.isAnd()) {
                  intersectionChances.add(chance);
                  intersection.add(transform);
               } else {
                  if (!intersection.isEmpty()) {
                     if (intersection.size() == 1) {
                        throw new InputParseException(Caption.of("fawe.error.parse.invalid-dangling-character", "&"));
                     }

                     MultiTransform multi = new MultiTransform();
                     double total = 0.0;

                     for (int i = 0; i < intersection.size(); i++) {
                        Double value = intersectionChances.get(i);
                        total += value;
                        multi.add(intersection.get(i), value);
                     }

                     union.add(multi);
                     unionChances.add(total);
                     intersection.clear();
                     intersectionChances.clear();
                  }

                  unionChances.add(chance);
                  union.add(transform);
               }
            }
         } catch (Throwable var24) {
            throw new InputParseException(TextComponent.of(var24.getMessage()), var24);
         }

         if (!intersection.isEmpty()) {
            if (intersection.size() == 1) {
               throw new InputParseException(Caption.of("fawe.error.parse.invalid-dangling-character", "&"));
            }

            MultiTransform multi = new MultiTransform();
            double total = 0.0;

            for (int i = 0; i < intersection.size(); i++) {
               Double value = intersectionChances.get(i);
               total += value;
               multi.add(intersection.get(i), value);
            }

            union.add(multi);
            unionChances.add(total);
            intersection.clear();
            intersectionChances.clear();
         }

         if (union.isEmpty()) {
            throw new NoMatchException(
               Caption.of(
                  "fawe.error.parse.unknown-transform",
                  input,
                  TextComponent.of("https://intellectualsites.github.io/fastasyncworldedit-documentation/transforms/transforms")
                     .clickEvent(ClickEvent.openUrl("https://intellectualsites.github.io/fastasyncworldedit-documentation/transforms/transforms"))
               )
            );
         } else if (union.size() == 1) {
            return union.get(0);
         } else {
            RandomTransform random = new RandomTransform(new TrueRandom());

            for (int i = 0; i < union.size(); i++) {
               random.add(union.get(i), unionChances.get(i));
            }

            return random;
         }
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return Collections.emptyList();
   }
}
