package com.sk89q.worldedit;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.item.ItemType;

public class NotABlockException extends WorldEditException {
   public NotABlockException() {
      super(Caption.of("worldedit.error.not-a-block"));
   }

   @Deprecated
   public NotABlockException(String input) {
      super(Caption.of("worldedit.error.not-a-block.item", TextComponent.of(input)));
   }

   @Deprecated
   public NotABlockException(int input) {
      super(Caption.of("worldedit.error.not-a-block.item", TextComponent.of(input)));
   }

   public NotABlockException(ItemType input) {
      super(Caption.of("worldedit.error.not-a-block.item", input.getRichName()));
   }
}
