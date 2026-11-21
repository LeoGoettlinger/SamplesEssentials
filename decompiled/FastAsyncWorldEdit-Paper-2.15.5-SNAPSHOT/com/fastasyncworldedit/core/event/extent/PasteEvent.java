package com.fastasyncworldedit.core.event.extent;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.Cancellable;
import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import java.net.URI;

public class PasteEvent extends Event implements Cancellable {
   private final Actor actor;
   private final Clipboard clipboard;
   private final URI uri;
   private final BlockVector3 to;
   private final Extent extent;
   private boolean cancelled;

   public PasteEvent(Actor actor, Clipboard clipboard, URI uri, Extent extent, BlockVector3 to) {
      this.actor = actor;
      this.clipboard = clipboard;
      this.uri = uri;
      this.extent = extent;
      this.to = to;
   }

   public Actor getActor() {
      return this.actor;
   }

   public Clipboard getClipboard() {
      return this.clipboard;
   }

   public URI getURI() {
      return this.uri;
   }

   public BlockVector3 getPosition() {
      return this.to;
   }

   public Extent getExtent() {
      return this.extent;
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public PasteEvent clone(EditSession.Stage stage) {
      return new PasteEvent(this.actor, this.clipboard, this.uri, this.extent, this.to);
   }
}
