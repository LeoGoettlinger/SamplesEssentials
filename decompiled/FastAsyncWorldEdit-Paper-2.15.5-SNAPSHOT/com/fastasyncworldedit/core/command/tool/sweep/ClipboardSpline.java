package com.fastasyncworldedit.core.command.tool.sweep;

import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.fastasyncworldedit.core.math.transform.RoundedTransform;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.interpolation.Interpolation;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

public class ClipboardSpline extends Spline {
   private final Transform transform;
   private final ClipboardHolder clipboardHolder;
   private final BlockVector3 originalOrigin;
   private final Transform originalTransform;
   private BlockVector3 center;
   private final BlockVector3 centerOffset;
   private final BlockVector3Set buffer;

   public ClipboardSpline(EditSession editSession, ClipboardHolder clipboardHolder, Interpolation interpolation) {
      this(editSession, clipboardHolder, interpolation, new AffineTransform(), -1);
   }

   public ClipboardSpline(EditSession editSession, ClipboardHolder clipboardHolder, Interpolation interpolation, Transform transform, int nodeCount) {
      super(editSession, interpolation, nodeCount);
      this.clipboardHolder = clipboardHolder;
      this.originalTransform = clipboardHolder.getTransform();
      Clipboard clipboard = clipboardHolder.getClipboard();
      this.originalOrigin = clipboard.getOrigin();
      Region region = clipboard.getRegion();
      BlockVector3 origin = clipboard.getOrigin();
      this.center = region.getCenter().withY(origin.y() - 1).toBlockPoint();
      this.centerOffset = this.center.subtract(this.center.round());
      this.center = this.center.subtract(this.centerOffset);
      this.transform = transform;
      this.buffer = LocalBlockVectorSet.wrapped();
   }

   @Override
   protected int pasteBlocks(BlockVector3 target, Vector3 offset, double angle) throws MaxChangedBlocksException {
      RoundedTransform transform = new RoundedTransform(new AffineTransform().translate(offset).rotateY(angle));
      if (!this.transform.isIdentity()) {
         transform = transform.combine(this.transform);
      }

      if (!this.originalTransform.isIdentity()) {
         transform = transform.combine(this.originalTransform);
      }

      Clipboard clipboard = this.clipboardHolder.getClipboard();
      clipboard.setOrigin(this.center.subtract(this.centerOffset).round());
      this.clipboardHolder.setTransform(transform);
      BlockVector3 functionOffset = target.subtract(clipboard.getOrigin());
      int offX = functionOffset.x();
      int offY = functionOffset.y();
      int offZ = functionOffset.z();
      Operation operation = this.clipboardHolder
         .createPaste(this.editSession)
         .to(target)
         .ignoreAirBlocks(true)
         .filter(v -> this.buffer.add(v.x() + offX, v.y() + offY, v.z() + offZ))
         .build();
      Operations.completeLegacy(operation);
      this.clipboardHolder.setTransform(this.originalTransform);
      clipboard.setOrigin(this.originalOrigin);
      return operation instanceof ForwardExtentCopy ? ((ForwardExtentCopy)operation).getAffected() : 0;
   }
}
