package com.sk89q.worldedit.util.task;

import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.util.task.progress.ProgressObservable;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;

public interface Task<V> extends ListenableFuture<V>, ProgressObservable {
   UUID getUniqueId();

   String getName();

   @Nullable
   Object getOwner();

   Task.State getState();

   Date getCreationDate();

   public static enum State {
      SCHEDULED,
      CANCELLED,
      RUNNING,
      FAILED,
      SUCCEEDED;
   }
}
