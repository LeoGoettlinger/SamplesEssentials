package com.sk89q.worldedit.event.platform;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.AbstractCancellable;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import javax.annotation.Nullable;

public class CommandEvent extends AbstractCancellable {
   private final Actor actor;
   private final String arguments;
   @Nullable
   private final EditSession session;

   public CommandEvent(Actor actor, String arguments) {
      Preconditions.checkNotNull(actor);
      Preconditions.checkNotNull(arguments);
      this.actor = actor;
      this.arguments = arguments;
      this.session = null;
   }

   public CommandEvent(Actor actor, String arguments, @Nullable EditSession editSession) {
      Preconditions.checkNotNull(actor);
      Preconditions.checkNotNull(arguments);
      this.actor = actor;
      this.arguments = arguments;
      this.session = editSession;
   }

   @Nullable
   public EditSession getSession() {
      return this.session;
   }

   public Actor getActor() {
      return this.actor;
   }

   public String getArguments() {
      return this.arguments;
   }

   @Override
   public boolean call() {
      PlatformCommandManager.getInstance().handleCommandOnCurrentThread(this);
      return true;
   }
}
