package com.fastasyncworldedit.core.function.pattern;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import javax.annotation.Nullable;

public class BufferedPattern2D extends BufferedPattern {
   public BufferedPattern2D(Actor actor, Pattern parent) {
      super(actor, parent, null);
   }

   public BufferedPattern2D(Actor actor, Pattern parent, @Nullable Region region) {
      super(actor, parent, region);
   }

   @Override
   public boolean set(BlockVector3 pos) {
      return this.set.add(pos.x(), 0, pos.z());
   }
}
