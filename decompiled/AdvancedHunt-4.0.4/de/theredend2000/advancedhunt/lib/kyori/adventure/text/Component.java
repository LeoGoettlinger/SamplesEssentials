package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.ClickEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.HoverEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.HoverEventSource;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.ShadowColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.StyleBuilderApplicable;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.StyleGetter;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.StyleSetter;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextDecoration;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.object.ObjectContents;
import de.theredend2000.advancedhunt.lib.kyori.adventure.translation.Translatable;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.ARGBLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.ForwardingIterator;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.MonkeyBars;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.Nullable;

public sealed interface Component
   extends ComponentBuilderApplicable,
   ComponentLike,
   HoverEventSource<Component>,
   StyleGetter,
   StyleSetter<Component>
   permits NBTComponent,
   ScopedComponent {
   BiPredicate<? super Component, ? super Component> EQUALS = Objects::equals;
   BiPredicate<? super Component, ? super Component> EQUALS_IDENTITY = (a, b) -> a == b;
   Predicate<? super Component> IS_NOT_EMPTY = component -> component != empty();

   static TextComponent empty() {
      return TextComponentImpl.EMPTY;
   }

   static TextComponent newline() {
      return TextComponentImpl.NEWLINE;
   }

   static TextComponent space() {
      return TextComponentImpl.SPACE;
   }

   @Contract(
      pure = true
   )
   static Component join(final JoinConfiguration.Builder configBuilder, final ComponentLike... components) {
      return join(configBuilder, List.of(components));
   }

   @Contract(
      pure = true
   )
   static Component join(final JoinConfiguration.Builder configBuilder, final Iterable<? extends ComponentLike> components) {
      return JoinConfigurationImpl.join(configBuilder.build(), components);
   }

   @Contract(
      pure = true
   )
   static Component join(final JoinConfiguration config, final ComponentLike... components) {
      return join(config, List.of(components));
   }

   @Contract(
      pure = true
   )
   static Component join(final JoinConfiguration config, final Iterable<? extends ComponentLike> components) {
      return JoinConfigurationImpl.join(config, components);
   }

   static Collector<Component, ? extends ComponentBuilder<?, ?>, Component> toComponent() {
      return toComponent(empty());
   }

   static Collector<Component, ? extends ComponentBuilder<?, ?>, Component> toComponent(final Component separator) {
      return Collector.of(Component::text, (builder, add) -> {
         if (separator != empty() && !builder.children().isEmpty()) {
            builder.append(separator);
         }

         builder.append(add);
      }, (a, b) -> {
         List<Component> aChildren = a.children();
         TextComponent.Builder ret = text().append(aChildren);
         if (!aChildren.isEmpty()) {
            ret.append(separator);
         }

         ret.append(b.children());
         return ret;
      }, ComponentBuilder::build);
   }

   @Contract(
      pure = true
   )
   static BlockNBTComponent.Builder blockNBT() {
      return new BlockNBTComponentImpl.BuilderImpl();
   }

   @Contract("_ -> new")
   static BlockNBTComponent blockNBT(final Consumer<? super BlockNBTComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(blockNBT(), consumer);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static BlockNBTComponent blockNBT(final String nbtPath, final BlockNBTComponent.Pos pos) {
      return blockNBT(nbtPath, false, pos);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static BlockNBTComponent blockNBT(final String nbtPath, final boolean interpret, final BlockNBTComponent.Pos pos) {
      return blockNBT(nbtPath, interpret, null, pos);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static BlockNBTComponent blockNBT(final String nbtPath, final boolean interpret, @Nullable final ComponentLike separator, final BlockNBTComponent.Pos pos) {
      return BlockNBTComponentImpl.create(List.of(), Style.empty(), nbtPath, interpret, separator, pos, false);
   }

   @Contract(
      pure = true
   )
   static EntityNBTComponent.Builder entityNBT() {
      return new EntityNBTComponentImpl.BuilderImpl();
   }

   @Contract("_ -> new")
   static EntityNBTComponent entityNBT(final Consumer<? super EntityNBTComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(entityNBT(), consumer);
   }

   @Contract("_, _ -> new")
   static EntityNBTComponent entityNBT(final String nbtPath, final String selector) {
      return entityNBT().nbtPath(nbtPath).selector(selector).build();
   }

   @Contract(
      pure = true
   )
   static KeybindComponent.Builder keybind() {
      return new KeybindComponentImpl.BuilderImpl();
   }

   @Contract("_ -> new")
   static KeybindComponent keybind(final Consumer<? super KeybindComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(keybind(), consumer);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static KeybindComponent keybind(final String keybind) {
      return keybind(keybind, Style.empty());
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static KeybindComponent keybind(final KeybindComponent.KeybindLike keybind) {
      return keybind(Objects.requireNonNull(keybind, "keybind").asKeybind(), Style.empty());
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static KeybindComponent keybind(final String keybind, final Style style) {
      return KeybindComponentImpl.create(List.of(), Objects.requireNonNull(style, "style"), keybind);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static KeybindComponent keybind(final KeybindComponent.KeybindLike keybind, final Style style) {
      return KeybindComponentImpl.create(List.of(), Objects.requireNonNull(style, "style"), Objects.requireNonNull(keybind, "keybind").asKeybind());
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static KeybindComponent keybind(final String keybind, @Nullable final TextColor color) {
      return keybind(keybind, Style.style(color));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static KeybindComponent keybind(final KeybindComponent.KeybindLike keybind, @Nullable final TextColor color) {
      return keybind(Objects.requireNonNull(keybind, "keybind").asKeybind(), Style.style(color));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static KeybindComponent keybind(final String keybind, @Nullable final TextColor color, final TextDecoration... decorations) {
      return keybind(keybind, Style.style(color, decorations));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static KeybindComponent keybind(final KeybindComponent.KeybindLike keybind, @Nullable final TextColor color, final TextDecoration... decorations) {
      return keybind(Objects.requireNonNull(keybind, "keybind").asKeybind(), Style.style(color, decorations));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static KeybindComponent keybind(final String keybind, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return keybind(keybind, Style.style(color, decorations));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static KeybindComponent keybind(final KeybindComponent.KeybindLike keybind, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return keybind(Objects.requireNonNull(keybind, "keybind").asKeybind(), Style.style(color, decorations));
   }

   @Contract(
      pure = true
   )
   static ObjectComponent.Builder object() {
      return new ObjectComponentImpl.BuilderImpl();
   }

   @Contract("_ -> new")
   static ObjectComponent object(final Consumer<? super ObjectComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(object(), consumer);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static ObjectComponent object(final ObjectContents objectContents) {
      return ObjectComponentImpl.create(List.of(), Style.empty(), objectContents, null);
   }

   @Contract(
      pure = true
   )
   static ScoreComponent.Builder score() {
      return new ScoreComponentImpl.BuilderImpl();
   }

   @Contract("_ -> new")
   static ScoreComponent score(final Consumer<? super ScoreComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(score(), consumer);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static ScoreComponent score(final String name, final String objective) {
      return score(name, objective, null);
   }

   @Obsolete
   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static ScoreComponent score(final String name, final String objective, @Nullable final String value) {
      return ScoreComponentImpl.create(List.of(), Style.empty(), name, objective, value);
   }

   @Contract(
      pure = true
   )
   static SelectorComponent.Builder selector() {
      return new SelectorComponentImpl.BuilderImpl();
   }

   @Contract("_ -> new")
   static SelectorComponent selector(final Consumer<? super SelectorComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(selector(), consumer);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static SelectorComponent selector(final String pattern) {
      return selector(pattern, null);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static SelectorComponent selector(final String pattern, @Nullable final ComponentLike separator) {
      return SelectorComponentImpl.create(List.of(), Style.empty(), pattern, separator);
   }

   @Contract(
      pure = true
   )
   static StorageNBTComponent.Builder storageNBT() {
      return new StorageNBTComponentImpl.BuilderImpl();
   }

   @Contract("_ -> new")
   static StorageNBTComponent storageNBT(final Consumer<? super StorageNBTComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(storageNBT(), consumer);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static StorageNBTComponent storageNBT(final String nbtPath, final Key storage) {
      return storageNBT(nbtPath, false, storage);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static StorageNBTComponent storageNBT(final String nbtPath, final boolean interpret, final Key storage) {
      return storageNBT(nbtPath, interpret, null, storage);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static StorageNBTComponent storageNBT(final String nbtPath, final boolean interpret, @Nullable final ComponentLike separator, final Key storage) {
      return StorageNBTComponentImpl.create(List.of(), Style.empty(), nbtPath, interpret, separator, storage, false);
   }

   @Contract(
      pure = true
   )
   static TextComponent.Builder text() {
      return new TextComponentImpl.BuilderImpl();
   }

   static TextComponent textOfChildren(final ComponentLike... components) {
      return components.length == 0 ? empty() : TextComponentImpl.create(List.of(components), Style.empty(), "");
   }

   @Contract("_ -> new")
   static TextComponent text(final Consumer<? super TextComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(text(), consumer);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static TextComponent text(final String content) {
      return content.isEmpty() ? empty() : text(content, Style.empty());
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final String content, final Style style) {
      return TextComponentImpl.create(List.of(), Objects.requireNonNull(style, "style"), content);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final String content, @Nullable final TextColor color) {
      return text(content, Style.style(color));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final String content, @Nullable final TextColor color, final TextDecoration... decorations) {
      return text(content, Style.style(color, decorations));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final String content, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return text(content, Style.style(color, decorations));
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static TextComponent text(final boolean value) {
      return text(String.valueOf(value));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final boolean value, final Style style) {
      return text(String.valueOf(value), style);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final boolean value, @Nullable final TextColor color) {
      return text(String.valueOf(value), color);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final boolean value, @Nullable final TextColor color, final TextDecoration... decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final boolean value, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      pure = true
   )
   static TextComponent text(final char value) {
      if (value == '\n') {
         return newline();
      } else {
         return value == ' ' ? space() : text(String.valueOf(value));
      }
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final char value, final Style style) {
      return text(String.valueOf(value), style);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final char value, @Nullable final TextColor color) {
      return text(String.valueOf(value), color);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final char value, @Nullable final TextColor color, final TextDecoration... decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final char value, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static TextComponent text(final double value) {
      return text(String.valueOf(value));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final double value, final Style style) {
      return text(String.valueOf(value), style);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final double value, @Nullable final TextColor color) {
      return text(String.valueOf(value), color);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final double value, @Nullable final TextColor color, final TextDecoration... decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final double value, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static TextComponent text(final float value) {
      return text(String.valueOf(value));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final float value, final Style style) {
      return text(String.valueOf(value), style);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final float value, @Nullable final TextColor color) {
      return text(String.valueOf(value), color);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final float value, @Nullable final TextColor color, final TextDecoration... decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final float value, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static TextComponent text(final int value) {
      return text(String.valueOf(value));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final int value, final Style style) {
      return text(String.valueOf(value), style);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final int value, @Nullable final TextColor color) {
      return text(String.valueOf(value), color);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final int value, @Nullable final TextColor color, final TextDecoration... decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final int value, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static TextComponent text(final long value) {
      return text(String.valueOf(value));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final long value, final Style style) {
      return text(String.valueOf(value), style);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TextComponent text(final long value, @Nullable final TextColor color) {
      return text(String.valueOf(value), color);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final long value, @Nullable final TextColor color, final TextDecoration... decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TextComponent text(final long value, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return text(String.valueOf(value), color, decorations);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static <C> VirtualComponent virtual(final Class<C> contextType, final VirtualComponentRenderer<C> renderer) {
      Objects.requireNonNull(contextType, "context type");
      Objects.requireNonNull(renderer, "renderer");
      return VirtualComponentImpl.createVirtual(contextType, renderer);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static <C> VirtualComponent virtual(final Class<C> contextType, final VirtualComponentRenderer<C> renderer, final Style style) {
      Objects.requireNonNull(contextType, "context type");
      Objects.requireNonNull(renderer, "renderer");
      return VirtualComponentImpl.createVirtual(contextType, renderer, List.of(), style);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static <C> VirtualComponent virtual(final Class<C> contextType, final VirtualComponentRenderer<C> renderer, final StyleBuilderApplicable... style) {
      Objects.requireNonNull(contextType, "context type");
      Objects.requireNonNull(renderer, "renderer");
      return VirtualComponentImpl.createVirtual(contextType, renderer, List.of(), Style.style(style));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static <C> VirtualComponent virtual(final Class<C> contextType, final VirtualComponentRenderer<C> renderer, final Iterable<StyleBuilderApplicable> style) {
      Objects.requireNonNull(contextType, "context type");
      Objects.requireNonNull(renderer, "renderer");
      return VirtualComponentImpl.createVirtual(contextType, renderer, List.of(), Style.style(style));
   }

   @Contract(
      pure = true
   )
   static TranslatableComponent.Builder translatable() {
      return new TranslatableComponentImpl.BuilderImpl();
   }

   @Contract("_ -> new")
   static TranslatableComponent translatable(final Consumer<? super TranslatableComponent.Builder> consumer) {
      return AbstractBuilder.configureAndBuild(translatable(), consumer);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key) {
      return translatable(key, Style.empty());
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), Style.empty());
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final String fallback) {
      return translatable(key, fallback, Style.empty());
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final String fallback) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), fallback, Style.empty());
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, final Style style) {
      return TranslatableComponentImpl.create(List.of(), Objects.requireNonNull(style, "style"), key, null, List.of());
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, final Style style) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), style);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final String fallback, final Style style) {
      return TranslatableComponentImpl.create(List.of(), Objects.requireNonNull(style, "style"), key, fallback, List.of());
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final String fallback, final Style style) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), fallback, style);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final String fallback, final StyleBuilderApplicable... style) {
      return translatable(Objects.requireNonNull(key, "key"), fallback, Style.style(style));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final String fallback, final Iterable<StyleBuilderApplicable> style) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), fallback, Style.style(style));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final String fallback, final ComponentLike... args) {
      return translatable(key, fallback, Style.empty(), args);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final String fallback, final ComponentLike... args) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), fallback, args);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final String fallback, final Style style, final ComponentLike... args) {
      return TranslatableComponentImpl.create(List.of(), Objects.requireNonNull(style, "style"), key, fallback, Objects.requireNonNull(args, "args"));
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final String fallback, final Style style, final ComponentLike... args) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), fallback, style, args);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final String fallback, final Style style, final List<? extends ComponentLike> args) {
      return TranslatableComponentImpl.create(List.of(), style, key, fallback, Objects.requireNonNull(args, "args"));
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final Translatable translatable, @Nullable final String fallback, final Style style, final List<? extends ComponentLike> args
   ) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), fallback, style, args);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final String key, @Nullable final String fallback, final List<? extends ComponentLike> args, final Iterable<StyleBuilderApplicable> style
   ) {
      return TranslatableComponentImpl.create(List.of(), Style.style(style), key, fallback, Objects.requireNonNull(args, "args"));
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final Translatable translatable, @Nullable final String fallback, final List<? extends ComponentLike> args, final Iterable<StyleBuilderApplicable> style
   ) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), fallback, args, style);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final String key, @Nullable final String fallback, final List<? extends ComponentLike> args, final StyleBuilderApplicable... style
   ) {
      return TranslatableComponentImpl.create(List.of(), Style.style(style), key, fallback, Objects.requireNonNull(args, "args"));
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final Translatable translatable, @Nullable final String fallback, final List<? extends ComponentLike> args, final StyleBuilderApplicable... style
   ) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), fallback, args, style);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final TextColor color) {
      return translatable(key, Style.style(color));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final TextColor color) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), color);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final TextColor color, final TextDecoration... decorations) {
      return translatable(key, Style.style(color, decorations));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final TextColor color, final TextDecoration... decorations) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), color, decorations);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return translatable(key, Style.style(color, decorations));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final TextColor color, final Set<TextDecoration> decorations) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), color, decorations);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, final ComponentLike... args) {
      return translatable(key, Style.empty(), args);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, final ComponentLike... args) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), args);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, final Style style, final ComponentLike... args) {
      return TranslatableComponentImpl.create(List.of(), Objects.requireNonNull(style, "style"), key, null, Objects.requireNonNull(args, "args"));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, final Style style, final ComponentLike... args) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), style, args);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final TextColor color, final ComponentLike... args) {
      return translatable(key, Style.style(color), args);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final TextColor color, final ComponentLike... args) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), color, args);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final String key, @Nullable final TextColor color, final Set<TextDecoration> decorations, final ComponentLike... args
   ) {
      return translatable(key, Style.style(color, decorations), args);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final Translatable translatable, @Nullable final TextColor color, final Set<TextDecoration> decorations, final ComponentLike... args
   ) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), color, decorations, args);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, final List<? extends ComponentLike> args) {
      return TranslatableComponentImpl.create(List.of(), Style.empty(), key, null, Objects.requireNonNull(args, "args"));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, final List<? extends ComponentLike> args) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), args);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, final Style style, final List<? extends ComponentLike> args) {
      return TranslatableComponentImpl.create(List.of(), Objects.requireNonNull(style, "style"), key, null, Objects.requireNonNull(args, "args"));
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, final Style style, final List<? extends ComponentLike> args) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), style, args);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final String key, @Nullable final TextColor color, final List<? extends ComponentLike> args) {
      return translatable(key, Style.style(color), args);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(final Translatable translatable, @Nullable final TextColor color, final List<? extends ComponentLike> args) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), color, args);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final String key, @Nullable final TextColor color, final Set<TextDecoration> decorations, final List<? extends ComponentLike> args
   ) {
      return translatable(key, Style.style(color, decorations), args);
   }

   @Contract(
      value = "_, _, _, _ -> new",
      pure = true
   )
   static TranslatableComponent translatable(
      final Translatable translatable, @Nullable final TextColor color, final Set<TextDecoration> decorations, final List<? extends ComponentLike> args
   ) {
      return translatable(Objects.requireNonNull(translatable, "translatable").translationKey(), color, decorations, args);
   }

   @Unmodifiable
   List<Component> children();

   @Contract(
      pure = true
   )
   Component children(final List<? extends ComponentLike> children);

   default boolean contains(final Component that) {
      return this.contains(that, EQUALS_IDENTITY);
   }

   default boolean contains(final Component that, final BiPredicate<? super Component, ? super Component> equals) {
      if (equals.test(this, that)) {
         return true;
      } else {
         for (Component child : this.children()) {
            if (child.contains(that, equals)) {
               return true;
            }
         }

         HoverEvent<?> hoverEvent = this.hoverEvent();
         if (hoverEvent != null) {
            Object value = hoverEvent.value();
            Component component = null;
            if (value instanceof Component) {
               component = (Component)hoverEvent.value();
            } else if (value instanceof HoverEvent.ShowEntity se) {
               component = se.name();
            }

            if (component != null) {
               if (equals.test(that, component)) {
                  return true;
               }

               for (Component childx : component.children()) {
                  if (childx.contains(that, equals)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   @Contract(
      pure = true
   )
   default Component append(final Component component) {
      return this.append((ComponentLike)component);
   }

   default Component append(final ComponentLike like) {
      Objects.requireNonNull(like, "like");
      Component component = like.asComponent();
      Objects.requireNonNull(component, "component");
      if (component == empty()) {
         return this;
      } else {
         List<Component> oldChildren = this.children();
         return this.children(MonkeyBars.addOne(oldChildren, component));
      }
   }

   @Contract(
      pure = true
   )
   default Component append(final ComponentBuilder<?, ?> builder) {
      return this.append(builder.build());
   }

   @Contract(
      pure = true
   )
   default Component appendNewline() {
      return this.append((Component)newline());
   }

   @Contract(
      pure = true
   )
   default Component appendSpace() {
      return this.append((Component)space());
   }

   @Contract(
      pure = true
   )
   default Component append(final ComponentLike... components) {
      if (components.length == 0) {
         return this;
      } else {
         List<ComponentLike> newChildren = new ArrayList<>(components.length + this.children().size());
         newChildren.addAll(this.children());
         Collections.addAll(newChildren, components);
         return this.children(newChildren);
      }
   }

   @Contract(
      pure = true
   )
   default Component append(final List<? extends ComponentLike> components) {
      if (components.isEmpty()) {
         return this;
      } else if (this.children().isEmpty()) {
         return this.children(components);
      } else {
         List<ComponentLike> newChildren = new ArrayList<>(components.size() + this.children().size());
         newChildren.addAll(this.children());
         newChildren.addAll(components);
         return this.children(newChildren);
      }
   }

   @Contract(
      pure = true
   )
   default Component applyFallbackStyle(final Style style) {
      Objects.requireNonNull(style, "style");
      return this.style(this.style().merge(style, Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
   }

   @Contract(
      pure = true
   )
   default Component applyFallbackStyle(final StyleBuilderApplicable... style) {
      return this.applyFallbackStyle(Style.style(style));
   }

   Style style();

   @Contract(
      pure = true
   )
   Component style(final Style style);

   @Contract(
      pure = true
   )
   default Component style(final Consumer<Style.Builder> consumer) {
      return this.style(this.style().edit(consumer));
   }

   @Contract(
      pure = true
   )
   default Component style(final Consumer<Style.Builder> consumer, final Style.Merge.Strategy strategy) {
      return this.style(this.style().edit(consumer, strategy));
   }

   @Contract(
      pure = true
   )
   default Component style(final Style.Builder style) {
      return this.style(style.build());
   }

   @Contract(
      pure = true
   )
   default Component mergeStyle(final Component that) {
      return this.mergeStyle(that, Style.Merge.all());
   }

   @Contract(
      pure = true
   )
   default Component mergeStyle(final Component that, final Style.Merge... merges) {
      return this.mergeStyle(that, Style.Merge.merges(merges));
   }

   @Contract(
      pure = true
   )
   default Component mergeStyle(final Component that, final Set<Style.Merge> merges) {
      return this.style(this.style().merge(that.style(), merges));
   }

   @Nullable
   @Override
   default Key font() {
      return this.style().font();
   }

   default Component font(@Nullable final Key key) {
      return this.style(this.style().font(key));
   }

   @Nullable
   @Override
   default TextColor color() {
      return this.style().color();
   }

   @Nullable
   @Override
   default ShadowColor shadowColor() {
      return this.style().shadowColor();
   }

   @Contract(
      pure = true
   )
   default Component color(@Nullable final TextColor color) {
      return this.style(this.style().color(color));
   }

   @Contract(
      pure = true
   )
   default Component colorIfAbsent(@Nullable final TextColor color) {
      return this.color() == null ? this.color(color) : this;
   }

   @Contract(
      pure = true
   )
   default Component shadowColor(@Nullable final ARGBLike argb) {
      return this.style(this.style().shadowColor(argb));
   }

   @Contract(
      pure = true
   )
   default Component shadowColorIfAbsent(@Nullable final ARGBLike argb) {
      return this.shadowColor() == null ? this.shadowColor(argb) : this;
   }

   @Override
   default boolean hasDecoration(final TextDecoration decoration) {
      return StyleGetter.super.hasDecoration(decoration);
   }

   @Contract(
      pure = true
   )
   default Component decorate(final TextDecoration decoration) {
      return (Component)StyleSetter.super.decorate(decoration);
   }

   @Override
   default TextDecoration.State decoration(final TextDecoration decoration) {
      return this.style().decoration(decoration);
   }

   @Contract(
      pure = true
   )
   default Component decoration(final TextDecoration decoration, final boolean flag) {
      return (Component)StyleSetter.super.decoration(decoration, flag);
   }

   @Contract(
      pure = true
   )
   default Component decoration(final TextDecoration decoration, final TextDecoration.State state) {
      return this.style(this.style().decoration(decoration, state));
   }

   default Component decorationIfAbsent(final TextDecoration decoration, final TextDecoration.State state) {
      Objects.requireNonNull(state, "state");
      TextDecoration.State oldState = this.decoration(decoration);
      return oldState == TextDecoration.State.NOT_SET ? this.style(this.style().decoration(decoration, state)) : this;
   }

   @Override
   default Map<TextDecoration, TextDecoration.State> decorations() {
      return this.style().decorations();
   }

   @Contract(
      pure = true
   )
   default Component decorations(final Map<TextDecoration, TextDecoration.State> decorations) {
      return this.style(this.style().decorations(decorations));
   }

   @Nullable
   @Override
   default ClickEvent<?> clickEvent() {
      return this.style().clickEvent();
   }

   @Contract(
      pure = true
   )
   default Component clickEvent(@Nullable final ClickEvent<?> event) {
      return this.style(this.style().clickEvent(event));
   }

   @Nullable
   @Override
   default HoverEvent<?> hoverEvent() {
      return this.style().hoverEvent();
   }

   @Contract(
      pure = true
   )
   default Component hoverEvent(@Nullable final HoverEventSource<?> source) {
      return this.style(this.style().hoverEvent(source));
   }

   @Nullable
   @Override
   default String insertion() {
      return this.style().insertion();
   }

   @Contract(
      pure = true
   )
   default Component insertion(@Nullable final String insertion) {
      return this.style(this.style().insertion(insertion));
   }

   default boolean hasStyling() {
      return !this.style().isEmpty();
   }

   @ScopedComponentOverrideNotRequired
   @Contract(
      pure = true
   )
   default Component replaceText(final Consumer<TextReplacementConfig.Builder> configurer) {
      Objects.requireNonNull(configurer, "configurer");
      return this.replaceText(AbstractBuilder.configureAndBuild(TextReplacementConfig.builder(), configurer));
   }

   @ScopedComponentOverrideNotRequired
   @Contract(
      pure = true
   )
   default Component replaceText(final TextReplacementConfig config) {
      Objects.requireNonNull(config, "replacement");
      return TextReplacementRenderer.INSTANCE.render(this, ((TextReplacementConfigImpl)config).createState());
   }

   @ScopedComponentOverrideNotRequired
   default Component compact() {
      return this.compact(null);
   }

   @ScopedComponentOverrideNotRequired
   default Component compact(@Nullable final Style parentStyle) {
      return ComponentCompaction.compact(this, parentStyle);
   }

   default Iterable<Component> iterable(final ComponentIteratorType type, @Nullable final ComponentIteratorFlag... flags) {
      return this.iterable(type, flags == null ? Set.of() : MonkeyBars.enumSet(ComponentIteratorFlag.class, flags));
   }

   default Iterable<Component> iterable(final ComponentIteratorType type, final Set<ComponentIteratorFlag> flags) {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(flags, "flags");
      return new ForwardingIterator<>(() -> this.iterator(type, flags), () -> this.spliterator(type, flags));
   }

   default Iterator<Component> iterator(final ComponentIteratorType type, @Nullable final ComponentIteratorFlag... flags) {
      return this.iterator(type, flags == null ? Set.of() : MonkeyBars.enumSet(ComponentIteratorFlag.class, flags));
   }

   default Iterator<Component> iterator(final ComponentIteratorType type, final Set<ComponentIteratorFlag> flags) {
      return new ComponentIterator(this, Objects.requireNonNull(type, "type"), Objects.requireNonNull(flags, "flags"));
   }

   default Spliterator<Component> spliterator(final ComponentIteratorType type, @Nullable final ComponentIteratorFlag... flags) {
      return this.spliterator(type, flags == null ? Set.of() : MonkeyBars.enumSet(ComponentIteratorFlag.class, flags));
   }

   default Spliterator<Component> spliterator(final ComponentIteratorType type, final Set<ComponentIteratorFlag> flags) {
      return Spliterators.spliteratorUnknownSize(this.iterator(type, flags), 1296);
   }

   ComponentBuilder<?, ?> toBuilder();

   @Override
   default void componentBuilderApply(final ComponentBuilder<?, ?> component) {
      component.append(this);
   }

   @Override
   default Component asComponent() {
      return this;
   }

   @Override
   default HoverEvent<Component> asHoverEvent(final UnaryOperator<Component> op) {
      return HoverEvent.showText(op.apply(this));
   }
}
