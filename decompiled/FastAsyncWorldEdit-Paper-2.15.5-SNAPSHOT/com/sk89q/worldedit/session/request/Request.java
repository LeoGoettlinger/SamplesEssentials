package com.sk89q.worldedit.session.request;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.World;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

public final class Request {
   private static final Map<Thread, Request> requests = new ConcurrentHashMap<>();
   @Nullable
   private World world;
   @Nullable
   private LocalSession session;
   @Nullable
   private EditSession editSession;
   private boolean valid;
   @Nullable
   private Actor actor;
   @Nullable
   private Extent extent;

   private Request() {
   }

   public static Collection<Request> getAll() {
      return requests.values();
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public void setWorld(@Nullable World world) {
      this.world = world;
   }

   public void setExtent(@Nullable Extent extent) {
      this.extent = extent;
   }

   @Nullable
   public Extent getExtent() {
      if (this.extent != null) {
         return this.extent;
      } else if (this.editSession != null) {
         return this.editSession;
      } else {
         return this.world != null ? this.world : null;
      }
   }

   @Nullable
   public Actor getActor() {
      return this.actor;
   }

   public void setActor(@Nullable Actor actor) {
      this.actor = actor;
   }

   @Nullable
   public LocalSession getSession() {
      return this.session;
   }

   public void setSession(@Nullable LocalSession session) {
      this.session = session;
   }

   @Nullable
   public EditSession getEditSession() {
      return this.editSession;
   }

   public void setEditSession(@Nullable EditSession editSession) {
      this.editSession = editSession;
   }

   public static Request request() {
      return requests.computeIfAbsent(Thread.currentThread(), t -> new Request());
   }

   public static void reset() {
      Request request = requests.remove(Thread.currentThread());
      if (request != null) {
         request.invalidate();
      }
   }

   public boolean isValid() {
      return this.valid;
   }

   private void invalidate() {
      this.valid = false;
   }
}
