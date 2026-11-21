package com.sk89q.worldedit.util.formatting.text;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface SelectorComponent extends BuildableComponent<SelectorComponent, SelectorComponent.Builder>, ScopedComponent<SelectorComponent> {
   @NonNull
   static SelectorComponent.Builder builder() {
      return new SelectorComponentImpl.BuilderImpl();
   }

   @NonNull
   static SelectorComponent.Builder builder(@NonNull final String pattern) {
      return builder().pattern(pattern);
   }

   @NonNull
   static SelectorComponent of(@NonNull final String pattern) {
      return builder(pattern).build();
   }

   @NonNull
   static SelectorComponent make(@NonNull final Consumer<? super SelectorComponent.Builder> consumer) {
      SelectorComponent.Builder builder = builder();
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   static SelectorComponent make(@NonNull final String pattern, @NonNull final Consumer<? super SelectorComponent.Builder> consumer) {
      SelectorComponent.Builder builder = builder(pattern);
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   String pattern();

   @NonNull
   SelectorComponent pattern(@NonNull final String pattern);

   public interface Builder extends ComponentBuilder<SelectorComponent, SelectorComponent.Builder> {
      @NonNull
      SelectorComponent.Builder pattern(@NonNull final String pattern);
   }
}
