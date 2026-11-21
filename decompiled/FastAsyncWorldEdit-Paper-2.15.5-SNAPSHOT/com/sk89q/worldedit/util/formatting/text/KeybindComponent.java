package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import com.sk89q.worldedit.util.formatting.text.util.ShadyPines;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface KeybindComponent extends BuildableComponent<KeybindComponent, KeybindComponent.Builder>, ScopedComponent<KeybindComponent> {
   @NonNull
   static KeybindComponent.Builder builder() {
      return new KeybindComponentImpl.BuilderImpl();
   }

   @NonNull
   static KeybindComponent.Builder builder(@NonNull final String keybind) {
      return builder().keybind(keybind);
   }

   @NonNull
   static KeybindComponent of(@NonNull final String keybind) {
      return builder(keybind).build();
   }

   @NonNull
   static KeybindComponent of(@NonNull final String keybind, @Nullable final TextColor color) {
      return of(keybind, color, Collections.emptySet());
   }

   @NonNull
   static KeybindComponent of(@NonNull final String content, @Nullable final TextColor color, @NonNull final TextDecoration... decorations) {
      return of(content, color, ShadyPines.enumSet(TextDecoration.class, decorations));
   }

   @NonNull
   static KeybindComponent of(@NonNull final String keybind, @Nullable final TextColor color, @NonNull final Set<TextDecoration> decorations) {
      return builder(keybind).color(color).decorations(decorations, true).build();
   }

   @NonNull
   static KeybindComponent make(@NonNull final Consumer<? super KeybindComponent.Builder> consumer) {
      KeybindComponent.Builder builder = builder();
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   static KeybindComponent make(@NonNull final String keybind, @NonNull final Consumer<? super KeybindComponent.Builder> consumer) {
      KeybindComponent.Builder builder = builder(keybind);
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   String keybind();

   @NonNull
   KeybindComponent keybind(@NonNull final String keybind);

   public interface Builder extends ComponentBuilder<KeybindComponent, KeybindComponent.Builder> {
      @NonNull
      KeybindComponent.Builder keybind(@NonNull final String keybind);
   }
}
