package com.sk89q.worldedit.util.task;

import java.util.Comparator;

public class TaskStateComparator implements Comparator<Task<?>> {
   public int compare(Task<?> o1, Task<?> o2) {
      int ordinal1 = o1.getState().ordinal();
      int ordinal2 = o2.getState().ordinal();
      return Integer.compare(ordinal1, ordinal2);
   }
}
