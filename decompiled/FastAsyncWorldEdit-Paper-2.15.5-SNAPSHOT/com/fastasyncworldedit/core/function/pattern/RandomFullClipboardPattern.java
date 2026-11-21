package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.math.transform.MutatingOperationTransformHolder;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomFullClipboardPattern extends AbstractPattern {
   private final List<ClipboardHolder> clipboards;
   private final boolean randomRotate;
   private final boolean randomFlip;
   private final Vector3 flipVector = Vector3.at(1.0, 0.0, 0.0).multiply(-2.0).add(1.0, 1.0, 1.0);
   private final BlockVector3 size;

   public RandomFullClipboardPattern(List<ClipboardHolder> clipboards, boolean randomRotate, boolean randomFlip) {
      Preconditions.checkNotNull(clipboards);
      this.clipboards = clipboards;
      MutableBlockVector3 mut = new MutableBlockVector3();
      clipboards.stream().flatMap(c -> c.getClipboards().stream()).map(c -> {
         Region region = c.getRegion();
         return region.getMaximumPoint().subtract(c.getOrigin().getMinimum(region.getMinimumPoint()));
      }).forEach(mut::getMaximum);
      this.size = mut.toImmutable();
      this.randomRotate = randomRotate;
      this.randomFlip = randomFlip;
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      ClipboardHolder holder = this.clipboards.get(ThreadLocalRandom.current().nextInt(this.clipboards.size()));
      AffineTransform transform = new AffineTransform();
      if (this.randomRotate) {
         transform = transform.rotateY(ThreadLocalRandom.current().nextInt(4) * 90);
      }

      if (this.randomFlip && ThreadLocalRandom.current().nextBoolean()) {
         transform = transform.scale(this.flipVector);
      }

      if (!transform.isIdentity()) {
         holder.setTransform(transform.combine(holder.getTransform()));
      }

      Clipboard clipboard = holder.getClipboard();
      Transform newTransform = holder.getTransform();
      if (newTransform.isIdentity()) {
         clipboard.paste(extent, set, false, true, false);
      } else {
         newTransform = MutatingOperationTransformHolder.transform(newTransform, true);
         clipboard.paste(extent, set, false, newTransform);
      }

      return true;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      throw new IllegalStateException("Incorrect use. This pattern can only be applied to an extent!");
   }

   @Override
   public BlockVector3 size() {
      return this.size;
   }
}
