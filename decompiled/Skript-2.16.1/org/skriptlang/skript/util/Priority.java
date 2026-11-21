package org.skriptlang.skript.util;

import java.util.Collection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

public interface Priority extends Comparable<Priority> {
   @Contract("-> new")
   static Priority base() {
      return new PriorityImpl();
   }

   @Contract("_ -> new")
   static Priority before(Priority priority) {
      return new PriorityImpl(priority, true);
   }

   @Contract("_ -> new")
   static Priority after(Priority priority) {
      return new PriorityImpl(priority, false);
   }

   @Unmodifiable
   Collection<Priority> after();

   @Unmodifiable
   Collection<Priority> before();
}
