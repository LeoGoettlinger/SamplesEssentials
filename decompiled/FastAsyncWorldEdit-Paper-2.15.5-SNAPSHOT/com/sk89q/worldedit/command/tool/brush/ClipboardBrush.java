package com.sk89q.worldedit.command.tool.brush;

import com.fastasyncworldedit.core.math.transform.MutatingOperationTransformHolder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.util.concurrent.ThreadLocalRandom;

public class ClipboardBrush implements Brush {
   private final ClipboardHolder holder;
   private final boolean ignoreAirBlocks;
   private final boolean usingOrigin;
   private final boolean pasteEntities;
   private final boolean pasteBiomes;
   private final Mask sourceMask;
   private final boolean randomRotate;

   public ClipboardBrush(ClipboardHolder holder, boolean ignoreAirBlocks, boolean usingOrigin) {
      this.holder = holder;
      this.ignoreAirBlocks = ignoreAirBlocks;
      this.usingOrigin = usingOrigin;
      this.pasteBiomes = false;
      this.pasteEntities = false;
      this.sourceMask = null;
      this.randomRotate = false;
   }

   public ClipboardBrush(ClipboardHolder holder, boolean ignoreAirBlocks, boolean usingOrigin, boolean pasteEntities, boolean pasteBiomes, Mask sourceMask) {
      this(holder, ignoreAirBlocks, usingOrigin, pasteEntities, pasteBiomes, sourceMask, false);
   }

   public ClipboardBrush(
      ClipboardHolder holder, boolean ignoreAirBlocks, boolean usingOrigin, boolean pasteEntities, boolean pasteBiomes, Mask sourceMask, boolean randomRotate
   ) {
      this.holder = holder;
      this.ignoreAirBlocks = ignoreAirBlocks;
      this.usingOrigin = usingOrigin;
      this.pasteEntities = pasteEntities;
      this.pasteBiomes = pasteBiomes;
      this.sourceMask = sourceMask;
      this.randomRotate = randomRotate;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
      Transform originalTransform = this.holder.getTransform();
      Transform transform = new AffineTransform();
      if (this.randomRotate) {
         int rotate = 90 * ThreadLocalRandom.current().nextInt(4);
         transform = ((AffineTransform)transform).rotateY(rotate);
         if (originalTransform != null) {
            transform = transform.combine(MutatingOperationTransformHolder.transform(originalTransform, true));
         }
      }

      this.holder.setTransform(transform);
      Clipboard clipboard = this.holder.getClipboard();
      Region region = clipboard.getRegion();
      BlockVector3 centerOffset = region.getCenter().toBlockPoint().subtract(clipboard.getOrigin());
      Operation operation = this.holder
         .createPaste(editSession)
         .to(this.usingOrigin ? position : position.subtract(centerOffset))
         .ignoreAirBlocks(this.ignoreAirBlocks)
         .copyEntities(this.pasteEntities)
         .copyBiomes(this.pasteBiomes)
         .maskSource(this.sourceMask)
         .build();
      Operations.completeLegacy(operation);
      this.holder.setTransform(originalTransform);
   }
}
