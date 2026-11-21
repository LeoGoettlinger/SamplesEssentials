package com.sk89q.worldedit.util.task;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;

public class SimpleSupervisor implements Supervisor {
   private final List<Task<?>> monitored = new ArrayList<>();
   private final Object lock = new Object();

   @Override
   public List<Task<?>> getTasks() {
      synchronized (this.lock) {
         return new ArrayList<>(this.monitored);
      }
   }

   @Override
   public void monitor(final Task<?> task) {
      Preconditions.checkNotNull(task);
      synchronized (this.lock) {
         this.monitored.add(task);
      }

      task.addListener(() -> {
         synchronized (this.lock) {
            this.monitored.remove(task);
         }
      }, MoreExecutors.directExecutor());
   }
}
