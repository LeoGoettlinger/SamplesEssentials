package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.fastasyncworldedit.core.util.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

public class ResettableExtent extends AbstractDelegateExtent implements Serializable {
   public ResettableExtent(Extent parent) {
      super(parent);
   }

   public final void init(BlockVector3 pos) {
      Extent extent = this.getExtent();
      if (extent instanceof ResettableExtent && extent != this) {
         ((ResettableExtent)extent).init(pos);
      }

      this.setOrigin(pos);
   }

   protected void setOrigin(BlockVector3 pos) {
   }

   public ResettableExtent setExtent(Extent extent) {
      Preconditions.checkNotNull(extent);
      Extent next = this.getExtent();
      if (!(next instanceof NullExtent) && !(next instanceof World) && next instanceof ResettableExtent) {
         ((ResettableExtent)next).setExtent(extent);
      } else {
         new ExtentTraverser<>(this).setNext(extent);
      }

      return this;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      Extent extent = this.getExtent();
      boolean next = extent instanceof ResettableExtent;
      stream.writeBoolean(next);
      if (next) {
         stream.writeObject(extent);
      }
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      if (stream.readBoolean()) {
         try {
            Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
            ReflectionUtils.setFailsafeFieldValue(field, this, stream.readObject());
         } catch (Throwable var3) {
            var3.printStackTrace();
         }
      }
   }
}
