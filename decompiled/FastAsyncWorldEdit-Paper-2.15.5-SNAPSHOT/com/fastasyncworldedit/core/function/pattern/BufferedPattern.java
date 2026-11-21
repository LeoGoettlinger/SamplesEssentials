package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.fastasyncworldedit.core.util.FaweTimer;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import javax.annotation.Nullable;

public class BufferedPattern extends AbstractPattern implements ResettablePattern {
   protected final BlockVector3Set set;
   protected final FaweTimer timer;
   protected final long[] actionTime;
   protected final Pattern pattern;

   public BufferedPattern(Actor actor, Pattern parent) {
      this(actor, parent, null);
   }

   public BufferedPattern(Actor actor, Pattern parent, @Nullable Region region) {
      long[] tmp = actor.getMeta("lastActionTime");
      if (tmp == null) {
         actor.setMeta("lastActionTime", tmp = new long[2]);
      }

      this.actionTime = tmp;
      this.pattern = parent;
      this.timer = Fawe.instance().getTimer();
      this.set = region == null ? LocalBlockVectorSet.wrapped() : BlockVector3Set.getAppropriateVectorSet(region);
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      return this.pattern.applyBlock(position);
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      this.actionTime[1] = this.timer.getTick();
      return !this.set(get) ? false : this.pattern.apply(extent, get, set);
   }

   public boolean set(BlockVector3 pos) {
      return this.set.add(pos);
   }

   @Override
   public void reset() {
      long now = this.timer.getTick();
      if (now - this.actionTime[1] > 5L) {
         this.set.clear();
      }

      this.actionTime[1] = this.actionTime[0];
      this.actionTime[0] = now;
   }
}
