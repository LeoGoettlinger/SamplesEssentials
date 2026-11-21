package com.earth2me.essentials.libs.kyori.adventure.text.flattener;

import com.earth2me.essentials.libs.kyori.adventure.internal.properties.AdventureProperties;
import com.earth2me.essentials.libs.kyori.adventure.key.Key;
import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import com.earth2me.essentials.libs.kyori.adventure.text.KeybindComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.ObjectComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.ScoreComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.SelectorComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.TextComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.TranslatableComponent;
import com.earth2me.essentials.libs.kyori.adventure.text.format.Style;
import com.earth2me.essentials.libs.kyori.adventure.text.object.ObjectContents;
import com.earth2me.essentials.libs.kyori.adventure.text.object.PlayerHeadObjectContents;
import com.earth2me.essentials.libs.kyori.adventure.text.object.SpriteObjectContents;
import com.earth2me.essentials.libs.kyori.adventure.util.InheritanceAwareMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

final class ComponentFlattenerImpl implements ComponentFlattener {
   static final ComponentFlattener BASIC = new ComponentFlattenerImpl.BuilderImpl()
      .mapper(KeybindComponent.class, component -> component.keybind())
      .mapper(ScoreComponent.class, component -> {
         String value = component.value();
         return value != null ? value : "";
      })
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
            if (contents instanceof SpriteObjectContents) {
               SpriteObjectContents spriteContents = (SpriteObjectContents)contents;
               Key atlas = spriteContents.atlas();
               return "["
                  + spriteContents.sprite().asMinimalString()
                  + (!atlas.equals(SpriteObjectContents.DEFAULT_ATLAS) ? "@" + atlas.asMinimalString() : "")
                  + "]";
            } else if (contents instanceof PlayerHeadObjectContents) {
               PlayerHeadObjectContents playerHeadContents = (PlayerHeadObjectContents)contents;
               return "[" + (playerHeadContents.name() != null ? playerHeadContents.name() : "unknown player") + " head]";
            } else {
               return "";
            }
         }
      )
      .build();
   static final ComponentFlattener TEXT_ONLY = new ComponentFlattenerImpl.BuilderImpl().mapper(TextComponent.class, TextComponent::content).build();
   private static final int MAX_DEPTH = 512;
   private final InheritanceAwareMap<Component, ComponentFlattenerImpl.Handler> flatteners;
   private final Function<Component, String> unknownHandler;
   private final int maxNestedDepth;

   ComponentFlattenerImpl(
      final InheritanceAwareMap<Component, ComponentFlattenerImpl.Handler> flatteners,
      @Nullable final Function<Component, String> unknownHandler,
      final int maxNestedDepth
   ) {
      this.flatteners = flatteners;
      this.unknownHandler = unknownHandler;
      this.maxNestedDepth = maxNestedDepth;
   }

   @Override
   public void flatten(@NotNull final Component input, @NotNull final FlattenerListener listener) {
      this.flatten0(input, listener, 0, 0);
   }

   private void flatten0(@NotNull final Component input, @NotNull final FlattenerListener listener, final int depth, final int nestedDepth) {
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

   @NotNull
   public ComponentFlattener.Builder toBuilder() {
      return new ComponentFlattenerImpl.BuilderImpl(this.flatteners, this.unknownHandler, this.maxNestedDepth);
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

      @NotNull
      public ComponentFlattener build() {
         return new ComponentFlattenerImpl(this.flatteners.build(), this.unknownHandler, this.maxNestedDepth);
      }

      @NotNull
      @Override
      public <T extends Component> ComponentFlattener.Builder mapper(@NotNull final Class<T> type, @NotNull final Function<T, String> converter) {
         this.flatteners.put(type, (self, component, listener, depth, nestedDepth) -> listener.component(converter.apply((T)component)));
         return this;
      }

      @NotNull
      @Override
      public <T extends Component> ComponentFlattener.Builder complexMapper(
         @NotNull final Class<T> type, @NotNull final BiConsumer<T, Consumer<Component>> converter
      ) {
         this.flatteners
            .put(
               type, (self, component, listener, depth, nestedDepth) -> converter.accept((T)component, c -> self.flatten0(c, listener, depth, nestedDepth + 1))
            );
         return this;
      }

      @NotNull
      @Override
      public ComponentFlattener.Builder unknownMapper(@Nullable final Function<Component, String> converter) {
         this.unknownHandler = converter;
         return this;
      }

      @NotNull
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

   private static final class StackEntry {
      final Component component;
      final int depth;
      final int stylesToPop;

      StackEntry(final Component component, final int depth, final int stylesToPop) {
         this.component = component;
         this.depth = depth;
         this.stylesToPop = stylesToPop;
      }
   }
}
