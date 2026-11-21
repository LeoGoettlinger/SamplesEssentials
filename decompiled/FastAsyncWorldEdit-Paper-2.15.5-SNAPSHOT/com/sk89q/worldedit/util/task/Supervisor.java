package com.sk89q.worldedit.util.task;

import java.util.List;

public interface Supervisor {
   List<Task<?>> getTasks();

   void monitor(Task<?> task);
}
