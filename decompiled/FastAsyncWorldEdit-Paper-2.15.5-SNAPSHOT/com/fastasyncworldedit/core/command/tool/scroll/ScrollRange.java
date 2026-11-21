package com.fastasyncworldedit.core.command.tool.scroll;

import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.entity.Player;

public class ScrollRange extends Scroll {
   public ScrollRange(BrushTool tool) {
      super(tool);
   }

   @Override
   public boolean increment(Player player, int amount) {
      int max = player.getLimit().MAX_BRUSH_RADIUS;
      int newSize = MathMan.wrap(this.getTool().getRange() + amount, (int)(this.getTool().getSize() + 1.0), max);
      this.getTool().setRange(newSize);
      return true;
   }
}
