package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extension.factory.parser.DefaultItemParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.AbstractFactory;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

public class ItemFactory extends AbstractFactory<BaseItem> {
   public ItemFactory(WorldEdit worldEdit) {
      super(worldEdit, new DefaultItemParser(worldEdit));
   }

   public BaseItem parseFromInput(String input, ParserContext context) throws InputParseException {
      for (InputParser<BaseItem> parser : this.parsers) {
         BaseItem match = parser.parseFromInput(input, context);
         if (match != null) {
            return match;
         }
      }

      throw new NoMatchException(TranslatableComponent.of("worldedit.error.no-match", TextComponent.of(input)));
   }
}
