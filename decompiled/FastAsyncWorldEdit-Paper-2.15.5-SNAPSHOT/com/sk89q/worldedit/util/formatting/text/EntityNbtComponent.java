package com.sk89q.worldedit.util.formatting.text;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface EntityNbtComponent extends NbtComponent<EntityNbtComponent, EntityNbtComponent.Builder>, ScopedComponent<EntityNbtComponent> {
   @NonNull
   static EntityNbtComponent.Builder builder() {
      return new EntityNbtComponentImpl.BuilderImpl();
   }

   @NonNull
   static EntityNbtComponent of(@NonNull final String nbtPath, @NonNull final String selector) {
      return builder().nbtPath(nbtPath).selector(selector).build();
   }

   @NonNull
   static EntityNbtComponent make(@NonNull final Consumer<? super EntityNbtComponent.Builder> consumer) {
      EntityNbtComponent.Builder builder = builder();
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   String selector();

   @NonNull
   EntityNbtComponent selector(@NonNull final String selector);

   public interface Builder extends NbtComponentBuilder<EntityNbtComponent, EntityNbtComponent.Builder> {
      @NonNull
      EntityNbtComponent.Builder selector(@NonNull final String selector);
   }
}
