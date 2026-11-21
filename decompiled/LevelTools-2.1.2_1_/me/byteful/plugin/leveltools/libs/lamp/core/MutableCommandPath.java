package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.Iterator;
import me.byteful.plugin.leveltools.libs.lamp.command.ArgumentStack;
import org.jetbrains.annotations.NotNull;

public class MutableCommandPath extends CommandPath {
   @NotNull
   public static MutableCommandPath empty() {
      return new MutableCommandPath(new String[0]);
   }

   public MutableCommandPath(String[] path) {
      super(path);
   }

   public MutableCommandPath(ArgumentStack argumentStack) {
      super(argumentStack.toArray(new String[0]));
   }

   public String removeFirst() {
      return this.path.removeFirst();
   }

   public String removeLast() {
      return this.path.removeLast();
   }

   public void addFirst(String s) {
      this.path.addFirst(s.toLowerCase());
   }

   public void addLast(String s) {
      this.path.addLast(s.toLowerCase());
   }

   public boolean contains(Object o) {
      return o instanceof String ? this.path.contains(((String)o).toLowerCase()) : this.path.contains(o);
   }

   public boolean add(String s) {
      return this.path.add(s.toLowerCase());
   }

   public void clear() {
      this.path.clear();
   }

   public void add(int index, String element) {
      this.path.add(index, element.toLowerCase());
   }

   public String peek() {
      return this.path.peek();
   }

   public String poll() {
      return this.path.poll();
   }

   public void push(String s) {
      this.path.push(s);
   }

   public String pop() {
      return this.path.pop();
   }

   public CommandPath toImmutablePath() {
      return new CommandPath(this.path.toArray(new String[0]));
   }

   @NotNull
   @Override
   public Iterator<String> iterator() {
      return this.path.iterator();
   }

   @Override
   public boolean isMutable() {
      return true;
   }
}
