package com.sk89q.worldedit.extension.factory.parser.mask;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.ExpressionMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

public class ExpressionMaskParser extends InputParser<Mask> implements AliasedParser {
   private final List<String> aliases = ImmutableList.of("=");

   public ExpressionMaskParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      return input.isEmpty() ? Stream.of("=") : Stream.empty();
   }

   public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
      if (!input.startsWith("=")) {
         return null;
      } else {
         if (input.charAt(1) == '[') {
            int end = input.lastIndexOf(93);
            if (end == -1) {
               return null;
            }

            input = input.substring(2, end);
         } else {
            input = input.substring(1);
         }

         try {
            Expression exp = Expression.compile(input, "x", "y", "z");
            WorldEditExpressionEnvironment env = new WorldEditExpressionEnvironment(context.requireExtent(), Vector3.ONE, Vector3.ZERO);
            exp.setEnvironment(env);
            if (context.getActor() != null) {
               SessionOwner owner = context.getActor();
               IntSupplier timeout = () -> WorldEdit.getInstance().getSessionManager().get(owner).getTimeout();
               return new ExpressionMask(exp, timeout);
            } else {
               return new ExpressionMask(exp);
            }
         } catch (ExpressionException var7) {
            throw new InputParseException(Caption.of("worldedit.error.parser.invalid-expression", TextComponent.of(var7.getMessage())));
         }
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return this.aliases;
   }
}
