package com.sk89q.worldedit.extension.factory;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.parser.DefaultBlockParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.AbstractFactory;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.HashSet;
import java.util.Set;

public class BlockFactory extends AbstractFactory<BaseBlock> {
   public BlockFactory(WorldEdit worldEdit) {
      super(worldEdit, new DefaultBlockParser(worldEdit));
   }

   public Set<BaseBlock> parseFromListInput(String input, ParserContext context) throws InputParseException {
      Set<BaseBlock> blocks = new HashSet<>();

      for (String token : StringUtil.split(input, ',', '[', ']')) {
         blocks.add(this.parseFromInput(token, context));
      }

      return blocks;
   }

   public BaseBlock parseFromInput(String input, ParserContext context) throws InputParseException {
      for (InputParser<BaseBlock> parser : this.parsers) {
         BaseBlock match = parser.parseFromInput(input, context);
         if (match != null) {
            return match;
         }
      }

      throw new NoMatchException(TranslatableComponent.of("worldedit.error.no-match", TextComponent.of(input)));
   }
}
