package com.fastasyncworldedit.core.queue;

public interface Trimable {
   boolean trim(boolean aggressive);

   default void recycle() {
   }
}
