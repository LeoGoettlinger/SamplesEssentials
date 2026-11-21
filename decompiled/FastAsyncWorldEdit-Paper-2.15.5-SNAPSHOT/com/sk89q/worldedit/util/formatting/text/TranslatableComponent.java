package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import com.sk89q.worldedit.util.formatting.text.util.ShadyPines;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface TranslatableComponent extends BuildableComponent<TranslatableComponent, TranslatableComponent.Builder>, ScopedComponent<TranslatableComponent> {
   @NonNull
   static TranslatableComponent.Builder builder() {
      return new TranslatableComponentImpl.BuilderImpl();
   }

   @NonNull
   static TranslatableComponent.Builder builder(@NonNull final String key) {
      return builder().key(key);
   }

   @NonNull
   static TranslatableComponent of(@NonNull final String key) {
      return builder(key).build();
   }

   @NonNull
   static TranslatableComponent of(@NonNull final String key, @Nullable final TextColor color) {
      return builder(key).color(color).build();
   }

   @NonNull
   static TranslatableComponent of(@NonNull final String key, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(key, color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static TranslatableComponent of(@NonNull final String key, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(key).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static TranslatableComponent of(@NonNull final String key, final Component... args) {
      return of(key, null, args);
   }

   @NonNull
   static TranslatableComponent of(@NonNull final String key, @Nullable final TextColor color, final Component... args) {
      return of(key, color, Collections.emptySet(), args);
   }

   @NonNull
   static TranslatableComponent of(
      @NonNull final String key, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations, final Component... args
   ) {
      return of(key, color, decorations, Arrays.asList(args));
   }

   @NonNull
   static TranslatableComponent of(@NonNull final String key, @NonNull final List<? extends Component> args) {
      return of(key, null, args);
   }

   static TranslatableComponent of(@NonNull final String key, @Nullable final TextColor color, @NonNull final List<? extends Component> args) {
      return of(key, color, Collections.emptySet(), args);
   }

   @NonNull
   static TranslatableComponent of(
      @NonNull final String key, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations, @NonNull final List<? extends Component> args
   ) {
      return builder(key).color(color).decorations(decorations, true).args(args).build();
   }

   @NonNull
   static TranslatableComponent make(@NonNull final Consumer<? super TranslatableComponent.Builder> consumer) {
      TranslatableComponent.Builder builder = builder();
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   static TranslatableComponent make(@NonNull final String key, @NonNull final Consumer<? super TranslatableComponent.Builder> consumer) {
      TranslatableComponent.Builder builder = builder(key);
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   static TranslatableComponent make(
      @NonNull final String key, @NonNull final List<? extends Component> args, @NonNull final Consumer<? super TranslatableComponent.Builder> consumer
   ) {
      TranslatableComponent.Builder builder = builder(key).args(args);
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   String key();

   @NonNull
   TranslatableComponent key(@NonNull final String key);

   @NonNull
   List<Component> args();

   @NonNull
   TranslatableComponent args(@NonNull final Component... args);

   @NonNull
   TranslatableComponent args(@NonNull final List<? extends Component> args);

   public interface Builder extends ComponentBuilder<TranslatableComponent, TranslatableComponent.Builder> {
      @NonNull
      TranslatableComponent.Builder key(@NonNull final String key);

      @NonNull
      TranslatableComponent.Builder args(@NonNull final ComponentBuilder<?, ?> arg);

      @NonNull
      TranslatableComponent.Builder args(final ComponentBuilder<?, ?>... args);

      @NonNull
      TranslatableComponent.Builder args(@NonNull final Component arg);

      @NonNull
      TranslatableComponent.Builder args(final Component... args);

      @NonNull
      TranslatableComponent.Builder args(@NonNull final List<? extends Component> args);
   }
}
