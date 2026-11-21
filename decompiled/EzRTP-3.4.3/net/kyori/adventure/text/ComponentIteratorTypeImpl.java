package net.kyori.adventure.text;

import java.util.Deque;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.event.HoverEvent;

final class ComponentIteratorTypeImpl {
   private ComponentIteratorTypeImpl() {
   }

   static final class BreadthFirst implements ComponentIteratorType {
      static final ComponentIteratorType INSTANCE = new ComponentIteratorTypeImpl.BreadthFirst();

      @Override
      public void populate(final Component component, final Deque<Component> deque, final Set<ComponentIteratorFlag> flags) {
         if (flags.contains(ComponentIteratorFlag.INCLUDE_TRANSLATABLE_COMPONENT_ARGUMENTS) && component instanceof TranslatableComponent tc) {
            for (TranslationArgument argument : tc.arguments()) {
               deque.add(argument.asComponent());
            }
         }

         HoverEvent<?> hoverEvent = component.hoverEvent();
         if (hoverEvent != null) {
            HoverEvent.Action<?> action = hoverEvent.action();
            if (flags.contains(ComponentIteratorFlag.INCLUDE_HOVER_SHOW_ENTITY_NAME) && action == HoverEvent.Action.SHOW_ENTITY) {
               Component name = ((HoverEvent.ShowEntity)hoverEvent.value()).name();
               if (name != null) {
                  deque.addLast(name);
               }
            } else if (flags.contains(ComponentIteratorFlag.INCLUDE_HOVER_SHOW_TEXT_COMPONENT) && action == HoverEvent.Action.SHOW_TEXT) {
               deque.addLast((Component)hoverEvent.value());
            }
         }

         deque.addAll(component.children());
      }
   }

   static final class DepthFirst implements ComponentIteratorType {
      static final ComponentIteratorType INSTANCE = new ComponentIteratorTypeImpl.DepthFirst();

      @Override
      public void populate(final Component component, final Deque<Component> deque, final Set<ComponentIteratorFlag> flags) {
         if (flags.contains(ComponentIteratorFlag.INCLUDE_TRANSLATABLE_COMPONENT_ARGUMENTS) && component instanceof TranslatableComponent translatable) {
            List<? extends ComponentLike> args = translatable.arguments();

            for (int i = args.size() - 1; i >= 0; i--) {
               deque.addFirst(args.get(i).asComponent());
            }
         }

         HoverEvent<?> hoverEvent = component.hoverEvent();
         if (hoverEvent != null) {
            HoverEvent.Action<?> action = hoverEvent.action();
            if (flags.contains(ComponentIteratorFlag.INCLUDE_HOVER_SHOW_ENTITY_NAME) && action == HoverEvent.Action.SHOW_ENTITY) {
               deque.addFirst(((HoverEvent.ShowEntity)hoverEvent.value()).name());
            } else if (flags.contains(ComponentIteratorFlag.INCLUDE_HOVER_SHOW_TEXT_COMPONENT) && action == HoverEvent.Action.SHOW_TEXT) {
               deque.addFirst((Component)hoverEvent.value());
            }
         }

         List<Component> children = component.children();

         for (int i = children.size() - 1; i >= 0; i--) {
            deque.addFirst(children.get(i));
         }
      }
   }
}
