package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.util.Collections;
import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import com.artillexstudios.axtrade.libs.lamp.util.Strings;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandPath implements Iterable<String>, Comparable<CommandPath> {
   protected final LinkedList<String> path;

   @NotNull
   public static CommandPath parse(@NotNull String path) {
      Preconditions.notEmpty(path, "Path cannot be empty!");
      return get(Strings.splitBySpace(path));
   }

   @NotNull
   public static CommandPath get(@NotNull String... path) {
      Preconditions.notEmpty(path, "Path cannot be empty!");
      return new CommandPath((String[])path.clone());
   }

   @NotNull
   public static CommandPath get(@NotNull Collection<String> path) {
      Preconditions.notEmpty(path, "Path cannot be empty!");
      return new CommandPath(path.toArray(new String[0]));
   }

   CommandPath(String[] path) {
      for (int i = 0; i < path.length; i++) {
         String s = path[i];
         path[i] = s.toLowerCase();
      }

      this.path = Collections.linkedListOf(path);
   }

   @NotNull
   public String toRealString() {
      return String.join(" ", this.path);
   }

   @NotNull
   public LinkedList<String> toList() {
      return new LinkedList<>(this.path);
   }

   @NotNull
   public String getParent() {
      return this.path.getFirst();
   }

   @NotNull
   public String getName() {
      return this.path.getLast();
   }

   @NotNull
   public String getFirst() {
      return this.path.getFirst();
   }

   @NotNull
   public String getLast() {
      return this.path.getLast();
   }

   @NotNull
   public String get(int index) {
      return this.path.get(index);
   }

   public int size() {
      return this.path.size();
   }

   public boolean isRoot() {
      return this.path.size() == 1;
   }

   @Nullable
   public CommandPath getCategoryPath() {
      if (this.path.size() <= 1) {
         return null;
      } else {
         LinkedList<String> list = this.toList();
         list.removeLast();
         return get(list);
      }
   }

   @NotNull
   public LinkedList<String> getSubcommandPath() {
      return new LinkedList<>(this.path.subList(1, this.path.size()));
   }

   public MutableCommandPath toMutablePath() {
      return new MutableCommandPath(this.path.toArray(new String[0]));
   }

   public boolean isMutable() {
      return false;
   }

   public boolean isChildOf(CommandPath other) {
      if (this.path.size() < other.size()) {
         return false;
      } else {
         for (int i = 0; i < this.path.size(); i++) {
            if (other.path.size() <= i) {
               return true;
            }

            if (!other.path.get(i).equals(this.path.get(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isParentOf(CommandPath other) {
      return other.isChildOf(this);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof CommandPath)) {
         return false;
      } else {
         CommandPath that = (CommandPath)o;
         return Objects.equals(this.path, that.path);
      }
   }

   @Override
   public int hashCode() {
      return this.path.hashCode();
   }

   @Override
   public String toString() {
      return "CommandPath{" + this.toRealString() + '}';
   }

   @NotNull
   @Override
   public Iterator<String> iterator() {
      return new CommandPath.PathIterator<>(this.path.iterator());
   }

   public int compareTo(@NotNull CommandPath o) {
      if (this.isParentOf(o)) {
         return -1;
      } else if (this.isChildOf(o)) {
         return 1;
      } else {
         return o.equals(this) ? 0 : this.toRealString().compareTo(o.toRealString());
      }
   }

   private static class PathIterator<E> implements Iterator<E> {
      private final Iterator<? extends E> iterator;

      public PathIterator(Iterator<? extends E> iterator) {
         this.iterator = iterator;
      }

      @Override
      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      @Override
      public E next() {
         return (E)this.iterator.next();
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException("Iterator.remove() is disabled.");
      }
   }
}
