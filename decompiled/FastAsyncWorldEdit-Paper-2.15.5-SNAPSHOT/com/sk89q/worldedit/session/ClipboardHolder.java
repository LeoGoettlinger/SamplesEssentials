package com.sk89q.worldedit.session;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.transform.Identity;
import com.sk89q.worldedit.math.transform.Transform;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ClipboardHolder implements Closeable, Flushable {
   private Clipboard clipboard;
   private Transform transform = new Identity();

   public ClipboardHolder(Clipboard clipboard) {
      Preconditions.checkNotNull(clipboard);
      this.clipboard = clipboard;
   }

   @Deprecated
   public Clipboard getClipboard() {
      return this.clipboard;
   }

   public List<Clipboard> getClipboards() {
      return Collections.singletonList(this.getClipboard());
   }

   public boolean contains(Clipboard clipboard) {
      return this.clipboard == clipboard;
   }

   public List<ClipboardHolder> getHolders() {
      return Collections.singletonList(this);
   }

   public void setTransform(Transform transform) {
      Preconditions.checkNotNull(transform);
      this.transform = transform;
   }

   public Transform getTransform() {
      return this.transform;
   }

   public PasteBuilder createPaste(Extent targetExtent) {
      return new PasteBuilder(this, targetExtent);
   }

   @Override
   public void close() {
      if (this.clipboard != null) {
         this.clipboard.close();
      }

      this.clipboard = null;
   }

   @Override
   public void flush() throws IOException {
      if (this.clipboard != null) {
         this.clipboard.flush();
      }
   }
}
