package de.theredend2000.advancedhunt.lib.kyori.adventure.text.flattener;

import de.theredend2000.advancedhunt.lib.kyori.adventure.internal.properties.AdventureProperties;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.KeybindComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ObjectComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.SelectorComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TextComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TranslatableComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.object.ObjectContents;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.object.PlayerHeadObjectContents;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.object.SpriteObjectContents;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.InheritanceAwareMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

record ComponentFlattenerImpl(
   InheritanceAwareMap<Component, ComponentFlattenerImpl.Handler> flatteners, @Nullable Function<Component, String> unknownHandler, int maxNestedDepth
) implements ComponentFlattener {
   static final ComponentFlattener BASIC = new ComponentFlattenerImpl.BuilderImpl()
      .mapper(KeybindComponent.class, component -> component.keybind())
      .mapper(SelectorComponent.class, SelectorComponent::pattern)
      .mapper(TextComponent.class, TextComponent::content)
      .mapper(TranslatableComponent.class, component -> {
         String fallback = component.fallback();
         return fallback != null ? fallback : component.key();
      })
      .mapper(
         ObjectComponent.class,
         component -> {
            ObjectContents contents = component.contents();
            if (contents instanceof SpriteObjectContents spriteContents) {
               Key atlas = spriteContents.atlas();
               return String.format(
                  "[%s%s]", spriteContents.sprite().asMinimalString(), !atlas.equals(SpriteObjectContents.DEFAULT_ATLAS) ? "@" + atlas.asMinimalString() : ""
               );
            } else {
               return contents instanceof PlayerHeadObjectContents playerHeadContents
                  ? String.format("[%s head]", playerHeadContents.name() != null ? playerHeadContents.name() : "unknown player")
                  : "";
            }
         }
      )
      .build();
   static final ComponentFlattener TEXT_ONLY = new ComponentFlattenerImpl.BuilderImpl().mapper(TextComponent.class, TextComponent::content).build();
   private static final int MAX_DEPTH = 512;

   @Override
   public void flatten(final Component input, final FlattenerListener listener) {
      this.flatten0(input, listener, 0, 0);
   }

   @Override
   public ComponentFlattener.Builder toBuilder() {
      return new ComponentFlattenerImpl.BuilderImpl(this.flatteners, this.unknownHandler, this.maxNestedDepth);
   }

   private void flatten0(final Component input, final FlattenerListener listener, final int depth, final int nestedDepth) {
      Objects.requireNonNull(input, "input");
      Objects.requireNonNull(listener, "listener");
      if (input != Component.empty()) {
         if (this.maxNestedDepth != -1 && nestedDepth > this.maxNestedDepth) {
            throw new IllegalStateException("Exceeded maximum nesting depth of " + this.maxNestedDepth + " while attempting to flatten components!");
         } else {
            Deque<ComponentFlattenerImpl.StackEntry> componentStack = new ArrayDeque<>();
            Deque<Style> styleStack = new ArrayDeque<>();
            componentStack.push(new ComponentFlattenerImpl.StackEntry(input, depth, 1));

            while (!componentStack.isEmpty()) {
               ComponentFlattenerImpl.StackEntry entry = componentStack.pop();
               int currentDepth = entry.depth;
               if (currentDepth > 512) {
                  throw new IllegalStateException("Exceeded maximum depth of 512 while attempting to flatten components!");
               }

               Component component = entry.component;
               ComponentFlattenerImpl.Handler flattener = this.flattener(component);
               Style componentStyle = component.style();
               listener.pushStyle(componentStyle);
               styleStack.push(componentStyle);
               if (flattener != null) {
                  flattener.handle(this, component, listener, currentDepth, nestedDepth);
               }

               if (!component.children().isEmpty() && listener.shouldContinue()) {
                  List<Component> children = component.children();

                  for (int i = children.size() - 1; i >= 0; i--) {
                     if (i == children.size() - 1) {
                        componentStack.push(new ComponentFlattenerImpl.StackEntry(children.get(i), currentDepth + 1, entry.stylesToPop + 1));
                     } else {
                        componentStack.push(new ComponentFlattenerImpl.StackEntry(children.get(i), currentDepth + 1, 1));
                     }
                  }
               } else {
                  for (int ix = entry.stylesToPop; ix > 0; ix--) {
                     Style style = styleStack.pop();
                     listener.popStyle(style);
                  }
               }
            }

            while (!styleStack.isEmpty()) {
               Style style = styleStack.pop();
               listener.popStyle(style);
            }
         }
      }
   }

   @Nullable
   private <T extends Component> ComponentFlattenerImpl.Handler flattener(final T test) {
      ComponentFlattenerImpl.Handler flattener = this.flatteners.get((Class<? extends Component>)test.getClass());
      return flattener == null && this.unknownHandler != null
         ? (self, component, listener, depth, nestedDepth) -> listener.component(this.unknownHandler.apply(component))
         : flattener;
   }

   static final class BuilderImpl implements ComponentFlattener.Builder {
      private final InheritanceAwareMap.Builder<Component, ComponentFlattenerImpl.Handler> flatteners;
      @Nullable
      private Function<Component, String> unknownHandler;
      private int maxNestedDepth = AdventureProperties.DEFAULT_FLATTENER_NESTING_LIMIT.valueOr(-1);

      BuilderImpl() {
         this.flatteners = InheritanceAwareMap.<Component, ComponentFlattenerImpl.Handler>builder().strict(true);
      }

      BuilderImpl(
         final InheritanceAwareMap<Component, ComponentFlattenerImpl.Handler> flatteners,
         @Nullable final Function<Component, String> unknownHandler,
         final int maxNestedDepth
      ) {
         this.flatteners = InheritanceAwareMap.builder(flatteners).strict(true);
         this.unknownHandler = unknownHandler;
         this.maxNestedDepth = maxNestedDepth;
      }

      public ComponentFlattener build() {
         return new ComponentFlattenerImpl(this.flatteners.build(), this.unknownHandler, this.maxNestedDepth);
      }

      @Override
      public <T extends Component> ComponentFlattener.Builder mapper(final Class<T> type, final Function<T, String> converter) {
         this.flatteners.put(type, (self, component, listener, depth, nestedDepth) -> listener.component(converter.apply((T)component)));
         return this;
      }

      @Override
      public <T extends Component> ComponentFlattener.Builder complexMapper(final Class<T> type, final BiConsumer<T, Consumer<Component>> converter) {
         this.flatteners
            .put(
               type, (self, component, listener, depth, nestedDepth) -> converter.accept((T)component, c -> self.flatten0(c, listener, depth, nestedDepth + 1))
            );
         return this;
      }

      @Override
      public ComponentFlattener.Builder unknownMapper(@Nullable final Function<Component, String> converter) {
         this.unknownHandler = converter;
         return this;
      }

      @Override
      public ComponentFlattener.Builder nestingLimit(@Range(from = 1L,to = 2147483647L) final int limit) {
         if (limit != -1 && limit < 1) {
            throw new IllegalArgumentException("limit must be positive or ComponentFlattener.NO_NESTING_LIMIT");
         } else {
            this.maxNestedDepth = limit;
            return this;
         }
      }
   }

   @FunctionalInterface
   interface Handler {
      void handle(final ComponentFlattenerImpl self, final Component input, final FlattenerListener listener, final int depth, final int nestedDepth);
   }

   private record StackEntry(Component component, int depth, int stylesToPop) {
   }
}
