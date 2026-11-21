package net.kyori.adventure.text;

import java.util.Deque;
import java.util.Set;

public sealed interface ComponentIteratorType permits ComponentIteratorTypeImpl.BreadthFirst, ComponentIteratorTypeImpl.DepthFirst {
   ComponentIteratorType DEPTH_FIRST = ComponentIteratorTypeImpl.DepthFirst.INSTANCE;
   ComponentIteratorType BREADTH_FIRST = ComponentIteratorTypeImpl.BreadthFirst.INSTANCE;

   void populate(final Component component, final Deque<Component> deque, final Set<ComponentIteratorFlag> flags);
}
