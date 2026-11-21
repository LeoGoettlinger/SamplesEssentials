package com.fastasyncworldedit.core.command.tool.scroll;

import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Pattern;

public class ScrollPattern extends Scroll {
   private final Pattern[] patterns;
   private int index;

   public ScrollPattern(BrushTool tool, Pattern... patterns) {
      super(tool);
      this.patterns = patterns;
   }

   @Override
   public boolean increment(Player player, int amount) {
      if (this.patterns.length > 1) {
         this.getTool().setFill(this.patterns[MathMan.wrap(this.index += amount, 0, this.patterns.length - 1)]);
         return true;
      } else {
         return false;
      }
   }
}
