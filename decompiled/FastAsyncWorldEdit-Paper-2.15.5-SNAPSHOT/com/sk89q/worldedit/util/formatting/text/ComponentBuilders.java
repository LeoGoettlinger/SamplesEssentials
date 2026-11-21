package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ComponentBuilders {
   @NonNull
   static BlockNbtComponent.Builder blockNbt() {
      return BlockNbtComponent.builder();
   }

   @NonNull
   static EntityNbtComponent.Builder entityNbt() {
      return EntityNbtComponent.builder();
   }

   @NonNull
   static StorageNbtComponent.Builder storageNbt() {
      return StorageNbtComponent.builder();
   }

   @NonNull
   static KeybindComponent.Builder keybind() {
      return KeybindComponent.builder();
   }

   @NonNull
   static KeybindComponent.Builder keybind(@NonNull final String keybind) {
      return KeybindComponent.builder(keybind);
   }

   @NonNull
   static ScoreComponent.Builder score() {
      return ScoreComponent.builder();
   }

   @NonNull
   static ScoreComponent.Builder score(@NonNull final String name, @NonNull final String objective) {
      return ScoreComponent.builder(name, objective);
   }

   @NonNull
   static SelectorComponent.Builder selector() {
      return SelectorComponent.builder();
   }

   @NonNull
   static SelectorComponent.Builder selector(@NonNull final String pattern) {
      return SelectorComponent.builder(pattern);
   }

   @NonNull
   static TextComponent.Builder text() {
      return TextComponent.builder();
   }

   @NonNull
   static TextComponent.Builder text(@NonNull final String content, @Nullable final TextColor color) {
      return TextComponent.builder(content).color(color);
   }

   @NonNull
   static TextComponent.Builder text(final char value) {
      return text(String.valueOf(value));
   }

   @NonNull
   static TextComponent.Builder text(final double value) {
      return text(String.valueOf(value));
   }

   @NonNull
   static TextComponent.Builder text(final float value) {
      return text(String.valueOf(value));
   }

   @NonNull
   static TextComponent.Builder text(final int value) {
      return text(String.valueOf(value));
   }

   @NonNull
   static TextComponent.Builder text(final long value) {
      return text(String.valueOf(value));
   }

   @NonNull
   static TextComponent.Builder text(@NonNull final String content) {
      return TextComponent.builder(content);
   }

   @NonNull
   static TranslatableComponent.Builder translatable() {
      return TranslatableComponent.builder();
   }

   @NonNull
   static TranslatableComponent.Builder translatable(@NonNull final String key) {
      return TranslatableComponent.builder(key);
   }
}
