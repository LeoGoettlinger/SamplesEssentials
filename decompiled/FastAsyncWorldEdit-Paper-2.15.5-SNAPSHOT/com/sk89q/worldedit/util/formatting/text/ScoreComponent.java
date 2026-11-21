package com.sk89q.worldedit.util.formatting.text;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ScoreComponent extends BuildableComponent<ScoreComponent, ScoreComponent.Builder>, ScopedComponent<ScoreComponent> {
   @NonNull
   static ScoreComponent.Builder builder() {
      return new ScoreComponentImpl.BuilderImpl();
   }

   @NonNull
   static ScoreComponent.Builder builder(@NonNull final String name, @NonNull final String objective) {
      return builder().name(name).objective(objective);
   }

   @NonNull
   static ScoreComponent of(@NonNull final String name, @NonNull final String objective) {
      return of(name, objective, null);
   }

   @NonNull
   static ScoreComponent of(@NonNull final String name, @NonNull final String objective, @Nullable final String value) {
      return builder().name(name).objective(objective).value(value).build();
   }

   @NonNull
   static ScoreComponent make(@NonNull final Consumer<? super ScoreComponent.Builder> consumer) {
      ScoreComponent.Builder builder = builder();
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   static ScoreComponent make(@NonNull final String name, @NonNull final String objective, @NonNull final Consumer<? super ScoreComponent.Builder> consumer) {
      ScoreComponent.Builder builder = builder(name, objective);
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   String name();

   @NonNull
   ScoreComponent name(@NonNull final String name);

   @NonNull
   String objective();

   @NonNull
   ScoreComponent objective(@NonNull final String objective);

   @Nullable
   String value();

   @NonNull
   ScoreComponent value(@Nullable final String value);

   public interface Builder extends ComponentBuilder<ScoreComponent, ScoreComponent.Builder> {
      @NonNull
      ScoreComponent.Builder name(@NonNull final String name);

      @NonNull
      ScoreComponent.Builder objective(@NonNull final String objective);

      @NonNull
      ScoreComponent.Builder value(@Nullable final String value);
   }
}
