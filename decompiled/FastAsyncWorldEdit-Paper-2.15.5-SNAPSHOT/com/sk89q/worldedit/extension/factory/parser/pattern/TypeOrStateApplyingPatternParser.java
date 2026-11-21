package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extension.factory.parser.AliasedParser;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.buffer.ExtentBuffer;
import com.sk89q.worldedit.function.pattern.ExtentBufferedCompositePattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.StateApplyingPattern;
import com.sk89q.worldedit.function.pattern.TypeApplyingPattern;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class TypeOrStateApplyingPatternParser extends InputParser<Pattern> implements AliasedParser {
   public TypeOrStateApplyingPatternParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      if (input.isEmpty()) {
         return Stream.of("^");
      } else if (!input.startsWith("^")) {
         return Stream.empty();
      } else {
         input = input.substring(1);
         String[] parts = input.split("\\[", 2);
         String type = parts[0];
         if (parts.length == 1) {
            return this.worldEdit.getBlockFactory().getSuggestions(input, context).stream().map(s -> "^" + s);
         } else if (type.isEmpty()) {
            return Stream.empty();
         } else {
            BlockType blockType = BlockTypes.get(type.toLowerCase(Locale.ROOT));
            return SuggestionHelper.getBlockPropertySuggestions(type, blockType, parts[1]).map(s -> "^" + s);
         }
      }
   }

   public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
      if (!input.startsWith("^")) {
         return null;
      } else {
         Extent extent = context.requireExtent();
         input = input.substring(1);
         String[] parts = input.split("\\[", 2);
         String type = parts[0];
         if (parts.length == 1) {
            return new TypeApplyingPattern(extent, this.worldEdit.getBlockFactory().parseFromInput(type, context).getBlockType().getDefaultState());
         } else if (!parts[1].endsWith("]")) {
            throw new InputParseException(Caption.of("worldedit.error.parser.missing-rbracket"));
         } else {
            String[] states = parts[1].substring(0, parts[1].length() - 1).split(",");
            Map<String, String> statesToSet = new HashMap<>();

            for (String state : states) {
               if (state.isEmpty()) {
                  throw new InputParseException(Caption.of("worldedit.error.parser.empty-state"));
               }

               String[] propVal = state.split("=", 2);
               if (propVal.length != 2) {
                  throw new InputParseException(Caption.of("worldedit.error.parser.missing-equals-separator"));
               }

               String prop = propVal[0];
               if (prop.isEmpty()) {
                  throw new InputParseException(Caption.of("worldedit.error.parser.empty-property"));
               }

               String value = propVal[1];
               if (value.isEmpty()) {
                  throw new InputParseException(Caption.of("worldedit.error.parser.empty-value"));
               }

               if (statesToSet.put(prop, value) != null) {
                  throw new InputParseException(Caption.of("worldedit.error.parser.duplicate-property", TextComponent.of(prop)));
               }
            }

            if (type.isEmpty()) {
               return new StateApplyingPattern(extent, statesToSet);
            } else {
               Extent buffer = new ExtentBuffer(extent);
               Pattern typeApplier = new TypeApplyingPattern(
                  buffer, this.worldEdit.getBlockFactory().parseFromInput(type, context).getBlockType().getDefaultState()
               );
               Pattern stateApplier = new StateApplyingPattern(buffer, statesToSet);
               return new ExtentBufferedCompositePattern(buffer, typeApplier, stateApplier);
            }
         }
      }
   }

   @Override
   public List<String> getMatchedAliases() {
      return Collections.singletonList("^");
   }
}
