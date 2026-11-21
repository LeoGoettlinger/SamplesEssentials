package com.earth2me.essentials.libs.configurate;

import java.util.Collection;
import java.util.Iterator;

public interface NodePath extends Iterable<Object> {
   static NodePath of(Object[] path) {
      return (NodePath)(path.length == 0 ? NodePathImpl.EMPTY : new NodePathImpl(path, true));
   }

   static NodePath of(Collection<?> path) {
      return (NodePath)(path.isEmpty() ? NodePathImpl.EMPTY : new NodePathImpl(path.toArray(), false));
   }

   static NodePath path(Object... elements) {
      return of(elements);
   }

   static NodePath path() {
      return NodePathImpl.EMPTY;
   }

   Object get(int i);

   int size();

   Object[] array();

   NodePath withAppendedChild(Object childKey);

   NodePath with(int index, Object value) throws IndexOutOfBoundsException;

   NodePath plus(NodePath other);

   @Override
   Iterator<Object> iterator();

   NodePath copy();
}
