package com.sk89q.worldedit.event;

import com.sk89q.worldedit.WorldEdit;

public abstract class Event {
   public boolean call() {
      WorldEdit.getInstance().getEventBus().post(this);
      return this instanceof Cancellable ? !((Cancellable)this).isCancelled() : true;
   }
}
