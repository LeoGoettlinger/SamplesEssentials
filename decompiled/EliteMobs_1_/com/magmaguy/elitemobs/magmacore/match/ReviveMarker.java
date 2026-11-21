package com.magmaguy.elitemobs.magmacore.match;

import org.bukkit.block.Block;

public interface ReviveMarker {
   ReviveMarker NONE = new ReviveMarker() {
      @Override
      public void show(Block banner, MatchPlayer dead, int livesLeft) {
      }

      @Override
      public void hide() {
      }
   };

   void show(Block var1, MatchPlayer var2, int var3);

   void hide();
}
