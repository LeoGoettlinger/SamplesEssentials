package ch.njol.skript.log;

import java.util.Iterator;
import java.util.LinkedList;
import org.jetbrains.annotations.Nullable;

public class HandlerList implements Iterable<LogHandler> {
   private final LinkedList<LogHandler> list = new LinkedList<>();

   public void add(LogHandler h) {
      this.list.addFirst(h);
   }

   @Nullable
   public LogHandler remove() {
      return this.list.pop();
   }

   @Override
   public Iterator<LogHandler> iterator() {
      return this.list.iterator();
   }

   public boolean contains(LogHandler h) {
      return this.list.contains(h);
   }
}
